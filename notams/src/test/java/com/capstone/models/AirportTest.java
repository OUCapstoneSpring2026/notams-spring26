package com.capstone.models;

import com.capstone.exceptions.AirportNotFoundException;
import com.capstone.services.AirportValidator;
import java.awt.geom.Point2D;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AirportTest
{

    // getAirportCode()

    @Test
    public void getAirportCode_returnsExactAirportCodePassedIn() throws Exception
    {
        AirportValidator validator = Mockito.mock( AirportValidator.class );
        when( validator.getCoordsForAirportCode( "KJFK" ) ).thenReturn(
                new Point2D.Double( 40.6413, -73.7781 ) );

        Airport airport = new Airport( "KJFK", validator );
        assertEquals( "KJFK", airport.getAirportCode() );
    }

    // getCoords()

    @Test
    public void constructor_fetchesCoordsFromValidator() throws Exception
    {
        AirportValidator validator = Mockito.mock( AirportValidator.class );
        when( validator.getCoordsForAirportCode( "KJFK" ) ).thenReturn(
                new Point2D.Double( 40.6413, -73.7781 ) );

        Airport airport = new Airport( "KJFK", validator );
        assertEquals( 40.6413, airport.getCoords().getX(), 0.0001 );
        assertEquals( -73.7781, airport.getCoords().getY(), 0.0001 );
    }

    @Test
    public void getCoords_returnsPoint2DInstance() throws Exception
    {
        AirportValidator validator = Mockito.mock( AirportValidator.class );
        when( validator.getCoordsForAirportCode( "KLAX" ) ).thenReturn(
                new Point2D.Double( 33.9425, -118.4081 ) );

        Airport airport = new Airport( "KLAX", validator );
        assertTrue( airport.getCoords() instanceof Point2D );
    }

    @Test
    public void constructor_passesAirportCodeToValidator() throws Exception
    {
        AirportValidator validator = Mockito.mock( AirportValidator.class );
        Point2D coords = new Point2D.Double( 40.6413, -73.7781 );
        when( validator.getCoordsForAirportCode( "KJFK" ) ).thenReturn(
                coords );

        Airport airport = new Airport( "KJFK", validator );
        assertNotNull( airport.getCoords() );
        verify( validator ).getCoordsForAirportCode( "KJFK" );
    }

    // AirportNotFoundException

    @Test
    public void constructor_unknownAirportCode_throwsAirportNotFoundException() throws Exception
    {
        AirportValidator validator = Mockito.mock( AirportValidator.class );
        when( validator.getCoordsForAirportCode( "ZZZZ" ) ).thenThrow(
                new AirportNotFoundException(
                        "Airport code not found: ZZZZ" ) );

        assertThrows( AirportNotFoundException.class, () -> new Airport( "ZZZZ",
                validator ) );
    }
}
