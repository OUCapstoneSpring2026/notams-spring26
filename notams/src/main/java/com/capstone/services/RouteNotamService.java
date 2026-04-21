package com.capstone.services;

import java.awt.geom.Point2D;
import java.util.List;
import java.io.IOException;

import com.capstone.NmsNotamFetcher;
import com.capstone.NotamDataFetcher;
import com.capstone.NotamFetcher;
import com.capstone.models.FlightPath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Service responsible for fetching NOTAMs along a flight route. Ensures the FAA
 * NOTAM API is called for each interpolated point.
 */
public class RouteNotamService
{
	private static final ObjectMapper mapper = new ObjectMapper();

	// Handles communication with the FAA NOTAM API
	private final NotamDataFetcher fetcher;

	public RouteNotamService()
	{
		this( new NmsNotamFetcher() );
	}

	public RouteNotamService( final NotamDataFetcher fetcher )
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
	 * @return single JSON String containing all FAA API responses
	 */
	public String fetchNotamsAlongRoute( FlightPath flightPath )    throws IOException,
																	InterruptedException
	{
		final List<Point2D> points = flightPath.getWaypoints();
		final ArrayNode mergedGeojson = mapper.createArrayNode();

		for( final Point2D point : points ) {
			final double lat = point.getX();
			final double lon = point.getY();

			final String response = fetcher.fetchByLocation( lat, lon, 50 );
			final JsonNode responseRoot = mapper.readTree( response );
			final JsonNode geojsonNode = responseRoot.path( "data" ).path(
					"geojson" );
			if( geojsonNode.isArray() ) {
				mergedGeojson.addAll( (ArrayNode) geojsonNode );
			}
		}

		final ObjectNode dataNode = mapper.createObjectNode();
		dataNode.set( "geojson", mergedGeojson );

		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.set( "data", dataNode );

		return mapper.writeValueAsString( rootNode );
	}
}
