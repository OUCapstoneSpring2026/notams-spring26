package com.capstone.services;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;

import com.capstone.models.Notam;
import com.capstone.fetching.NotamFetcherInterface;
import com.capstone.models.FlightPath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RouteNotamServiceTest
{
	@Test
	public void fetchNotamsAlongRoute_multipleWaypoints_returnsCombinedParsedNotams()   throws IOException,
																						InterruptedException
	{
		NotamFetcherInterface fetcher = mock( NotamFetcherInterface.class );
		FlightPath flightPath = mock( FlightPath.class );

		List<Point2D> waypoints = List.of( new Point2D.Double( 45.0, -90.0 ),
				new Point2D.Double( 46.0, -91.0 ) );

		when( flightPath.getWaypoints() ).thenReturn( waypoints );
		when( fetcher.fetchByCoordinates( 45.0, -90.0, 50 ) ).thenReturn(
				buildResponse( "id-1", "A0001/26" ) );
		when( fetcher.fetchByCoordinates( 46.0, -91.0, 50 ) ).thenReturn(
				buildResponse( "id-2", "A0002/26" ) );

		RouteNotamService service = new RouteNotamService( fetcher );
		List<Notam> result = service.fetchNotamsAlongRoute( flightPath );

		assertEquals( 2, result.size() );
		assertEquals( "id-1", result.get( 0 ).getId() );
		assertEquals( "id-2", result.get( 1 ).getId() );
		verify( fetcher ).fetchByCoordinates( 45.0, -90.0, 50 );
		verify( fetcher ).fetchByCoordinates( 46.0, -91.0, 50 );
	}

	@Test
	public void fetchNotamsAlongRoute_noWaypoints_returnsEmptyNotamList()   throws IOException,
																			InterruptedException
	{
		NotamFetcherInterface fetcher = mock( NotamFetcherInterface.class );
		FlightPath flightPath = mock( FlightPath.class );
		when( flightPath.getWaypoints() ).thenReturn( List.of() );

		RouteNotamService service = new RouteNotamService( fetcher );
		List<Notam> result = service.fetchNotamsAlongRoute( flightPath );

		assertEquals( 0, result.size() );
		verifyNoInteractions( fetcher );
	}

	@Test
	public void fetchNotamsAlongRoute_fetcherThrowsIOException_propagatesException()    throws IOException,
																						InterruptedException
	{
		NotamFetcherInterface fetcher = mock( NotamFetcherInterface.class );
		FlightPath flightPath = mock( FlightPath.class );
		when( flightPath.getWaypoints() ).thenReturn( List.of(
				new Point2D.Double( 45.0, -90.0 ) ) );
		when( fetcher.fetchByCoordinates( 45.0, -90.0, 50 ) ).thenThrow(
				new IOException( "network failure" ) );

		RouteNotamService service = new RouteNotamService( fetcher );

		assertThrows( IOException.class, () -> service.fetchNotamsAlongRoute(
				flightPath ) );
	}

	private static String buildResponse( String id, String number )
	{
		return "{" + "\"data\":{" + "\"geojson\":[" + "{" + "\"properties\":{"
				+ "\"coreNOTAMData\":{" + "\"notam\":{" + "\"id\":\"" + id
				+ "\"," + "\"number\":\"" + number + "\"," + "\"type\":\"N\","
				+ "\"issued\":\"2026-02-01T00:00:00Z\","
				+ "\"effectiveStart\":\"2026-02-01T01:00:00Z\","
				+ "\"effectiveEnd\":\"2026-02-02T01:00:00Z\","
				+ "\"text\":\"RWY CLSD\"" + "}," + "\"notamTranslation\":[]"
				+ "}" + "}" + "}" + "]" + "}" + "}";
	}
}
