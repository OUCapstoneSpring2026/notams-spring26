package com.capstone.services;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.capstone.exceptions.AirportNotFoundException;

public class IcaoParser
{
    private static final String AIRPORT_COORDS_FILENAME = "airportCoords.csv";
    private static final Map<String, Point2D> ICAO_COORDS_MAP = loadIcaoCoords();

    private static Map<String, Point2D> loadIcaoCoords()
    {
        try (final InputStream is = IcaoParser.class.getResourceAsStream( "/"
                + AIRPORT_COORDS_FILENAME )) {
            return IcaoParser.parseIcaoCoords( is );
        }
        catch( IOException e ) {
            throw new ExceptionInInitializerError( e );
        }
    }

    private static Map<String, Point2D> parseIcaoCoords( final InputStream is ) throws IOException
    {
        final Map<String, Point2D> icaoCoordsMap = new HashMap<>();
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader( is, StandardCharsets.UTF_8 ) )) {
            String line;
            while( (line = reader.readLine()) != null ) {
                final String[] parts = line.trim().split( "," );
                if( parts.length != 3 )
                    continue;
                final String icao = parts[0].trim().toUpperCase();
                final double lat = Double.parseDouble( parts[1].trim() );
                final double lon = Double.parseDouble( parts[2].trim() );
                icaoCoordsMap.put( icao, new Point2D.Double( lat, lon ) );
            }
        }
        return icaoCoordsMap;
    }

    public static boolean icaoExists( final String icao )
    {
        return ICAO_COORDS_MAP.containsKey( icao );
    }

    public static String parseIcaoInput( final String icao ) throws AirportNotFoundException
    {
        // ICAO needs US country designation "K" prefix for coord lookup
        return switch( icao.length() ) {
        case 4 -> icao; // Assume prefix included
        case 3 -> { // Unknown if prefix included
            final boolean exactMatchFound = icaoExists( icao );
            final boolean prefixedMatchFound = icaoExists( "K" + icao );
            if( exactMatchFound && prefixedMatchFound ) {
                throw new AirportNotFoundException(
                        "Ambiguous ICAO input. Matches found for: " + icao
                                + " and K" + icao );
            }
            else if( exactMatchFound ) {
                yield icao;
            }
            else if( prefixedMatchFound ) {
                yield "K" + icao;
            }
            else {
                throw new AirportNotFoundException( "ICAO not found: " + icao );
            }
        }
        case 2 -> "K" + icao;   // Assume prefix missing
        default -> throw new AirportNotFoundException( "Invalid ICAO length: "
                + icao );
        };
    }

    public static Point2D getCoordsForIcao( final String icao ) throws AirportNotFoundException
    {
        final Point2D coords = ICAO_COORDS_MAP.get( icao );
        if( coords == null ) {
            throw new AirportNotFoundException( "ICAO coords not found: "
                    + icao );
        }
        return coords;
    }
}
