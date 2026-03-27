package com.capstone.models;

import com.capstone.exceptions.AirportNotFoundException;
import java.awt.geom.Point2D;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AirportTest
{

    // getIcao()

    @Test
    public void getIcao_returnsExactIcaoPassedIn() throws Exception
    {
        Airport airport = new Airport( "KJFK" );
        assertEquals( "KJFK", airport.getIcao() );
    }

    // getCoords()

    @Test
    public void getCoords_knownAirport_returnsCorrectLatitude() throws Exception
    {
        Airport airport = new Airport( "KJFK" );
        assertEquals( 40.6413, airport.getCoords().getX(), 0.0001 );
    }

    @Test
    public void getCoords_knownAirport_returnsCorrectLongitude() throws Exception
    {
        Airport airport = new Airport( "KJFK" );
        assertEquals( -73.7781, airport.getCoords().getY(), 0.0001 );
    }

    @Test
    public void getCoords_returnsPoint2DInstance() throws Exception
    {
        Airport airport = new Airport( "KLAX" );
        assertTrue( airport.getCoords() instanceof Point2D );
    }

    // Case insensitivity

    @Test
    public void constructor_lowercaseIcao_stillFindsAirport() throws Exception
    {
        Airport airport = new Airport( "kjfk" );
        assertEquals( 40.6413, airport.getCoords().getX(), 0.0001 );
    }

    @Test
    public void constructor_mixedCaseIcao_stillFindsAirport() throws Exception
    {
        Airport airport = new Airport( "kJfK" );
        assertNotNull( airport.getCoords() );
    }

    // Multiple airports

    @Test
    public void constructor_differentAirports_returnDifferentCoords() throws Exception
    {
        Airport jfk = new Airport( "KJFK" );
        Airport lax = new Airport( "KLAX" );
        assertNotEquals( jfk.getCoords(), lax.getCoords() );
    }

    // AirportNotFoundException

    @Test
    public void constructor_unknownIcao_throwsAirportNotFoundException() throws Exception
    {
        assertThrows( AirportNotFoundException.class, () -> new Airport( "ZZZZ" ) );
    }

    @Test
    public void constructor_emptyString_throwsAirportNotFoundException() throws Exception
    {
        assertThrows( AirportNotFoundException.class, () -> new Airport( "" ) );
    }

    @Test
    public void constructor_nullIcao_throwsException() throws Exception
    {
        assertThrows( Exception.class, () -> new Airport( null ) );
    }

    // Incorrectly formatted CSV row

    @Test
    public void constructor_icaoWithMalformedCoords_throwsNumberFormatException() throws Exception
    {
        assertThrows( NumberFormatException.class, () -> new Airport( "KBAD" ) );
    }
}
