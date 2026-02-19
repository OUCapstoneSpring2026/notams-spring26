package com.capstone.models;

import java.time.LocalDateTime;

public class Flight
{
    private final Airport departureAirport;
    private final Airport arrivalAirport;
    private final LocalDateTime date;
    private final FlightPath flightPath;

    public Flight( Airport departureAirport,
                   Airport arrivalAirport,
                   LocalDateTime date )
    {
        if( departureAirport == null ) {
            throw new IllegalArgumentException(
                    "Cannot create flight path with null departure Airport." );
        }

        if( arrivalAirport == null ) {
            throw new IllegalArgumentException(
                    "Cannot create flight path with null arrival Airport." );
        }
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.date = date;

        this.flightPath = new FlightPath( departureAirport, arrivalAirport );
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

}