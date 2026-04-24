package com.capstone;

import com.capstone.exceptions.AirportNotFoundException;
import com.capstone.models.Airport;
import com.capstone.models.FlightPath;
import com.capstone.models.Notam;
import com.capstone.services.AirportValidator;
import com.capstone.services.RouteNotamService;
import com.capstone.services.NotamPrinter;

import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class App
{
	private static final Logger logger = LogManager.getLogger();

	public static void main( String[] args )
	{
		String validatedDepartureAirportCode = null;
		String validatedArrivalAirportCode = null;
		AirportValidator airportValidator = null;

		try {
			airportValidator = new AirportValidator();
		}
		catch( final Exception e ) {
			logger.error( "Failed to initialize AirportValidator: {}", e
					.getMessage() );
			System.exit( 1 );
		}

		if( args.length > 0 ) {
			final String departureArg = parseArg( args, "--departure" );
			final String arrivalArg = parseArg( args, "--arrival" );

			if( departureArg == null || arrivalArg == null ) {
				logger.error(
						"Usage: ... --departure <airportCode> --arrival <airportCode>" );
				System.exit( 1 );
			}

			try {
				validatedDepartureAirportCode = airportValidator
						.validateAirportCodeInput( departureArg );
				validatedArrivalAirportCode = airportValidator
						.validateAirportCodeInput( arrivalArg );
			}
			catch( final AirportNotFoundException e ) {
				logger.error( "{}", e.getMessage() );
				System.exit( 1 );
			}
		}
		else {
			final String[] airportCodes = promptAndConfirmAirportCodes(
					airportValidator );
			validatedDepartureAirportCode = airportCodes[0];
			validatedArrivalAirportCode = airportCodes[1];
		}

		try {
			final Airport departure = new Airport(
					validatedDepartureAirportCode, airportValidator );
			final Airport arrival = new Airport( validatedArrivalAirportCode,
					airportValidator );

			final FlightPath flightPath = new FlightPath( departure, arrival );

			final RouteNotamService routeNotamService = new RouteNotamService();
			final List<Notam> notams = routeNotamService.fetchNotamsAlongRoute(
					flightPath );

			System.out.println( "Fetched " + notams.size()
					+ " NOTAMs successfully.\n" );
			NotamPrinter notamPrinter = new NotamPrinter();
			notamPrinter.printCompactNotamTable( notams );
		}
		catch( final Exception e ) {
			logger.error( "Application error", e );
		}
	}

	private static String[] promptAndConfirmAirportCodes( final AirportValidator airportValidator )
	{
		try (final Scanner scanner = new Scanner( System.in )) {
			while( true ) {
				final String[] resolved = resolveAirportCodePair( scanner,
						airportValidator );
				if( confirmAirportCodes( scanner, resolved[0], resolved[1] ) ) {
					return resolved;
				}
			}
		}
	}

	private static String[] resolveAirportCodePair( final Scanner scanner,
													final AirportValidator airportValidator )
	{
		while( true ) {
			System.out.print( "\n" );
			final String rawDeparture = promptForAirportCode( scanner,
					"departure" );
			final String rawArrival = promptForAirportCode( scanner,
					"arrival" );
			try {
				return new String[] { airportValidator.validateAirportCodeInput(
						rawDeparture ), airportValidator
								.validateAirportCodeInput( rawArrival ) };
			}
			catch( final AirportNotFoundException e ) {
				System.out.println( "[ERROR] " + e.getMessage()
						+ " - please try again." );
			}
		}
	}

	private static boolean confirmAirportCodes( final Scanner scanner,
												final String departure,
												final String arrival )
	{
		System.out.printf(
				"Departure: %s | Arrival: %s%nIs this correct? (y/n): ",
				departure, arrival );
		while( true ) {
			final String answer = scanner.nextLine().trim().toLowerCase();
			if( answer.equals( "y" ) || answer.equals( "yes" ) ) {
				return true;
			}
			if( answer.equals( "n" ) || answer.equals( "no" ) ) {
				return false;
			}
			System.out.print( "Please enter 'y' or 'n': " );
		}
	}

	private static String promptForAirportCode( final Scanner scanner,
												final String label )
	{
		System.out.print( "Enter " + label + " airport code: " );
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
