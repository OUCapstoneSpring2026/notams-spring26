package com.capstone.models;

import java.awt.geom.Point2D;
import java.util.List;
import com.capstone.services.FlightPathCalculator;

public class FlightPath
{
    private final Airport departure;
    private final Airport arrival;
    private final List<Point2D> waypoints;

    public FlightPath( final Airport departure, final Airport arrival )
    {
        this.departure = departure;
        this.arrival = arrival;
        this.waypoints = FlightPathCalculator.interpolate( departure
                .getCoords(), arrival.getCoords() );
    }

    // Overload constructor to allow for specific interval
    public FlightPath( final Airport departure,
                       final Airport arrival,
                       final double intervalNm )
    {
        this.departure = departure;
        this.arrival = arrival;
        this.waypoints = FlightPathCalculator.interpolate( departure
                .getCoords(), arrival.getCoords(), intervalNm );
    }

    public Airport getDeparture()
    {
        return departure;
    }

    public Airport getArrival()
    {
        return arrival;
    }

    public List<Point2D> getWaypoints()
    {
        return waypoints;
    }
}