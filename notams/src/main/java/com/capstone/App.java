package com.capstone;

import com.capstone.models.Airport;


public class App 
{
    public static void main( String[] args )
    {
        try {
            Airport departure = new Airport("KOKC");
            Airport arrival = new Airport("KDFW");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
