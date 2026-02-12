package com.capstone;

import com.capstone.models.Airport;

public class App
{
    public static void main( String[] args )
    {
        try {
            final Airport departure = new Airport( "KOKC" );
            final Airport arrival = new Airport( "KDFW" );
        }
        catch( final Exception e ) {
            System.out.println( e.getMessage() );
        }
    }
}
