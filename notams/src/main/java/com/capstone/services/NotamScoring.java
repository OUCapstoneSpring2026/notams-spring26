package com.capstone.services;

import com.capstone.models.Notam;

public class NotamScoring
{
    /**
     * DUMMY SCORING ALGORITHM (CAP-29):
     * Calculates a priority score based on the length of the NOTAM text.
     * The score is capped at a maximum of 100.
     * This should work when our Notam class is merged and we have access.
     * Will update App.java to include this once approved.
     */
    public static int calculateScore( Notam notam )
    {
        if( notam == null ) {
            throw new IllegalArgumentException(
                    "null Notam object cannot be scored" );
        }

        // Get the char. count of notam text
        int textLength = notam.getText().length();

        // Return the length as the priority score (capped at 100)
        return Math.min( textLength, 100 );
    }
}