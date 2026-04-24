package com.capstone.services;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.capstone.exceptions.AirportNotFoundException;

public class AirportValidator
{
    private static final String AIRPORT_COORDS_FILENAME = "APT_BASE.csv";
    private static final String HEADER_ICAO = "ICAO_ID";
    private static final String HEADER_ARPT_ID = "ARPT_ID";
    private static final String HEADER_LAT = "LAT_DECIMAL";
    private static final String HEADER_LON = "LONG_DECIMAL";
    private final Map<String, Point2D> airportCodeCoordsMap;

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

            this.airportCodeCoordsMap = parseAirportCodeCoords( is );
        }
        catch( final IOException e ) {
            throw new RuntimeException(
                    "Failed to load airport coordinates from resource: "
                            + filename, e );
        }
    }

    private static Map<String, Point2D> parseAirportCodeCoords( final InputStream is ) throws IOException
    {
        final Map<String, Point2D> map = new HashMap<>();
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader( is, StandardCharsets.UTF_8 ) )) {

            final String headerLine = reader.readLine();
            if( headerLine == null )
                return map;

            final String[] headers = parseCsvLine( headerLine );
            final int icaoIdx = findHeaderIndex( headers, HEADER_ICAO );
            final int arptIdIdx = findHeaderIndex( headers, HEADER_ARPT_ID );
            final int latIdx = findHeaderIndex( headers, HEADER_LAT );
            final int lonIdx = findHeaderIndex( headers, HEADER_LON );

            final int minLength = Math.max( icaoIdx, Math.max( arptIdIdx, Math
                    .max( latIdx, lonIdx ) ) ) + 1;

            String line;
            while( (line = reader.readLine()) != null ) {
                final String[] parts = parseCsvLine( line );
                if( parts.length < minLength )
                    continue;

                String airportCode = null;
                final String icao = parts[icaoIdx].trim();
                final String arptId = parts[arptIdIdx].trim();
                if( icao.isEmpty() && arptId.isEmpty() )
                    continue;

                if( icao.isEmpty() ) {
                    airportCode = arptId;
                }
                else {
                    airportCode = icao;
                }

                try {
                    final double lat = Double.parseDouble( parts[latIdx]
                            .trim() );
                    final double lon = Double.parseDouble( parts[lonIdx]
                            .trim() );
                    map.put( airportCode.toUpperCase(), new Point2D.Double( lat,
                            lon ) );
                }
                catch( NumberFormatException e ) {
                    // skip rows with unparseable coordinates
                }
            }
        }
        return map;
    }

    private static int findHeaderIndex( final String[] headers,
                                        final String target )
    {
        for( int i = 0; i < headers.length; i++ ) {
            if( headers[i].trim().equalsIgnoreCase( target ) )
                return i;
        }
        throw new IllegalArgumentException( "Required CSV column not found: "
                + target );
    }

    private static String[] parseCsvLine( final String line )
    {
        final List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        final StringBuilder sb = new StringBuilder();

        for( int i = 0; i < line.length(); i++ ) {
            final char c = line.charAt( i );
            if( c == '"' ) {
                inQuotes = !inQuotes;
            }
            else if( c == ',' && !inQuotes ) {
                fields.add( sb.toString().trim() );
                sb.setLength( 0 );
            }
            else {
                sb.append( c );
            }
        }
        fields.add( sb.toString().trim() ); // last field
        return fields.toArray( new String[0] );
    }

    /**
     * Resolves a raw airport code input string to a standardized 3 or
     * 4-character airport code.
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
    public String validateAirportCodeInput( final String rawAirportCode ) throws AirportNotFoundException
    {
        if( rawAirportCode == null ) {
            throw new IllegalArgumentException(
                    "Airport code input cannot be null" );
        }

        final String normalizedAirportCode = rawAirportCode.trim()
                .toUpperCase();

        return switch( normalizedAirportCode.length() ) {
        case 4 -> {
            if( airportCodeCoordsMap.containsKey( normalizedAirportCode ) ) {
                yield normalizedAirportCode;
            }
            throw new AirportNotFoundException( "Airport code not found: "
                    + normalizedAirportCode );
        }
        case 3 -> {
            final boolean exactMatchFound = airportCodeCoordsMap.containsKey(
                    normalizedAirportCode );
            final boolean prefixedMatchFound = airportCodeCoordsMap.containsKey(
                    "K" + normalizedAirportCode );
            if( exactMatchFound && prefixedMatchFound ) {
                throw new AirportNotFoundException(
                        "Ambiguous airport code input. Matches found for: "
                                + normalizedAirportCode + " and K"
                                + normalizedAirportCode );
            }
            else if( exactMatchFound ) {
                yield normalizedAirportCode;
            }
            else if( prefixedMatchFound ) {
                yield "K" + normalizedAirportCode;
            }
            else {
                throw new AirportNotFoundException( "Airport code not found: "
                        + normalizedAirportCode );
            }
        }
        case 2 -> {
            final String prefixedCode = "K" + normalizedAirportCode;
            if( airportCodeCoordsMap.containsKey( prefixedCode ) ) {
                yield prefixedCode;
            }
            throw new AirportNotFoundException( "Airport code not found: "
                    + prefixedCode );
        }
        default -> throw new AirportNotFoundException(
                "Invalid airport code length: " + normalizedAirportCode );
        };
    }

    /**
     * Returns the geographic coordinates for the given airport code.
     *
     * <p>The returned {@link Point2D} follows the project-wide convention:
     * {@code x} = latitude, {@code y} = longitude, both in decimal degrees.
     *
     * @param validatedAirportCode
     *     the validated airport code to look up (e.g. {@code "KJFK"})
     *
     * @return a {@link Point2D.Double} containing the airport's latitude and
     *     longitude
     *
     * @throws AirportNotFoundException
     *     if no coordinates are found for the given code
     */
    public Point2D getCoordsForAirportCode( final String validatedAirportCode ) throws AirportNotFoundException
    {
        final Point2D coords = airportCodeCoordsMap.get( validatedAirportCode );
        if( coords == null ) {
            throw new AirportNotFoundException(
                    "Airport code coords not found: " + validatedAirportCode );
        }
        return coords;
    }
}
