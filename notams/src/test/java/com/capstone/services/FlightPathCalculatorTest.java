package com.capstone.services;

import org.junit.Test;
import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.Assert.*;

public class FlightPathCalculatorTest
{

    private static final double DELTA = 0.5; // miles tolerance for distance checks
    private static final double COORD_DELTA = 0.001; // degrees tolerance for coordinate checks

    // Known real-world values for NYC -> LAX
    private static final Point2D NYC = new Point2D.Double( 40.7128, -74.0060 );
    private static final Point2D LAX = new Point2D.Double( 33.9425, -118.4081 );
    private static final double NYC_LAX_MILES = 2451.0;

    // haversineDistance

    @Test
    public void testHaversineDistance_knownRoute()
    {
        double distance = FlightPathCalculator.haversineDistance( NYC, LAX );
        assertEquals( NYC_LAX_MILES, distance, 10.0 ); // within 10 miles of known value
    }

    @Test
    public void testHaversineDistance_samePoint()
    {
        double distance = FlightPathCalculator.haversineDistance( NYC, NYC );
        assertEquals( 0.0, distance, DELTA );
    }

    @Test
    public void testHaversineDistance_isSymmetric()
    {
        double forward = FlightPathCalculator.haversineDistance( NYC, LAX );
        double backward = FlightPathCalculator.haversineDistance( LAX, NYC );
        assertEquals( forward, backward, DELTA );
    }

    // interpolate

    @Test
    public void testInterpolate_includesBothEndpoints()
    {
        List<Point2D> points = FlightPathCalculator.interpolate( NYC, LAX,
                100 );

        Point2D first = points.get( 0 );
        Point2D last = points.get( points.size() - 1 );

        assertEquals( NYC.getX(), first.getX(), COORD_DELTA );
        assertEquals( NYC.getY(), first.getY(), COORD_DELTA );
        assertEquals( LAX.getX(), last.getX(), COORD_DELTA );
        assertEquals( LAX.getY(), last.getY(), COORD_DELTA );
    }

    @Test
    public void testInterpolate_correctNumberOfPoints()
    {
        double intervalMiles = 100.0;
        double totalDistance = FlightPathCalculator.haversineDistance( NYC,
                LAX );
        int expectedSegments = (int) Math.ceil( totalDistance / intervalMiles );
        int expectedPoints = expectedSegments + 1; // segments + 1 = points

        List<Point2D> points = FlightPathCalculator.interpolate( NYC, LAX,
                intervalMiles );

        assertEquals( expectedPoints, points.size() );
    }

    @Test
    public void testInterpolate_singlePointWhenSameLocation()
    {
        List<Point2D> points = FlightPathCalculator.interpolate( NYC, NYC,
                100 );

        // distance is 0, so numSegments rounds up to 0 — only 1 point
        assertEquals( 1, points.size() );
    }

    @Test
    public void testInterpolate_pointsAreRoughlyEquallySpaced()
    {
        double intervalMiles = 100.0;
        List<Point2D> points = FlightPathCalculator.interpolate( NYC, LAX,
                intervalMiles );

        for( int i = 0; i < points.size() - 1; i++ ) {
            double segmentDistance = FlightPathCalculator.haversineDistance(
                    points.get( i ), points.get( i + 1 ) );
            assertEquals( intervalMiles, segmentDistance, 5.0 ); // within 5 miles
        }
    }

    @Test
    public void testInterpolate_noNullPoints()
    {
        List<Point2D> points = FlightPathCalculator.interpolate( NYC, LAX,
                100 );
        for( Point2D point : points ) {
            assertNotNull( point );
        }
    }

    @Test
    public void testInterpolate_coordinatesInValidRange()
    {
        List<Point2D> points = FlightPathCalculator.interpolate( NYC, LAX,
                100 );
        for( Point2D point : points ) {
            assertTrue( "Latitude out of range", point.getX() >= -90 && point
                    .getX() <= 90 );
            assertTrue( "Longitude out of range", point.getY() >= -180 && point
                    .getY() <= 180 );
        }
    }
}