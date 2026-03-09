package com.capstone.models;

import java.awt.geom.Point2D;
import java.util.List;
import com.capstone.services.FlightPathCalculator;

public class FlightPath
{
    private final List<Point2D> waypoints;

    public FlightPath( Airport departure, Airport arrival )
    {
        this.waypoints = FlightPathCalculator.interpolate( departure
                .getCoords(), arrival.getCoords(), 50.0 );
    }

    public List<Point2D> getWaypoints()
    {
        return waypoints;
    }
}