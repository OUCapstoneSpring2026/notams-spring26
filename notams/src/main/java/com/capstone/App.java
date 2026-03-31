package com.capstone;

import com.capstone.models.Airport;
import com.capstone.models.FlightPath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Scanner;

public class App
{
    public static void main( String[] args )
    {
        String departureIcao = null;
        String arrivalIcao = null;

        // Check if command line arguments were provided
        if( args.length > 0 ) {
            departureIcao = parseArg( args, "--departure" );
            arrivalIcao = parseArg( args, "--arrival" );

            if( departureIcao == null || arrivalIcao == null ) {
                System.err.println(
                        "[ERROR] Usage: java -cp target/classes com.capstone.App --departure <ICAO> --arrival <ICAO>" );
                System.exit( 1 );
            }
        }
        // If command line arguments are not provided, prompt the user for input
        else {
            final Scanner scanner = new Scanner( System.in );

            System.out.print( "Enter departure airport ICAO: " );
            departureIcao = scanner.nextLine().trim().toUpperCase();

            System.out.print( "Enter arrival airport ICAO: " );
            arrivalIcao = scanner.nextLine().trim().toUpperCase();

            scanner.close();
        }

        try {
            final Airport departureAirport = new Airport( departureIcao );
            final Airport arrivalAirport = new Airport( arrivalIcao );

            final FlightPath flightPath = new FlightPath( departureAirport,
                    arrivalAirport );

            NotamFetcher fetcher = new NotamFetcher();
            String json = fetcher.fetchByIcao( "KOKC", 1000, 1 );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree( json );

            int notamCount = rootNode.get( "totalCount" ).asInt();

            System.out.println( "Fetched " + notamCount
                    + " NOTAMs successfully." );
            System.out.println( json );

        }
        catch( final Exception e ) {
            System.err.println( "[ERROR] " + e.getMessage() );
        }
    }

    private static String parseArg( String[] args, String flag )
    {
        // Loop through all args so they can be provided in any order
        for( int i = 0; i < args.length - 1; i++ ) {
            if( flag.equals( args[i] ) ) {
                return args[i + 1].trim().toUpperCase();
            }
        }
        return null;
    }
}
