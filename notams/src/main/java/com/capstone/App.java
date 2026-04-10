package com.capstone;

import com.capstone.exceptions.AirportNotFoundException;
import com.capstone.models.Airport;
import com.capstone.models.FlightPath;
import com.capstone.services.AirportValidator;

import java.util.Scanner;

public class App
{

    public static void main( String[] args )
    {
        String departureIcao = null;
        String arrivalIcao = null;
        AirportValidator airportValidator = null;

        if( args.length > 0 ) {
            final String rawDeparture = parseArg( args, "--departure" );
            final String rawArrival = parseArg( args, "--arrival" );

            try {
                airportValidator = new AirportValidator();
            }
            catch( final Exception e ) {
                System.err.println(
                        "[ERROR] Failed to initialize AirportValidator: " + e
                                .getMessage() );
                System.exit( 1 );
            }

            if( rawDeparture == null || rawArrival == null ) {
                System.err.println(
                        "[ERROR] Usage: ... --departure <ICAO> --arrival <ICAO>" );
                System.exit( 1 );
            }

            try {
                departureIcao = airportValidator.parseIcaoInput( rawDeparture );
                arrivalIcao = airportValidator.parseIcaoInput( rawArrival );
            }
            catch( final AirportNotFoundException e ) {
                System.err.println( "[ERROR] " + e.getMessage() );
                System.exit( 1 );
            }
        }
        else {
            final String[] icaos = promptAndConfirmIcaos( airportValidator );
            departureIcao = icaos[0];
            arrivalIcao = icaos[1];
        }

        try {
            final Airport departure = new Airport( departureIcao );
            final Airport arrival = new Airport( arrivalIcao );

            final FlightPath flightPath = new FlightPath( departure, arrival );

            final NotamFetcher fetcher = new NotamFetcher();
            final String json = fetcher.fetchByIcao( "KOKC", 1000, 1 );
            System.out.println( json );

        }
        catch( final Exception e ) {
            System.err.println( "[ERROR] " + e.getMessage() );
        }
    }

    private static String[] promptAndConfirmIcaos( final AirportValidator airportValidator )
    {
        try (final Scanner scanner = new Scanner( System.in )) {
            while( true ) {
                final String[] resolved = resolveIcaoPair( scanner,
                        airportValidator );
                if( confirmIcaos( scanner, resolved[0], resolved[1] ) ) {
                    return resolved;
                }
            }
        }
    }

    private static String[] resolveIcaoPair( final Scanner scanner,
                                             final AirportValidator airportValidator )
    {
        while( true ) {
            final String rawDeparture = promptForIcao( scanner, "departure" );
            final String rawArrival = promptForIcao( scanner, "arrival" );
            try {
                return new String[] { airportValidator.parseIcaoInput(
                        rawDeparture ), airportValidator.parseIcaoInput(
                                rawArrival ) };
            }
            catch( final AirportNotFoundException e ) {
                System.out.println( "[ERROR] " + e.getMessage()
                        + " — please try again." );
            }
        }
    }

    private static boolean confirmIcaos( final Scanner scanner,
                                         final String departure,
                                         final String arrival )
    {
        System.out.printf(
                "Departure: %s | Arrival: %s%nIs this correct? (y/n): ",
                departure, arrival );
        while( true ) {
            final String answer = scanner.nextLine().trim().toLowerCase();
            if( answer.equals( "y" ) || answer.equals( "yes" ) )
                return true;
            if( answer.equals( "n" ) || answer.equals( "no" ) )
                return false;
            System.out.print( "Please enter 'y' or 'n': " );
        }
    }

    private static String promptForIcao( final Scanner scanner,
                                         final String label )
    {
        System.out.print( "Enter " + label + " airport ICAO: " );
        return scanner.nextLine().trim().toUpperCase();
    }

    private static String parseArg( final String[] args, final String flag )
    {
        for( int i = 0; i < args.length - 1; i++ ) {
            if( flag.equals( args[i] ) ) {
                return args[i + 1].trim().toUpperCase();
            }
        }
        return null;
    }
}