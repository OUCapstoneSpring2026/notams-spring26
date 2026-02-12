package com.capstone.models;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Airport {
    private String icao;
    private Point2D coords;

    // Constructor
    public Airport(String icao) throws Exception{
        this.icao = icao;
        this.coords = loadAirportCoordsFromCsv(icao);
    }

    public String getIcao() { return icao; }

    public Point2D getCoords() { return coords; }

    private Point2D loadAirportCoordsFromCsv(String icao) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("airportCoords.csv");
        if (is == null) throw new Exception("airportCoords.csv not found.");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {

                // TODO: parse coords from CSV
                if (true) {
                    return new Point2D.Double(0, 0);
                }
            }
        }

        throw new Exception("ICAO coords not found: " + icao);
    }
}
