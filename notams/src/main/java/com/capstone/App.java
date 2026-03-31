package com.capstone;

import com.capstone.exceptions.AirportNotFoundException;
import com.capstone.models.Airport;
import com.capstone.models.FlightPath;
import com.capstone.services.IcaoParser;

import java.util.Scanner;

public class App
{

    public static void main( String[] args )
    {
        String departureIcao = null;
        String arrivalIcao = null;

        if( args.length > 0 ) {
            String rawDeparture = parseArg( args, "--departure" );
            String rawArrival = parseArg( args, "--arrival" );

            if( rawDeparture == null || rawArrival == null ) {
                System.err.println(
                        "[ERROR] Usage: ... --departure <ICAO> --arrival <ICAO>" );
                System.exit( 1 );
            }

            try {
                departureIcao = IcaoParser.parseIcaoInput( rawDeparture );
                arrivalIcao = IcaoParser.parseIcaoInput( rawArrival );
            }
            catch( AirportNotFoundException e ) {
                System.err.println( "[ERROR] " + e.getMessage() );
                System.exit( 1 );
            }
        }
        else {
            String[] icaos = promptAndConfirmIcaos();
            departureIcao = icaos[0];
            arrivalIcao = icaos[1];
        }

        try {
            Airport departure = new Airport( departureIcao );
            Airport arrival = new Airport( arrivalIcao );

            FlightPath flightPath = new FlightPath( departure, arrival );

            NotamFetcher fetcher = new NotamFetcher();
            String json = fetcher.fetchByIcao( "KOKC", 1000, 1 );
            System.out.println( json );

        }
        catch( Exception e ) {
            System.err.println( "[ERROR] " + e.getMessage() );
        }
    }

    private static String[] promptAndConfirmIcaos()
    {
        try (Scanner scanner = new Scanner( System.in )) {
            while( true ) {
                String[] resolved = resolveIcaoPair( scanner );
                if( confirmIcaos( scanner, resolved[0], resolved[1] ) ) {
                    return resolved;
                }
            }
        }
    }

    private static String[] resolveIcaoPair( Scanner scanner )
    {
        while( true ) {
            String rawDeparture = promptForIcao( scanner, "departure" );
            String rawArrival = promptForIcao( scanner, "arrival" );
            try {
                return new String[] { IcaoParser.parseIcaoInput( rawDeparture ),
                        IcaoParser.parseIcaoInput( rawArrival ) };
            }
            catch( AirportNotFoundException e ) {
                System.out.println( "[ERROR] " + e.getMessage()
                        + " — please try again." );
            }
        }
    }

    private static boolean confirmIcaos( Scanner scanner,
                                         String departure,
                                         String arrival )
    {
        System.out.printf(
                "Departure: %s | Arrival: %s%nIs this correct? (y/n): ",
                departure, arrival );
        while( true ) {
            String answer = scanner.nextLine().trim().toLowerCase();
            if( answer.equals( "y" ) || answer.equals( "yes" ) )
                return true;
            if( answer.equals( "n" ) || answer.equals( "no" ) )
                return false;
            System.out.print( "Please enter 'y' or 'n': " );
        }
    }

    private static String promptForIcao( Scanner scanner, String label )
    {
        System.out.print( "Enter " + label + " airport ICAO: " );
        return scanner.nextLine().trim().toUpperCase();
    }

    private static String parseArg( String[] args, String flag )
    {
        for( int i = 0; i < args.length - 1; i++ ) {
            if( flag.equals( args[i] ) ) {
                return args[i + 1].trim().toUpperCase();
            }
        }
        return null;
    }
}