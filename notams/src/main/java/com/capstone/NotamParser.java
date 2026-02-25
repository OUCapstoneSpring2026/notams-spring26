package com.capstone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotamParser
{
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses the provided JSON string and returns a list where each element
     * represents a NOTAM's data as key-value pairs.
     */
    public List<Map<String, String>> parseNotams( String jsonResponse )
    {
        List<Map<String, String>> notamList = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree( jsonResponse );
            JsonNode items = root.path( "items" );

            for( JsonNode item : items ) {
                JsonNode coreData = item.path( "properties" ).path(
                        "coreNOTAMData" );
                JsonNode notamNode = coreData.path( "notam" );

                Map<String, String> data = new HashMap<>();

                // extracting the fields we might need 
                data.put( "id", notamNode.path( "id" ).asText() );
                data.put( "number", notamNode.path( "number" ).asText() );
                data.put( "type", notamNode.path( "type" ).asText() );
                data.put( "issued", notamNode.path( "issued" ).asText() );
                data.put( "effectiveStart", notamNode.path( "effectiveStart" )
                        .asText() );
                data.put( "effectiveEnd", notamNode.path( "effectiveEnd" )
                        .asText() );
                data.put( "text", notamNode.path( "text" ).asText() );
                data.put( "location", notamNode.path( "location" ).asText() );
                data.put( "classification", notamNode.path( "classification" )
                        .asText() );
                data.put( "icaoLocation", notamNode.path( "icaoLocation" )
                        .asText() );
                data.put( "coordinates", notamNode.path( "coordinates" )
                        .asText() );
                data.put( "radius", notamNode.path( "radius" ).asText() );
                data.put( "series", notamNode.path( "series" ).asText() );
                data.put( "affectedFIR", notamNode.path( "affectedFIR" )
                        .asText() );

                JsonNode translations = coreData.path( "notamTranslation" );
                for( JsonNode t : translations ) {
                    if( "ICAO".equals( t.path( "type" ).asText() ) ) {
                        String fullText = t.path( "formattedText" ).asText();
                        data.put( "formattedText", fullText );

                        // This section splits the Q-line Q) KZFW/QPIXX/I/NBO/A/...
                        String[] lines = fullText.split( "\n" );
                        if( lines.length > 1 ) {
                            String[] qParts = lines[1].split( "/" );
                            if( qParts.length >= 5 ) {
                                data.put( "selectionCode", qParts[1] ); // This is the Q-code refer to CAP-16 for link to tables
                                data.put( "traffic", qParts[2] ); // Traffic can also be useful refer to CAP-16
                                data.put( "purpose", qParts[3] ); // Priority Code (e.g., NBO)
                                data.put( "scope", qParts[4] ); // Scope A-Aerodrome, E-Enroute,W-Navigation warning,K-checklist
                            }
                        }
                    }
                }
                notamList.add( data );
            }
        }
        catch( Exception e ) {
            System.err.println( "Parsing error: " + e.getMessage() );
        }
        return notamList;
    }
}