package com.capstone;

import com.capstone.exceptions.NotamApiException;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fetches raw NOTAM JSON from the NMS API.
 * <p>
 * How it works: - First requests a bearer token using the client credentials
 * flow - Reuses the token until it is close to expiration - Sends authenticated
 * NOTAM requests using the bearer token - Supports fetching NOTAMs by ICAO code
 * or by latitude/longitude/radius
 * <p>
 * - Returns raw JSON as a String
 *
 */
public class NmsNotamFetcher implements NotamDataFetcher
{
	// NM = Nautical Miles. Max allowed by API
	private static final int MAX_RADIUS_NM = 100;
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds( 30 );
	// Refresh token slightly before expiration to avoid edge-case failures
	private static final long TOKEN_REFRESH_BUFFER_SECONDS = 60;
	private static final long DEFAULT_TOKEN_TTL_SECONDS = 1800;

	private static final ObjectMapper mapper = new ObjectMapper();

	private final String clientId;
	private final String clientSecret;
	private final String authUrl;
	private final String notamBaseUrl;
	private final HttpClient httpClient;

	private String bearerToken;
	private Instant tokenExpiresAt;

	public NmsNotamFetcher()
	{
		Dotenv dotenv = Dotenv.load();
		this.clientId = requireEnv( dotenv, "CLIENT_ID" );
		this.clientSecret = requireEnv( dotenv, "CLIENT_SECRET" );
		this.authUrl = requireEnv( dotenv, "NMS_AUTH_URL" );
		this.notamBaseUrl = requireEnv( dotenv, "NMS_NOTAM_BASE_URL" );

		this.httpClient = HttpClient.newBuilder()
				.connectTimeout( REQUEST_TIMEOUT ).build();
	}

	// Test-only constructor for injecting mock dependencies
	NmsNotamFetcher( String clientId,
					 String clientSecret,
					 String authUrl,
					 String notamBaseUrl,
					 HttpClient httpClient )
	{
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.authUrl = authUrl;
		this.notamBaseUrl = notamBaseUrl;
		this.httpClient = httpClient;
	}

	public String fetchByIcao( String icaoCode )
			throws IOException, InterruptedException
	{
		validateIcaoCode( icaoCode );

		String location = icaoCode.toUpperCase();
		String url = notamBaseUrl + "?location=" + URLEncoder.encode( location,
				StandardCharsets.UTF_8 );

		return sendNotamRequest( url );
	}

	public String fetchByLocation( double latitude,
								   double longitude,
								   double radiusNm )
			throws IOException, InterruptedException
	{
		validateCoordinates( latitude, longitude );
		validateRadius( radiusNm );

		String url = notamBaseUrl + "?latitude=" + latitude + "&longitude="
				+ longitude + "&radius=" + radiusNm;

		return sendNotamRequest( url );
	}

	// Sends the NOTAM request with the required headers.
	private String sendNotamRequest( String url )
			throws IOException, InterruptedException
	{
		String token = getBearerToken();

		HttpRequest request = HttpRequest.newBuilder().uri( URI.create( url ) )
				.header( "Authorization", "Bearer " + token )
				.header( "Accept", "application/json" )
				.header( "nmsResponseFormat", "GEOJSON" ).GET()
				.timeout( REQUEST_TIMEOUT ).build();

		HttpResponse<String> response = httpClient.send( request,
				HttpResponse.BodyHandlers.ofString() );

		int status = response.statusCode();
		if( status < 200 || status >= 300 ) {
			throw new NotamApiException( status,
					"NOTAM API returned HTTP " + status + ": "
							+ response.body() );
		}

		return response.body();
	}

	private String getBearerToken() throws IOException, InterruptedException
	{
		if( bearerToken != null && tokenExpiresAt != null && Instant.now()
				.isBefore( tokenExpiresAt ) ) {
			return bearerToken;
		}

		String basicAuth = Base64.getEncoder().encodeToString(
				(clientId + ":" + clientSecret).getBytes(
						StandardCharsets.UTF_8 ) );

		HttpRequest request = HttpRequest.newBuilder()
				.uri( URI.create( authUrl ) )
				.header( "Authorization", "Basic " + basicAuth )
				.header( "Content-Type", "application/x-www-form-urlencoded" )
				.header( "Accept", "application/json" )
				.POST( HttpRequest.BodyPublishers.ofString(
						"grant_type=client_credentials" ) )
				.timeout( REQUEST_TIMEOUT ).build();

		HttpResponse<String> response = httpClient.send( request,
				HttpResponse.BodyHandlers.ofString() );

		int status = response.statusCode();
		if( status < 200 || status >= 300 ) {
			throw new NotamApiException( status,
					"Token request returned HTTP " + status + ": "
							+ response.body() );
		}

		String accessToken = parseAccessToken( response.body() );
		long expiresInSeconds = parseExpiresInSeconds( response.body() );

		this.bearerToken = accessToken;

		// Clamp to 0 in case the server returns an expires_in shorter than the buffer
		this.tokenExpiresAt = Instant.now().plusSeconds( Math.max( 0,
				expiresInSeconds - TOKEN_REFRESH_BUFFER_SECONDS ) );

		return bearerToken;
	}

	private static String parseAccessToken( String responseBody )
	{
		try {
			JsonNode root = mapper.readTree( responseBody );
			JsonNode tokenNode = root.get( "access_token" );

			if( tokenNode == null || tokenNode.isNull() ) {
				throw new IllegalStateException(
						"access_token was not found in token response: "
								+ responseBody );
			}

			return tokenNode.asText();
		}
		catch( Exception e ) {
			throw new IllegalStateException(
					"Failed to parse access_token from response: "
							+ responseBody, e );
		}
	}

	private static long parseExpiresInSeconds( String responseBody )
	{
		try {
			JsonNode root = mapper.readTree( responseBody );
			JsonNode expiresNode = root.get( "expires_in" );

			if( expiresNode == null || expiresNode.isNull() ) {
				return DEFAULT_TOKEN_TTL_SECONDS;
			}

			return expiresNode.asLong();
		}
		catch( Exception e ) {
			return DEFAULT_TOKEN_TTL_SECONDS;
		}
	}

	// Request validation

	private static void validateIcaoCode( String icaoCode )
	{
		if( icaoCode == null || icaoCode.isBlank() ) {
			throw new IllegalArgumentException(
					"ICAO code must not be null or blank" );
		}
		if( !icaoCode.matches( "^[A-Za-z][A-Za-z0-9]{3}$" ) ) {
			throw new IllegalArgumentException(
					"Invalid ICAO code: '" + icaoCode
							+ "'. Expected a letter followed by"
							+ " 3 alphanumeric characters." );
		}
	}

	private static void validateCoordinates( double latitude, double longitude )
	{
		if( latitude < -90 || latitude > 90 ) {
			throw new IllegalArgumentException(
					"Latitude must be between -90 and 90, currently: "
							+ latitude );
		}
		if( longitude < -180 || longitude > 180 ) {
			throw new IllegalArgumentException(
					"Longitude must be between -180 and 180, currently: "
							+ longitude );
		}
	}

	private static void validateRadius( double radiusNm )
	{
		if( radiusNm <= 0 || radiusNm > MAX_RADIUS_NM ) {
			throw new IllegalArgumentException(
					"Radius must be greater than 0 and at most " + MAX_RADIUS_NM
							+ ", currently: " + radiusNm );
		}
	}

	private static String requireEnv( Dotenv dotenv, String key )
	{
		String value = dotenv.get( key );
		if( value == null || value.isBlank() ) {
			throw new IllegalArgumentException(
					key + " is missing from .env file" );
		}
		return value;
	}
}
