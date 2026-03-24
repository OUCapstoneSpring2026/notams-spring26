package com.capstone.services;

import java.awt.geom.Point2D;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlightPathCalculatorTest
{

        private static final double TOLERANCE_DELTA_NM = 0.5; // nautical miles tolerance for distance checks
        private static final double TOLERANCE_COORD_DELTA_NM = 0.001; // degrees tolerance for coordinate checks

        // Known real-world values for JFK -> LAX
        private static final Point2D JFK = new Point2D.Double( 40.6397,
                        -73.7789 );
        private static final Point2D LAX = new Point2D.Double( 33.9425,
                        -118.4081 );
        private static final double JFK_LAX_NM = 2151.0;

        // haversineDistance

        @Test
        public void testHaversineDistance_knownRoute()
        {
                double distanceNm = FlightPathCalculator.haversineDistance( JFK,
                                LAX );
                assertEquals( JFK_LAX_NM, distanceNm, 10 ); // within 5 nautical miles of known value
        }

        @Test
        public void testHaversineDistance_samePoint()
        {
                double distanceNm = FlightPathCalculator.haversineDistance( JFK,
                                JFK );
                assertEquals( 0.0, distanceNm, TOLERANCE_DELTA_NM );
        }

        @Test
        public void testHaversineDistance_isSymmetric()
        {
                double forwardDistanceNm = FlightPathCalculator
                                .haversineDistance( JFK, LAX );
                double backwardDistanceNm = FlightPathCalculator
                                .haversineDistance( LAX, JFK );
                assertEquals( forwardDistanceNm, backwardDistanceNm,
                                TOLERANCE_DELTA_NM );
        }

        // interpolate

        @Test
        public void testInterpolate_includesBothEndpoints()
        {
                List<Point2D> points = FlightPathCalculator.interpolate( JFK,
                                LAX, 100 );

                Point2D first = points.get( 0 );
                Point2D last = points.get( points.size() - 1 );

                assertEquals( JFK.getX(), first.getX(),
                                TOLERANCE_COORD_DELTA_NM );
                assertEquals( JFK.getY(), first.getY(),
                                TOLERANCE_COORD_DELTA_NM );
                assertEquals( LAX.getX(), last.getX(),
                                TOLERANCE_COORD_DELTA_NM );
                assertEquals( LAX.getY(), last.getY(),
                                TOLERANCE_COORD_DELTA_NM );
        }

        @Test
        public void testInterpolate_correctNumberOfPoints()
        {
                double intervalNm = 100.0;
                double totalDistanceNm = FlightPathCalculator.haversineDistance(
                                JFK, LAX );
                int numExpectedSegments = (int) Math.ceil( totalDistanceNm
                                / intervalNm );
                int numExpectedPoints = numExpectedSegments + 1; // segments + 1 = points

                List<Point2D> points = FlightPathCalculator.interpolate( JFK,
                                LAX, intervalNm );

                assertEquals( numExpectedPoints, points.size() );
        }

        @Test
        public void testInterpolate_singlePointWhenSameLocation()
        {
                List<Point2D> points = FlightPathCalculator.interpolate( JFK,
                                JFK, 100 );

                // distance is 0, so numSegments rounds up to 0 — only 1 point
                assertEquals( 1, points.size() );
        }

        @Test
        public void testInterpolate_pointsAreRoughlyEquallySpaced()
        {
                double intervalNm = 100.0;
                List<Point2D> points = FlightPathCalculator.interpolate( JFK,
                                LAX, intervalNm );

                for( int i = 0; i < points.size() - 1; i++ ) {
                        double segmentDistanceNm = FlightPathCalculator
                                        .haversineDistance( points.get( i ),
                                                        points.get( i + 1 ) );
                        assertEquals( intervalNm, segmentDistanceNm, 5 ); // within 5 nautical miles
                }
        }

        @Test
        public void testInterpolate_noNullPoints()
        {
                List<Point2D> points = FlightPathCalculator.interpolate( JFK,
                                LAX, 100 );
                for( Point2D point : points ) {
                        assertNotNull( point );
                }
        }

        @Test
        public void testInterpolate_coordinatesInValidRange()
        {
                List<Point2D> points = FlightPathCalculator.interpolate( JFK,
                                LAX, 100 );
                for( Point2D point : points ) {
                        assertTrue( point.getX() >= -90 && point.getX() <= 90,
                                        "Latitude out of range" );
                        assertTrue( point.getY() >= -180 && point.getY() <= 180,
                                        "Longitude out of range" );
                }
        }
}
