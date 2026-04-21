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

public class AirportValidator
{
    private static final String AIRPORT_COORDS_FILENAME = "airportCoords.csv";
    private final Map<String, Point2D> icaoCoordsMap;

    public AirportValidator()
    {
        this( AIRPORT_COORDS_FILENAME );
    }

    public AirportValidator( final String filename )
    {
        try (final InputStream is = AirportValidator.class.getResourceAsStream(
                "/" + filename )) {

            if( is == null ) {
                throw new IllegalArgumentException( "Resource not found: "
                        + filename );
            }

            this.icaoCoordsMap = parseIcaoCoords( is );
        }
        catch( final IOException e ) {
            throw new RuntimeException(
                    "Failed to load airport coordinates from resource: "
                            + filename, e );
        }
    }

    private static Map<String, Point2D> parseIcaoCoords( final InputStream is ) throws IOException
    {
        final Map<String, Point2D> map = new HashMap<>();
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
                map.put( icao, new Point2D.Double( lat, lon ) );
            }
        }
        return map;
    }

    /**
     * Resolves a raw ICAO input string to a standardized 3 or 4-character ICAO
     * code.
     *
     * <p>Handles the ambiguity of 3-character inputs, which may be a US airport
     * code missing its {@code K} country prefix:
     *
     * <ul>
     * <li><b>4 characters</b> — returned as-is; prefix is assumed to be
     * included.</li>
     * <li><b>3 characters</b> — the map is checked for both the bare code and
     * the {@code K}-prefixed form. If both exist, an exception is thrown due
     * to ambiguity. If exactly one exists, that form is returned.</li>
     * <li><b>2 characters</b> — {@code K} is prepended; prefix is assumed
     * missing.</li>
     * <li><b>Any other length</b> — throws
     * {@link AirportNotFoundException}.</li>
     * </ul>
     *
     * @param rawAirportCode
     *     the raw ICAO input to resolve (e.g. {@code "JFK"}, {@code "KJFK"})
     *
     * @return the resolved, normalized ICAO code
     *
     * @throws IllegalArgumentException
     *     if {@code rawAirportCode} is {@code null}
     * @throws AirportNotFoundException
     *     if the code cannot be found, is ambiguous, or has an invalid length
     */
    public String validateIcaoInput( final String rawAirportCode ) throws AirportNotFoundException
    {
        if( rawAirportCode == null ) {
            throw new IllegalArgumentException( "ICAO input cannot be null" );
        }

        final String normalizedCode = rawAirportCode.trim().toUpperCase();

        return switch( normalizedCode.length() ) {
        case 4 -> normalizedCode;
        case 3 -> {
            final boolean exactMatchFound = icaoCoordsMap.containsKey(
                    normalizedCode );
            final boolean prefixedMatchFound = icaoCoordsMap.containsKey( "K"
                    + normalizedCode );
            if( exactMatchFound && prefixedMatchFound ) {
                throw new AirportNotFoundException(
                        "Ambiguous ICAO input. Matches found for: "
                                + normalizedCode + " and K" + normalizedCode );
            }
            else if( exactMatchFound ) {
                yield normalizedCode;
            }
            else if( prefixedMatchFound ) {
                yield "K" + normalizedCode;
            }
            else {
                throw new AirportNotFoundException( "ICAO not found: "
                        + normalizedCode );
            }
        }
        case 2 -> "K" + normalizedCode;
        default -> throw new AirportNotFoundException( "Invalid ICAO length: "
                + normalizedCode );
        };
    }

    /**
     * Returns the geographic coordinates for the given ICAO code.
     *
     * <p>The returned {@link Point2D} follows the project-wide convention:
     * {@code x} = latitude, {@code y} = longitude, both in decimal degrees.
     *
     * @param validatedIcao
     *     the validated ICAO code to look up (e.g. {@code "KJFK"})
     *
     * @return a {@link Point2D.Double} containing the airport's latitude and
     *     longitude
     *
     * @throws AirportNotFoundException
     *     if no coordinates are found for the given code
     */
    public Point2D getCoordsForIcao( final String validatedIcao ) throws AirportNotFoundException
    {
        final Point2D coords = icaoCoordsMap.get( validatedIcao );
        if( coords == null ) {
            throw new AirportNotFoundException( "ICAO coords not found: "
                    + validatedIcao );
        }
        return coords;
    }
}
