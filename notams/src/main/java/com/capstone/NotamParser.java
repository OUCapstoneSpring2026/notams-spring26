package com.capstone;

import com.capstone.models.Notam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

public class NotamParser
{
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses the provided JSON string and returns a list of Notam objects.
     */
    public List<Notam> parseNotams( String jsonResponse )
    {
        final List<Notam> notamList = new ArrayList<>();

        try {
            final JsonNode root = mapper.readTree( jsonResponse );
            final JsonNode items = root.path( "items" );

            for( final JsonNode item : items ) {
                try {
                    final JsonNode coreData = item.path( "properties" ).path(
                            "coreNOTAMData" );
                    final JsonNode notamNode = coreData.path( "notam" );

                    String formattedText = null;
                    String selectionCode = null;
                    String traffic = null;
                    String purpose = null;
                    String scope = null;

                    final JsonNode translations = coreData.path(
                            "notamTranslation" );
                    for( final JsonNode t : translations ) {
                        if( "ICAO".equals( t.path( "type" ).asText() ) ) {
                            formattedText = t.path( "formattedText" ).asText();
                            final String[] lines = formattedText.split( "\n" );

                            String qLine = null;

                            // Not all ICAO translations begin the same way, we need to find where the "Q)" is
                            for( final String line : lines ) {
                                if( line.trim().startsWith( "Q)" ) ) {
                                    qLine = line.trim();
                                    break;
                                }
                            }
                            if( qLine != null ) {
                                final String[] qParts = qLine.split( "/" );
                                // the Q-line has 8 parts but we are only interested in the first 5.
                                // Stuff could be missing like traffic, purpose, or scope, but the amount of / remain the same.
                                // example: Q) KZFW/QLPAS////000/999/3524N09736W005
                                // however if for some reason the Q-line has fewer than 5 parts
                                if( qParts.length < 5 ) {
                                    System.err.println(
                                            "Warning: Q-line has fewer than 5 parts: "
                                                    + qLine );
                                }
                                if( qParts.length > 1 && !qParts[1].isBlank() )
                                    selectionCode = qParts[1];
                                if( qParts.length > 2 && !qParts[2].isBlank() )
                                    traffic = qParts[2];
                                if( qParts.length > 3 && !qParts[3].isBlank() )
                                    purpose = qParts[3];
                                if( qParts.length > 4 && !qParts[4].isBlank() )
                                    scope = qParts[4];
                            }
                            else {
                                // if Q-line is missing, the selectionCode, traffic, etc., remain null as initialized above.
                                System.err.println(
                                        "Warning: ICAO translation found, but missing the Q-line" );
                            }
                        }
                    }

                    // parse timestamps into Instants
                    final Instant issued = parseInstant( notamNode.path(
                            "issued" ).asText() );
                    final Instant effectiveStart = parseInstant( notamNode.path(
                            "effectiveStart" ).asText() );
                    final Instant effectiveEnd = parseInstant( notamNode.path(
                            "effectiveEnd" ).asText() );

                    // Extract the required strings that Notam class is expecting
                    final String notamId = notamNode.path( "id" ).asText();
                    final String notamNumber = notamNode.path( "number" )
                            .asText();
                    final String notamType = notamNode.path( "type" ).asText();
                    final String notamText = notamNode.path( "text" ).asText();

                    // Check If any of the 7 required fields are missing/null.
                    if( notamId.isBlank() || notamNumber.isBlank() || notamType
                            .isBlank() || notamText.isBlank() || issued == null
                            || effectiveStart == null || effectiveEnd
                                    == null ) {

                        final String displayId = notamId.isBlank() ?
                                "MISSING_ID" :
                                notamId;
                        System.err.println(
                                "WARNING: Skipping NOTAM due to missing required field. ID: "
                                        + displayId );
                        continue; // log and skip to the next NOTAM
                    }

                    final Notam parsedNotam = Notam.builder().id( notamId )
                            .number( notamNumber ).type( notamType ).issued(
                                    issued ).effectiveStart( effectiveStart )
                            .effectiveEnd( effectiveEnd ).text( notamText )
                            .location( notamNode.path( "location" ).asText() )
                            .classification( notamNode.path( "classification" )
                                    .asText() ).icaoLocation( notamNode.path(
                                            "icaoLocation" ).asText() )
                            .coordinates( notamNode.path( "coordinates" )
                                    .asText() ).radius( notamNode.path(
                                            "radius" ).asText() ).series(
                                                    notamNode.path( "series" )
                                                            .asText() )
                            .affectedFIR( notamNode.path( "affectedFIR" )
                                    .asText() ).formattedText( formattedText )
                            .selectionCode( selectionCode ).traffic( traffic )
                            .purpose( purpose ).scope( scope ).build();

                    notamList.add( parsedNotam );

                }
                catch( final IllegalArgumentException |
                       NullPointerException e ) {
                    // Catch any bad NOTAM that got past our checks. Skip the broken/missing-info NOTAM, log it and move on
                    System.err.println( "Skipped malformed NOTAM: " + e
                            .getMessage() );
                }
            }
        }
        catch( final JsonProcessingException e ) {
            throw new RuntimeException( "Failed to parse NOTAM JSON: " + e
                    .getMessage(), e );
        }

        return notamList;
    }

    /**
     * Helper method to convert the effective start/end and issued strings into
     * Instants
     * Returns null if the string is empty or invalid
     */
    private Instant parseInstant( final String dateStr )
    {
        if( dateStr == null || dateStr.isBlank() ) {
            return null;
        }
        // After testing with our given example json file, there was a case where end
        // time was missing because it was "PERM". So for now we can represent perm
        // as a date far in the future. 
        if( "PERM".equalsIgnoreCase( dateStr.trim() ) ) {
            return Instant.parse( "2167-06-07T23:59:59Z" );
        }
        try {
            return Instant.parse( dateStr );
        }
        catch( final DateTimeParseException e ) {
            return null;
        }
    }
}