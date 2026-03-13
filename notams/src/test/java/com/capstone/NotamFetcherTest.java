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

@ExtendWith(MockitoExtension.class)
class NotamFetcherTest
{
	private static final String CLIENT_ID = "testId";
	private static final String CLIENT_SECRET = "testSecret";
	private static final String BASE_URL = "https://external-api.faa.gov/notamapi/v1/notams";
	private static final Duration TIMEOUT = Duration.ofSeconds( 30 );

	@Mock
	private HttpClient mockHttpClient;

	@Mock
	private HttpResponse<String> mockResponse;

	private NotamFetcher fetcher;

	@BeforeEach
	void setUp()
	{
		fetcher = new NotamFetcher( CLIENT_ID, CLIENT_SECRET, mockHttpClient );
	}

	// fetchByIcao

	@Test
	void testFetchByIcao_validCode() throws IOException, InterruptedException
	{
		when( mockResponse.statusCode() ).thenReturn( 200 );
		when( mockResponse.body() ).thenReturn( "{\"items\":[]}" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockResponse );

		String result = fetcher.fetchByIcao( "kokc", 50, 2 );

		assertEquals( "{\"items\":[]}", result );

		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(
				HttpRequest.class );
		verify( mockHttpClient ).send( captor.capture(),
				any( HttpResponse.BodyHandler.class ) );

		HttpRequest request = captor.getValue();

		assertEquals( URI.create(
				BASE_URL + "?responseFormat=geoJson" + "&icaoLocation=KOKC"
						+ "&pageSize=50" + "&pageNum=2" ), request.uri() );

		assertEquals( "GET", request.method() );
		assertEquals( CLIENT_ID,
				request.headers().firstValue( "client_id" ).orElseThrow() );
		assertEquals( CLIENT_SECRET,
				request.headers().firstValue( "client_secret" ).orElseThrow() );
		assertEquals( "application/json",
				request.headers().firstValue( "Accept" ).orElseThrow() );
		assertEquals( TIMEOUT, request.timeout().orElseThrow() );
	}

	@Test
	void testFetchByIcao_serverError() throws IOException, InterruptedException
	{
		when( mockResponse.statusCode() ).thenReturn( 500 );
		when( mockResponse.body() ).thenReturn( "Internal Server Error" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockResponse );

		NotamApiException ex = assertThrows( NotamApiException.class,
				() -> fetcher.fetchByIcao( "KOKC" ) );

		assertEquals( 500, ex.getStatusCode() );
		assertTrue( ex.getMessage().contains( "Internal Server Error" ) );
	}

	@Test
	void testFetchByIcao_ioException() throws IOException, InterruptedException
	{
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenThrow(
				new IOException( "Connection refused" ) );

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

	// pagination validation

	@Test
	void testFetchByIcao_invalidPagination()
	{
		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByIcao( "KOKC", 0, 1 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByIcao( "KOKC", 1001, 1 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByIcao( "KOKC", 100, 0 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByIcao( "KOKC", 100, -1 ) );
	}

	// pagination boundary values

	@Test
	void testFetchByIcao_paginationBoundaries()
			throws IOException, InterruptedException
	{
		when( mockResponse.statusCode() ).thenReturn( 200 );
		when( mockResponse.body() ).thenReturn( "{\"items\":[]}" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockResponse );

		String resultMin = fetcher.fetchByIcao( "KOKC", 1, 1 );
		assertEquals( "{\"items\":[]}", resultMin );

		String resultMax = fetcher.fetchByIcao( "KOKC", 1000, 1 );
		assertEquals( "{\"items\":[]}", resultMax );
	}

	// fetchByLocation

	@Test
	void testFetchByLocation_validCoords()
			throws IOException, InterruptedException
	{
		when( mockResponse.statusCode() ).thenReturn( 200 );
		when( mockResponse.body() ).thenReturn( "{\"items\":[]}" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockResponse );

		String result = fetcher.fetchByLocation( 35.393, -97.600, 50, 25, 3 );

		assertEquals( "{\"items\":[]}", result );

		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(
				HttpRequest.class );
		verify( mockHttpClient ).send( captor.capture(),
				any( HttpResponse.BodyHandler.class ) );

		String uri = captor.getValue().uri().toString();

		assertTrue( uri.contains( "locationLatitude=35.393" ) );
		assertTrue( uri.contains( "locationLongitude=-97.6" ) );
		assertTrue( uri.contains( "locationRadius=50.0" ) );
		assertTrue( uri.contains( "pageSize=25" ) );
		assertTrue( uri.contains( "pageNum=3" ) );
	}

	@Test
	void testFetchByLocation_unauthorized()
			throws IOException, InterruptedException
	{
		when( mockResponse.statusCode() ).thenReturn( 401 );
		when( mockResponse.body() ).thenReturn( "Unauthorized" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockResponse );

		NotamApiException ex = assertThrows( NotamApiException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 50 ) );

		assertEquals( 401, ex.getStatusCode() );
		assertTrue( ex.getMessage().contains( "Unauthorized" ) );
	}

	@Test
	void testFetchByLocation_interruptedException()
			throws IOException, InterruptedException
	{
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenThrow(
				new InterruptedException( "Interrupted" ) );

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

	// pagination validation

	@Test
	void testFetchByLocation_invalidPagination()
	{
		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 50, 0, 1 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 50, 1001, 1 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 50, 100, 0 ) );

		assertThrows( IllegalArgumentException.class,
				() -> fetcher.fetchByLocation( 35.0, -97.0, 50, 100, -1 ) );
	}

	// boundary coordinate and radius values

	@Test
	void testFetchByLocation_boundaryValues()
			throws IOException, InterruptedException
	{
		when( mockResponse.statusCode() ).thenReturn( 200 );
		when( mockResponse.body() ).thenReturn( "{\"items\":[]}" );
		when( mockHttpClient.send( any( HttpRequest.class ),
				any( HttpResponse.BodyHandler.class ) ) ).thenReturn(
				mockResponse );

		String result1 = fetcher.fetchByLocation( 90.0, 180.0, 100 );
		assertEquals( "{\"items\":[]}", result1 );

		String result2 = fetcher.fetchByLocation( -90.0, -180.0, 0.5 );
		assertEquals( "{\"items\":[]}", result2 );
	}
}