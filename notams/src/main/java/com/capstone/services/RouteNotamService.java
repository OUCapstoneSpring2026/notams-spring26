package com.capstone.services;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.capstone.NotamFetcher;
import com.capstone.models.Airport;

/**
 * RouteNotamService
 *
 * This service is responsible for ensuring the FAA NOTAM API
 * is called for every point along a generated flight path.
 *
 * CAP-31 requirement:
 * "Ensure we call API for each point along path"
 *
 * The class combines:
 *  - FlightPathCalculator (generates route points)
 *  - NotamFetcher (calls FAA NOTAM API)
 *
 * The result is a list of raw JSON responses from the API,
 * one response per route point.
 */
public class RouteNotamService {

    // Responsible for communicating with the FAA NOTAM API
    private final NotamFetcher fetcher;

    /**
     * Constructor initializes the NOTAM API fetcher.
     * The fetcher already handles authentication and HTTP calls.
     */
    public RouteNotamService() {
        this.fetcher = new NotamFetcher();
    }

    /**
     * Fetch NOTAM data along the flight path between two airports.
     *
     * Steps performed:
     * 1. Generate points along the great-circle flight path.
     * 2. Loop through each generated coordinate.
     * 3. Call the FAA NOTAM API for that location.
     * 4. Store the API response.
     *
     * @param departure departure airport
     * @param arrival arrival airport
     *
     * @return list of JSON responses from the FAA API
     */
    public List<String> fetchNotamsAlongRoute(Airport departure, Airport arrival)
            throws Exception {

        // Store responses from each API call
        List<String> responses = new ArrayList<>();

        // Generate points along the flight route
        // The interval is approximately 100 nautical miles between points
        List<Point2D> points = FlightPathCalculator.interpolate(
                departure.getCoords(),
                arrival.getCoords(),
                100
        );

        // Iterate through each generated point on the route
        for (Point2D point : points) {

            // Extract latitude and longitude
            double lat = point.getX();
            double lon = point.getY();

            // Call the FAA NOTAM API for this location
            // Radius is 50 nautical miles from the route point
            String response = fetcher.fetchByLocation(
                    lat,
                    lon,
                    50
            );

            // Save the response so it can be processed later
            responses.add(response);
        }

        // Return all collected NOTAM responses
        return responses;
    }
}