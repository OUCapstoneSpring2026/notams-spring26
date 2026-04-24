package com.capstone.models;

import java.awt.geom.Point2D;
import com.capstone.exceptions.AirportNotFoundException;
import com.capstone.services.AirportValidator;

public class Airport
{
    private final String airportCode;
    private final Point2D coords;

    public Airport( final String airportCode,
                    final AirportValidator airportValidator ) throws AirportNotFoundException
    {
        this.airportCode = airportCode;
        this.coords = airportValidator.getCoordsForAirportCode( airportCode );
    }

    public String getAirportCode()
    {
        return airportCode;
    }

    public Point2D getCoords()
    {
        return coords;
    }
}
