package com.capstone;

import com.capstone.exceptions.AirportNotFoundException;
import com.capstone.models.Airport;
import com.capstone.models.FlightPath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.capstone.services.AirportValidator;

import java.util.Scanner;

public class App
{
	private static final Logger logger = LogManager.getLogger();

	public static void main( String[] args )
	{
		String validatedDepartureIcao = null;
		String validatedArrivalIcao = null;
		AirportValidator airportValidator = null;

		try {
			airportValidator = new AirportValidator();
		}
		catch( final Exception e ) {
			logger.error( "Failed to initialize AirportValidator: {}",
					e.getMessage() );
			System.exit( 1 );
		}

		if( args.length > 0 ) {
			final String departureArg = parseArg( args, "--departure" );
			final String arrivalArg = parseArg( args, "--arrival" );

			if( departureArg == null || arrivalArg == null ) {
				logger.error(
						"Usage: ... --departure <ICAO> --arrival <ICAO>" );
				System.exit( 1 );
			}

			try {
				validatedDepartureIcao = airportValidator.validateIcaoInput(
						departureArg );
				validatedArrivalIcao = airportValidator.validateIcaoInput(
						arrivalArg );
			}
			catch( final AirportNotFoundException e ) {
				logger.error( "{}", e.getMessage() );
				System.exit( 1 );
			}
		}
		else {
			final String[] icaos = promptAndConfirmIcaos( airportValidator );
			validatedDepartureIcao = icaos[0];
			validatedArrivalIcao = icaos[1];
		}

		try {
			final Airport departure = new Airport( validatedDepartureIcao,
					airportValidator );
			final Airport arrival = new Airport( validatedArrivalIcao,
					airportValidator );

			final FlightPath flightPath = new FlightPath( departure, arrival );

			final NotamDataFetcher fetcher = new NmsNotamFetcher();
			final String json = fetcher.fetchByIcao( validatedDepartureIcao );

			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree( json );

			JsonNode notamsNode = rootNode.path( "data" ).path( "geojson" );
			int notamCount = notamsNode.isArray() ? notamsNode.size() : 0;

			System.out.println(
					"Fetched " + notamCount + " NOTAMs successfully." );
			System.out.println();
			printCompactNotamTable( notamsNode );

		}
		catch( final Exception e ) {
			logger.error( "Application error", e );
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
			System.out.print( "\n" );
			final String rawDeparture = promptForIcao( scanner, "departure" );
			final String rawArrival = promptForIcao( scanner, "arrival" );
			try {
				return new String[] {
						airportValidator.validateIcaoInput( rawDeparture ),
						airportValidator.validateIcaoInput( rawArrival ) };
			}
			catch( final AirportNotFoundException e ) {
				System.out.println(
						"[ERROR] " + e.getMessage() + " - please try again." );
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
			if( answer.equals( "y" ) || answer.equals( "yes" ) ) {
				return true;
			}
			if( answer.equals( "n" ) || answer.equals( "no" ) ) {
				return false;
			}
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

	private static void printCompactNotamTable( JsonNode notamsNode )
	{
		System.out.printf(
				"% -5s %-10s %-12s %-20s %-20s %s%n".replace( "% ", "%" ), "#",
				"Location", "Number", "Start Date UTC", "End Date UTC",
				"Condition" );

		if( !notamsNode.isArray() || notamsNode.isEmpty() ) {
			System.out.println( "No NOTAMs found." );
			return;
		}

		int count = 1;
		for( JsonNode feature : notamsNode ) {
			JsonNode notam = feature.path( "properties" )
					.path( "coreNOTAMData" ).path( "notam" );

			String location = notam.path( "location" ).asText( "N/A" );
			String number = notam.path( "number" ).asText( "N/A" );
			String start = formatUtc(
					notam.path( "effectiveStart" ).asText( "N/A" ) );
			String end = formatUtc(
					notam.path( "effectiveEnd" ).asText( "N/A" ) );
			String condition = extractCondition(
					notam.path( "text" ).asText( "N/A" ) );

			System.out.printf(
					"% -5s %-10s %-12s %-20s %-20s %s%n".replace( "% ", "%" ),
					"#" + count, location, number, start, end, condition );
			count++;
		}
	}

	private static String extractCondition( String text )
	{
		if( text == null || text.isBlank() ) {
			return "N/A";
		}

		int index = text.indexOf( "E)" );
		String condition = index != -1 ? text.substring( index + 2 ) : text;

		condition = condition.replaceAll( "\\r?\\n", " " );
		condition = condition.trim();

		if( condition.length() > 100 ) {
			condition = condition.substring( 0, 97 ) + "...";
		}

		return condition;
	}

	private static String formatUtc( String iso )
	{
		if( iso == null || iso.isBlank() || iso.equals( "N/A" ) ) {
			return "N/A";
		}

		return iso.replace( "T", " " ).replace( "Z", "" );
	}

}
