package com.capstone;

import com.capstone.exceptions.NotamApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class NmsNotamFetcherTest
{
	private static final String CLIENT_ID = "testId";
	private static final String CLIENT_SECRET = "testSecret";
	private static final String AUTH_URL = "https://api-staging.cgifederal-aim.com/v1/auth/token";
	private static final String BASE_URL = "https://api-staging.cgifederal-aim.com/nmsapi/v1/notams";
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds( 30 );
	private static final String TOKEN_RESPONSE = "{\"access_token\":\"test-token\",\"expires_in\":\"1799\"}";

	@Mock
	private HttpClient mockHttpClient;

	@Mock
	private HttpResponse<String> mockResponse;

	private NmsNotamFetcher fetcher;

	@BeforeEach
	void setUp()
	{
		fetcher = new NmsNotamFetcher( CLIENT_ID, CLIENT_SECRET, AUTH_URL,
				BASE_URL, mockHttpClient );
	}

	// fetchByIcao

	@Test
	void testFetchByIcao_validCode() throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 200 );
		when( mockTokenResponse.body() ).thenReturn( TOKEN_RESPONSE );

		when( mockResponse.statusCode() ).thenReturn( 200 );
		when( mockResponse.body() ).thenReturn( "{\"items\":[]}" );

		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockTokenResponse ).thenReturn( mockResponse );

		String result = fetcher.fetchByIcao( "kokc" );

		assertEquals( "{\"items\":[]}", result );

		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(
				HttpRequest.class );
		verify( mockHttpClient, org.mockito.Mockito.times( 2 ) ).send(
				captor.capture(), any( HttpResponse.BodyHandler.class ) );

		java.util.List<HttpRequest> requests = captor.getAllValues();
		HttpRequest tokenRequest = requests.get( 0 );
		HttpRequest dataRequest = requests.get( 1 );

		assertEquals( URI.create( AUTH_URL ), tokenRequest.uri() );
		assertEquals( "POST", tokenRequest.method() );
		assertEquals( "application/x-www-form-urlencoded",
				tokenRequest.headers().firstValue( "Content-Type" )
						.orElseThrow() );
		assertEquals( "application/json",
				tokenRequest.headers().firstValue( "Accept" ).orElseThrow() );
		assertEquals( REQUEST_TIMEOUT, tokenRequest.timeout().orElseThrow() );

		assertEquals( URI.create( BASE_URL + "?location=KOKC" ),
				dataRequest.uri() );
		assertEquals( "GET", dataRequest.method() );
		assertEquals( "Bearer test-token",
				dataRequest.headers().firstValue( "Authorization" )
						.orElseThrow() );
		assertEquals( "application/json",
				dataRequest.headers().firstValue( "Accept" ).orElseThrow() );
		assertEquals( "GEOJSON",
				dataRequest.headers().firstValue( "nmsResponseFormat" )
						.orElseThrow() );
		assertEquals( REQUEST_TIMEOUT, dataRequest.timeout().orElseThrow() );
	}

	@Test
	void testFetchByIcao_serverError() throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 200 );
		when( mockTokenResponse.body() ).thenReturn( TOKEN_RESPONSE );

		when( mockResponse.statusCode() ).thenReturn( 500 );
		when( mockResponse.body() ).thenReturn( "Internal Server Error" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockTokenResponse ).thenReturn( mockResponse );

		NotamApiException ex = assertThrows( NotamApiException.class,
				() -> fetcher.fetchByIcao( "KOKC" ) );

		assertEquals( 500, ex.getStatusCode() );
		assertTrue( ex.getMessage().contains( "Internal Server Error" ) );
	}

	@Test
	void testFetchByIcao_tokenRequestFailure()
			throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 401 );
		when( mockTokenResponse.body() ).thenReturn( "Unauthorized" );

		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockTokenResponse );

		NotamApiException ex = assertThrows( NotamApiException.class,
				() -> fetcher.fetchByIcao( "KOKC" ) );

		assertEquals( 401, ex.getStatusCode() );
		assertTrue(
				ex.getMessage().contains( "Token request returned HTTP 401" ) );
	}

	@Test
	void testFetchByIcao_ioException() throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 200 );
		when( mockTokenResponse.body() ).thenReturn( TOKEN_RESPONSE );

		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
						mockTokenResponse )
				.thenThrow( new IOException( "Connection refused" ) );

		assertThrows( IOException.class, () -> fetcher.fetchByIcao( "KOKC" ) );
	}

	// ICAO validation

	@Test
	void testFetchByIcao_nullCode()
	{
		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByIcao( null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "   ", "KOK", "KOKCC", "1OKC", "KO@C" })
	void testFetchByIcao_invalidCodes( String code )
	{
		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByIcao( code ) );
	}

	// fetchByLocation

	@Test
	void testFetchByLocation_validCoords()
			throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 200 );
		when( mockTokenResponse.body() ).thenReturn( TOKEN_RESPONSE );

		when( mockResponse.statusCode() ).thenReturn( 200 );
		when( mockResponse.body() ).thenReturn( "{\"items\":[]}" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockTokenResponse ).thenReturn( mockResponse );

		String result = fetcher.fetchByLocation( 35.393, -97.600, 50 );

		assertEquals( "{\"items\":[]}", result );

		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(
				HttpRequest.class );
		verify( mockHttpClient, org.mockito.Mockito.times( 2 ) ).send(
				captor.capture(), any( HttpResponse.BodyHandler.class ) );

		HttpRequest dataRequest = captor.getAllValues().get( 1 );
		String uri = dataRequest.uri().toString();

		assertTrue( uri.contains( "latitude=35.393" ) );
		assertTrue( uri.contains( "longitude=-97.6" ) );
		assertTrue( uri.contains( "radius=50.0" ) );
	}

	@Test
	void testFetchByLocation_unauthorized()
			throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 200 );
		when( mockTokenResponse.body() ).thenReturn( TOKEN_RESPONSE );

		when( mockResponse.statusCode() ).thenReturn( 401 );
		when( mockResponse.body() ).thenReturn( "Unauthorized" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockTokenResponse ).thenReturn( mockResponse );

		NotamApiException ex = assertThrows( NotamApiException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 50 ) );

		assertEquals( 401, ex.getStatusCode() );
		assertTrue( ex.getMessage().contains( "Unauthorized" ) );
	}

	@Test
	void testFetchByLocation_interruptedException()
			throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 200 );
		when( mockTokenResponse.body() ).thenReturn( TOKEN_RESPONSE );

		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
						mockTokenResponse )
				.thenThrow( new InterruptedException( "Interrupted" ) );

		assertThrows( InterruptedException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 50 ) );
	}

	// coordinate validation

	@Test
	void testFetchByLocation_invalidCoordinates()
	{
		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 91.0, -97.0, 50 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( -91.0, -97.0, 50 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, 181.0, 50 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, -181.0, 50 ) );
	}

	// radius validation

	@Test
	void testFetchByLocation_invalidRadius()
	{
		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 0 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, -10 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 101 ) );
	}

	// boundary coordinate and radius values

	@Test
	void testFetchByLocation_boundaryValues()
			throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 200 );
		when( mockTokenResponse.body() ).thenReturn( TOKEN_RESPONSE );

		when( mockResponse.statusCode() ).thenReturn( 200 );
		when( mockResponse.body() ).thenReturn( "{\"items\":[]}" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
						mockTokenResponse ).thenReturn( mockResponse )
				.thenReturn( mockResponse );

		String result1 = fetcher.fetchByLocation( 90.0, 180.0, 100 );
		assertEquals( "{\"items\":[]}", result1 );

		String result2 = fetcher.fetchByLocation( -90.0, -180.0, 0.5 );
		assertEquals( "{\"items\":[]}", result2 );
	}

	@Test
	void testFetchByIcao_reusesValidBearerToken()
			throws IOException, InterruptedException
	{
		HttpResponse<String> mockTokenResponse = mock( HttpResponse.class );
		when( mockTokenResponse.statusCode() ).thenReturn( 200 );
		when( mockTokenResponse.body() ).thenReturn( TOKEN_RESPONSE );

		when( mockResponse.statusCode() ).thenReturn( 200 );
		when( mockResponse.body() ).thenReturn( "{\"items\":[]}" );

		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
						mockTokenResponse ).thenReturn( mockResponse )
				.thenReturn( mockResponse );

		String firstResult = fetcher.fetchByIcao( "KOKC" );
		String secondResult = fetcher.fetchByIcao( "KOKC" );

		assertEquals( "{\"items\":[]}", firstResult );
		assertEquals( "{\"items\":[]}", secondResult );

		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(
				HttpRequest.class );
		verify( mockHttpClient, org.mockito.Mockito.times( 3 ) ).send(
				captor.capture(), any( HttpResponse.BodyHandler.class ) );

		java.util.List<HttpRequest> requests = captor.getAllValues();
		assertEquals( URI.create( AUTH_URL ), requests.get( 0 ).uri() );
		assertEquals( URI.create( BASE_URL + "?location=KOKC" ),
				requests.get( 1 ).uri() );
		assertEquals( URI.create( BASE_URL + "?location=KOKC" ),
				requests.get( 2 ).uri() );
	}
}
