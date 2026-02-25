package com.capstone;

import com.capstone.models.Airport;

public class App
{
    public static void main( String[] args )
    {
        try {
            final Airport departure = new Airport( "KOKC" );
            final Airport arrival = new Airport( "KDFW" );

            System.out.println( "Departure coords: " + departure.getCoords() );
            System.out.println( "Arrival coords: " + arrival.getCoords() );
        }
        catch( final Exception e ) {
            System.out.println( e.getMessage() );
        }
    }
}
