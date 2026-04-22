package com.capstone.services;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;

import com.capstone.NotamFetcher;
import com.capstone.models.FlightPath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RouteNotamServiceTest
{
	@Test
	public void fetchNotamsAlongRoute_multipleWaypoints_combinesIntoSingleJsonString()
			throws IOException, InterruptedException
	{
		NotamFetcher fetcher = mock( NotamFetcher.class );
		FlightPath flightPath = mock( FlightPath.class );

		List<Point2D> waypoints = List.of( new Point2D.Double( 35.0, -97.0 ),
				new Point2D.Double( 36.0, -98.0 ) );

		when( flightPath.getWaypoints() ).thenReturn( waypoints );
		when( fetcher.fetchByLocation( 35.0, -97.0, 50 ) ).thenReturn(
				"{\"data\":{\"geojson\":[{\"id\":1}]}}" );
		when( fetcher.fetchByLocation( 36.0, -98.0, 50 ) ).thenReturn(
				"{\"data\":{\"geojson\":[{\"id\":2}]}}" );

		RouteNotamService service = new RouteNotamService( fetcher );
		String result = service.fetchNotamsAlongRoute( flightPath );

		assertEquals( "{\"data\":{\"geojson\":[{\"id\":1},{\"id\":2}]}}",
				result );
		verify( fetcher ).fetchByLocation( 35.0, -97.0, 50 );
		verify( fetcher ).fetchByLocation( 36.0, -98.0, 50 );
	}

	@Test
	public void fetchNotamsAlongRoute_noWaypoints_returnsEmptyGeojsonArray()
			throws IOException, InterruptedException
	{
		NotamFetcher fetcher = mock( NotamFetcher.class );
		FlightPath flightPath = mock( FlightPath.class );
		when( flightPath.getWaypoints() ).thenReturn( List.of() );

		RouteNotamService service = new RouteNotamService( fetcher );
		String result = service.fetchNotamsAlongRoute( flightPath );

		assertEquals( "{\"data\":{\"geojson\":[]}}", result );
	}

	@Test
	public void fetchNotamsAlongRoute_fetcherThrowsIOException_propagatesException()
			throws IOException, InterruptedException
	{
		NotamFetcher fetcher = mock( NotamFetcher.class );
		FlightPath flightPath = mock( FlightPath.class );
		when( flightPath.getWaypoints() ).thenReturn(
				List.of( new Point2D.Double( 35.0, -97.0 ) ) );
		when( fetcher.fetchByLocation( 35.0, -97.0, 50 ) ).thenThrow(
				new IOException( "network failure" ) );

		RouteNotamService service = new RouteNotamService( fetcher );

		assertThrows( IOException.class,
				() -> service.fetchNotamsAlongRoute( flightPath ) );
	}
}
