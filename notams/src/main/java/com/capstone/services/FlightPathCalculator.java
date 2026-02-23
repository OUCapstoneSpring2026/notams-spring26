package com.capstone.services;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class FlightPathCalculator
{

    private static final double EARTH_RADIUS_MILES = 3958.8;

    /**
     * Returns a list of points spaced ~intervalMiles apart along the
     * great-circle path from start to end. Includes both endpoints.
     * Point2D convention: x = latitude, y = longitude
     */
    public static List<Point2D> interpolate( Point2D start,
                                             Point2D end,
                                             double intervalMiles )
    {
        List<Point2D> points = new ArrayList<>();

        double totalDistance = haversineDistance( start, end );
        int numSegments = (int) Math.ceil( totalDistance / intervalMiles );

        for( int i = 0; i <= numSegments; i++ ) {
            double fraction = (double) i / numSegments;
            points.add( interpolatePoint( start, end, fraction ) );
        }

        return points;
    }

    /**
     * Spherical linear interpolation (slerp) between two lat/lon points.
     * Follows the great-circle (shortest) path on the globe.
     */
    private static Point2D interpolatePoint( Point2D start,
                                             Point2D end,
                                             double fraction )
    {
        double lat1 = Math.toRadians( start.getX() );
        double lon1 = Math.toRadians( start.getY() );
        double lat2 = Math.toRadians( end.getX() );
        double lon2 = Math.toRadians( end.getY() );

        double d = 2 * Math.asin( Math.sqrt( haversin( lat2 - lat1 ) + Math.cos(
                lat1 ) * Math.cos( lat2 ) * haversin( lon2 - lon1 ) ) );

        if( d == 0 )
            return start;

        double a = Math.sin( (1 - fraction) * d ) / Math.sin( d );
        double b = Math.sin( fraction * d ) / Math.sin( d );

        double x = a * Math.cos( lat1 ) * Math.cos( lon1 ) + b * Math.cos(
                lat2 ) * Math.cos( lon2 );
        double y = a * Math.cos( lat1 ) * Math.sin( lon1 ) + b * Math.cos(
                lat2 ) * Math.sin( lon2 );
        double z = a * Math.sin( lat1 ) + b * Math.sin( lat2 );

        double lat = Math.toDegrees( Math.atan2( z, Math.sqrt( x * x + y
                * y ) ) );
        double lon = Math.toDegrees( Math.atan2( y, x ) );

        return new Point2D.Double( lat, lon );
    }

    /** Haversine distance between two points in miles */
    public static double haversineDistance( Point2D a, Point2D b )
    {
        double lat1 = Math.toRadians( a.getX() );
        double lat2 = Math.toRadians( b.getX() );
        double dLat = Math.toRadians( b.getX() - a.getX() );
        double dLon = Math.toRadians( b.getY() - a.getY() );

        double h = haversin( dLat ) + Math.cos( lat1 ) * Math.cos( lat2 )
                * haversin( dLon );
        return 2 * EARTH_RADIUS_MILES * Math.asin( Math.sqrt( h ) );
    }

    private static double haversin( double angle )
    {
        return Math.sin( angle / 2 ) * Math.sin( angle / 2 );
    }
}