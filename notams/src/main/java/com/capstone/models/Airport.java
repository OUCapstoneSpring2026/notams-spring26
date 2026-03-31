package com.capstone.models;

import java.awt.geom.Point2D;
import java.io.IOException;
import com.capstone.exceptions.AirportNotFoundException;
import com.capstone.services.IcaoParser;

public class Airport
{
    private final String icao;
    private final Point2D coords;

    public Airport( final String icao ) throws IOException,
                                        AirportNotFoundException
    {
        this.icao = icao;
        this.coords = IcaoParser.getCoordsForIcao( icao );
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
