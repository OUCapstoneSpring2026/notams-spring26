package com.capstone;

import com.capstone.models.Airport;

public class App {
    public static void main(String[] args) {
        try {
            final Airport departure = new Airport("KOKC");
            final Airport arrival = new Airport("KDFW");

            NotamFetcher fetcher = new NotamFetcher();

            // String json = fetcher.fetchByIcao("KOKC");
            String json = fetcher.fetchByIcao("KOKC", 1000, 1);
            // String json = fetcher.fetchByLocation(35.39307, -97.60077, 25);

            System.out.println("Fetched successfully.");
            System.out.println(json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}