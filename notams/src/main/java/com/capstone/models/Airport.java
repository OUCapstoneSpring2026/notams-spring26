package com.capstone.models;

import java.awt.geom.Point2D;
import com.capstone.exceptions.AirportNotFoundException;
import com.capstone.services.AirportValidator;

public class Airport
{
    private final String icao;
    private final Point2D coords;

    public Airport( final String icao, final AirportValidator airportValidator )
                                                                                 throws AirportNotFoundException
    {
        this.icao = icao;
        this.coords = airportValidator.getCoordsForIcao( icao );
    }

    public String getIcao()
    {
        return icao;
    }

    public Point2D getCoords()
    {
        return coords;
    }
}
