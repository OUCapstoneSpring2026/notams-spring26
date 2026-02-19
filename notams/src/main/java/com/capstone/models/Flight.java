package com.capstone.models;

import java.time.LocalDateTime;

public class Flight {
    private final Airport departureAirport;
    private final Airport arrivalAirport;
    private final LocalDateTime date;
    private FlightPath flightPath;

    public Flight( Airport departureAirport, Airport arrivalAirport, LocalDateTime date )
    {
        if ( departureAirport == null || arrivalAirport == null ) {
            throw new IllegalArgumentException( "Invalid airport entered." );
        }

        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.date = date;
    }

    public Airport getDepartureAirport() 
    { 
        return departureAirport; 
    }

    public Airport getArrivalAirport() 
    { 
        return arrivalAirport; 
    }

    public LocalDateTime getDate() 
    { 
        return date; 
    }

    public FlightPath getFlightPath() 
    { 
        return flightPath; 
    }

    public void setFlightPath( FlightPath flightPath ) 
    {
        if ( flightPath == null ) {
            throw new IllegalArgumentException( "Invalid flight path entered." );
        }

        this.flightPath = flightPath;
    }
}