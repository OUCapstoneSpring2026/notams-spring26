package com.capstone.fetching;

import com.capstone.exceptions.NotamApiException;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// This class returns raw Json. No mapping or parsing is done here

public class NotamFetcher implements NotamFetcherInterface
{
	private static final String BASE_URL = "https://external-api.faa.gov/notamapi/v1/notams";
	private static final int DEFAULT_PAGE_SIZE = 1000; // Large number to pull up to 1000 notams at once
	private static final int MAX_RADIUS_NM = 100; // NM = Nautical Miles. Max allowed by API
	private static final Duration TIMEOUT = Duration.ofSeconds( 30 );

	private final String clientId;
	private final String clientSecret;
	private final HttpClient httpClient;

	public NotamFetcher()
	{
		Dotenv dotenv = Dotenv.load();
		this.clientId = requireEnv( dotenv, "CLIENT_ID" );
		this.clientSecret = requireEnv( dotenv, "CLIENT_SECRET" );

		this.httpClient = HttpClient.newBuilder().connectTimeout( TIMEOUT )
				.build();
	}

	// Package-private constructor for testing purposes
	NotamFetcher( String clientId, String clientSecret, HttpClient httpClient )
	{
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.httpClient = httpClient;
	}

	// Overload: defaults to page 1 and DEFAULT_PAGE_SIZE results.
	public String fetchByIcao( String icaoCode )    throws IOException,
													InterruptedException
	{
		return fetchByIcao( icaoCode, DEFAULT_PAGE_SIZE, 1 );
	}

	// Overload: can specify pageNum and pageSize
	public String fetchByIcao( String icaoCode, int pageSize, int pageNum )
																			throws IOException,
																			InterruptedException
	{

		validateIcaoCode( icaoCode );
		validatePagination( pageSize, pageNum );

		String icao = icaoCode.toUpperCase();

		String url = BASE_URL + "?" + "responseFormat=geoJson"
				+ "&icaoLocation=" + icao + "&pageSize=" + pageSize
				+ "&pageNum=" + pageNum;

		return sendRequest( url );
	}

	// Overload: defaults to page 1 and DEFAULT_PAGE_SIZE results.
	public String fetchByCoordinates(   double latitude,
										double longitude,
										double radiusNm )   throws IOException,
															InterruptedException
	{
		return fetchByCoordinates( latitude, longitude, radiusNm,
				DEFAULT_PAGE_SIZE, 1 );
	}

	// Overload: can specify pageSize and pageNum.
	public String fetchByCoordinates(   double latitude,
										double longitude,
										double radiusNm,
										int pageSize,
										int pageNum )   throws IOException,
														InterruptedException
	{

		validateCoordinates( latitude, longitude );
		validateRadius( radiusNm );
		validatePagination( pageSize, pageNum );

		String url = BASE_URL + "?" + "responseFormat=geoJson"
				+ "&locationLatitude=" + latitude + "&locationLongitude="
				+ longitude + "&locationRadius=" + radiusNm + "&pageSize="
				+ pageSize + "&pageNum=" + pageNum;

		return sendRequest( url );
	}

	// This sends the http get request, it enforces the required headers and a 30-second timeout
	private String sendRequest( String url )    throws IOException,
												InterruptedException
	{
		HttpRequest request = HttpRequest.newBuilder().uri( URI.create( url ) )
				.header( "client_id", clientId ).header( "client_secret",
						clientSecret ).header( "Accept", "application/json" )
				.GET().timeout( TIMEOUT ).build();

		HttpResponse<String> response = httpClient.send( request,
				HttpResponse.BodyHandlers.ofString() );

		int status = response.statusCode();
		if( !(status >= 200 && status < 300) ) {
			throw new NotamApiException( status, "NOTAM API returned HTTP "
					+ status + ": " + response.body() );
		}

		return response.body();
	}

	// Validation Section

	private static void validateIcaoCode( String icaoCode )
	{
		if( icaoCode == null || icaoCode.isBlank() ) {
			throw new IllegalArgumentException(
					"ICAO code must not be null or blank" );
		}
		if( !icaoCode.matches( "^[A-Za-z][A-Za-z0-9]{3}$" ) ) {
			throw new IllegalArgumentException( "Invalid ICAO code: '"
					+ icaoCode + "'. Expected a letter followed by"
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
			throw new IllegalArgumentException( "Radius must be between 0 and "
					+ MAX_RADIUS_NM + ", currently: " + radiusNm );
		}
	}

	private static void validatePagination( int pageSize, int pageNum )
	{
		if( pageSize < 1 || pageSize > 1000 ) {
			throw new IllegalArgumentException(
					"pageSize must be between 1 and 1000, currently: "
							+ pageSize );
		}
		if( pageNum < 1 ) {
			throw new IllegalArgumentException(
					"pageNum must be >= 1, currently: " + pageNum );
		}
	}

	private static String requireEnv( Dotenv dotenv, String key )
	{
		String value = dotenv.get( key );
		if( value == null || value.isBlank() ) {
			throw new IllegalArgumentException( key
					+ " is missing from .env file" );
		}
		return value;
	}
}
