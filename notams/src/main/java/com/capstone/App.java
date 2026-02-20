package com.capstone;

import java.util.List;
import java.awt.geom.Point2D;

import com.capstone.services.FlightPathCalculator;

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

			NotamFetcher fetcher = new NotamFetcher();
			String json = fetcher.fetchByIcao( "KOKC", 1000, 1 );
			System.out.println( "Fetched successfully." );
			System.out.println( json );

			List<Point2D> points = FlightPathCalculator.interpolate(
					departure.getCoords(), arrival.getCoords(), 100 );

			System.out.printf( "Generated %d points:%n", points.size() );
			points.forEach( p -> System.out.printf( "(%.6f, %.6f)%n", p.getX(),
					p.getY() ) );
		}
		catch( final Exception e ) {
			System.out.println( e.getMessage() );
		}
	}
}
