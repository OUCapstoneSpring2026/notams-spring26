package com.capstone.services;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import com.capstone.NotamFetcher;
import com.capstone.models.Airport;

/**
 * Service responsible for fetching NOTAMs along a flight route.
 * Ensures the FAA NOTAM API is called for each interpolated point.
 */
public class RouteNotamService {

    // Handles communication with the FAA NOTAM API
    private final NotamFetcher fetcher;

    public RouteNotamService() {
        this.fetcher = new NotamFetcher();
    }

    /**
     * Fetch NOTAM data along the route between two airports.
     *
     * @param departure starting airport
     * @param arrival   destination airport
     * @return list of JSON responses from the FAA API
     */
    public List<String> fetchNotamsAlongRoute(Airport departure, Airport arrival)
            throws IOException, InterruptedException {

        // Store responses from each API call
        final List<String> responses = new ArrayList<>();

        // Generate interpolated points along the flight path
        final List<Point2D> points = FlightPathCalculator.interpolate(
                departure.getCoords(),
                arrival.getCoords(),
                100);

        // Call the API for each point along the route
        for (final Point2D point : points) {
            final double lat = point.getX();
            final double lon = point.getY();

            final String response = fetcher.fetchByLocation(
                    lat,
                    lon,
                    50);

            responses.add(response);
        }

        return responses;
    }
}
