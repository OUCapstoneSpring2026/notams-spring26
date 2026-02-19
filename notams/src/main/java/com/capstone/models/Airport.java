package com.capstone.models;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.capstone.exceptions.AirportNotFoundException;

public class Airport
{
    private final String icao;
    private final Point2D coords;
    private static final String AIRPORT_COORDS_FILENAME = "airportCoords.csv";

    public Airport( String icao ) throws IOException, AirportNotFoundException
    {
        this.icao = icao;
        this.coords = loadAirportCoordsFromCsv( icao );
    }

    public String getIcao()
    {
        return icao;
    }

    public Point2D getCoords()
    {
        return coords;
    }

    private Point2D loadAirportCoordsFromCsv( String icao ) throws IOException,
                                                            AirportNotFoundException
    {
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                AIRPORT_COORDS_FILENAME );
        if( is == null )
            throw new FileNotFoundException( "File not found: "
                    + AIRPORT_COORDS_FILENAME );

        try (final BufferedReader br = new BufferedReader(
                new InputStreamReader( is ) )) {
            String line;
            while( (line = br.readLine()) != null ) {

                // TODO: parse coords from CSV
                if( true ) {
                    return new Point2D.Double( 0, 0 );
                }
            }
        }

        throw new AirportNotFoundException( "ICAO coords not found: " + icao );
    }
}
