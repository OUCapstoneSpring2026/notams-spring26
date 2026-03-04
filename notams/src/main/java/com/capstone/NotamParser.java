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
        List<Notam> notamList = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree( jsonResponse );
            JsonNode items = root.path( "items" );

            for( JsonNode item : items ) {
                try {
                    JsonNode coreData = item.path( "properties" ).path(
                            "coreNOTAMData" );
                    JsonNode notamNode = coreData.path( "notam" );

                    String formattedText = null;
                    String selectionCode = null;
                    String traffic = null;
                    String purpose = null;
                    String scope = null;

                    JsonNode translations = coreData.path( "notamTranslation" );
                    for( JsonNode t : translations ) {
                        if( "ICAO".equals( t.path( "type" ).asText() ) ) {
                            formattedText = t.path( "formattedText" ).asText();
                            String[] lines = formattedText.split( "\n" );
                            if( lines.length > 1 ) {
                                String[] qParts = lines[1].split( "/" );
                                if( qParts.length > 1 )
                                    selectionCode = qParts[1];
                                if( qParts.length > 2 )
                                    traffic = qParts[2];
                                if( qParts.length > 3 )
                                    purpose = qParts[3];
                                if( qParts.length > 4 )
                                    scope = qParts[4];
                            }
                        }
                    }

                    // parse timestamps into Instants
                    Instant issued = parseInstant( notamNode.path( "issued" )
                            .asText() );
                    Instant effectiveStart = parseInstant( notamNode.path(
                            "effectiveStart" ).asText() );
                    Instant effectiveEnd = parseInstant( notamNode.path(
                            "effectiveEnd" ).asText() );

                    Notam parsedNotam = Notam.builder().id( notamNode.path(
                            "id" ).asText() ).number( notamNode.path( "number" )
                                    .asText() ).type( notamNode.path( "type" )
                                            .asText() ).issued( issued )
                            .effectiveStart( effectiveStart ).effectiveEnd(
                                    effectiveEnd ).text( notamNode.path(
                                            "text" ).asText() ).location(
                                                    notamNode.path( "location" )
                                                            .asText() )
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
                catch( IllegalArgumentException | NullPointerException e ) {
                    // Logs the bad NOTAM and keeps the loop moving
                    System.err.println( "Skipped malformed NOTAM: " + e
                            .getMessage() );
                }
            }
        }
        catch( JsonProcessingException e ) {
            System.err.println( "Failed to parse NOTAM JSON: " + e
                    .getMessage() );
        }

        return notamList;
    }

    /**
     * Helper method to convert the effective start/end and issued strings into
     * Instants
     * Returns null if the string is empty or invalid
     */
    private Instant parseInstant( String dateStr )
    {
        if( dateStr == null || dateStr.isBlank() ) {
            return null;
        }
        try {
            return Instant.parse( dateStr );
        }
        catch( DateTimeParseException e ) {
            return null;
        }
    }
}