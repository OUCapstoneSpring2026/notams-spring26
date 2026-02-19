package com.capstone.models;

import java.time.LocalDateTime;

public class Flight {
    private final Airport departureAirport;
    private final Airport arrivalAirport;
    private LocalDateTime date;
    private FlightPath flightPath;

    public Flight( Airport departureAirport, Airport arrivalAirport )
    {
        if ( departureAirport == null || arrivalAirport == null ) {
            throw new IllegalArgumentException( "Invalid airport entered." );
        }

        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.date = LocalDateTime.now();
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

    public boolean setFlightPath( FlightPath flightPath ) 
    {
        if ( flightPath == null ) {
            return false;
        }

        this.flightPath = flightPath;
        return true;
    }
}