package com.capstone.services;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import com.capstone.fetching.NmsNotamFetcher;
import com.capstone.fetching.NotamFetcherInterface;
import com.capstone.fetching.NotamFetcher;
import com.capstone.models.FlightPath;
import com.capstone.models.Notam;
import com.capstone.parsing.NmsNotamParser;
import com.capstone.parsing.NotamParserInterface;

/**
 * Service responsible for fetching NOTAMs along a flight route. Ensures the FAA
 * NOTAM API is called for each interpolated point.
 */
public class RouteNotamService
{
	private final NotamFetcherInterface fetcher;

	public RouteNotamService()
	{
		this( new NmsNotamFetcher() );
	}

	public RouteNotamService( final NotamFetcherInterface fetcher )
	{
		this.fetcher = fetcher;
	}

	RouteNotamService( NotamFetcher fetcher )
	{
		this.fetcher = fetcher;
	}

	/**
	 * Fetch NOTAM data along the provided flight path.
	 *
	 * @param flightPath
	 *     flight path to query for NOTAMs
	 *
	 * @return list of all NOTAMs for each point along the flight path
	 */
	public List<Notam> fetchNotamsAlongRoute( FlightPath flightPath )   throws IOException,
																		InterruptedException
	{
		final List<Point2D> points = flightPath.getWaypoints();
		final NotamParserInterface parser = new NmsNotamParser();
		List<Notam> flightPathNotams = new ArrayList<>();

		for( final Point2D point : points ) {
			final double lat = point.getX();
			final double lon = point.getY();

			final String response = fetcher.fetchByCoordinates( lat, lon, 50 );
			flightPathNotams.addAll( parser.parseNotams( response ) );
		}

		return flightPathNotams;
	}
}
