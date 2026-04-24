package com.capstone.parsing;

import com.capstone.models.Notam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class NmsNotamParser implements NotamParserInterface
{
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger();

    /**
     * Parses the provided JSON string and returns a list of Notam objects.
     */
    @Override
    public List<Notam> parseNotams( String jsonResponse )
    {
        final List<Notam> notamList = new ArrayList<>();

        try {
            final JsonNode root = mapper.readTree( jsonResponse );
            final JsonNode items = root.path( "data" ).path( "geojson" );

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
                    String affectedFIR = null;

                    final JsonNode translations = coreData.path(
                            "notamTranslation" );
                    for( final JsonNode t : translations ) {
                        if( "ICAO".equals( t.path( "type" ).asText() ) ) {
                            formattedText = t.path( "formattedText" ).asText();
                            final String[] lines = formattedText.split( "\n" );

                            String qLine = null;

                            // Not all ICAO translations begin the same way, we need to find where the "Q)" begins.
                            for( final String line : lines ) {
                                if( line.trim().startsWith( "Q)" ) ) {
                                    qLine = line.trim();
                                    break;
                                }
                            }
                            if( qLine != null ) {
                                final String[] qParts = qLine.split( "/" );
                                // qParts[0] is the FIR/header portion, ex: "Q) KZFW"
                                // qParts[1] = selectionCode
                                // qParts[2] = traffic
                                // qParts[3] = purpose
                                // qParts[4] = scope
                                // Later parts contain other Q-line data we are not using right now.
                                // If for some reason the Q-line has fewer than 5, print a warning to stderr,
                                // this will not stop parsing unless the Q-line is missing or we don't find a line that starts with "Q)"
                                if( qParts.length < 5 ) {
                                    logger.trace(
                                            "Q-line has fewer than 5 parts: {}",
                                            qLine );
                                }
                                // Extract affectedFIR from the first Q-line segment
                                if( qParts.length > 0 ) {
                                    final String firstPart = qParts[0].trim();
                                    affectedFIR = firstPart.substring( 2 )
                                            .trim();
                                }
                                // checks are still needed to prevent out of bounds access
                                if( qParts.length > 1 )
                                    selectionCode = qParts[1];
                                if( qParts.length > 2 )
                                    traffic = qParts[2];
                                if( qParts.length > 3 )
                                    purpose = qParts[3];
                                if( qParts.length > 4 )
                                    scope = qParts[4];
                            }
                            else {
                                // if Q-line is missing, the selectionCode, traffic, etc., remain null as initialized above.
                                logger.info(
                                        "ICAO translation found, but missing the Q-line" );
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

                    // Skip NOTAMs that are missing the 7 required fields, but log which fields are missing and add to a list.
                    if( notamId.isBlank() || notamNumber.isBlank() || notamType
                            .isBlank() || notamText.isBlank() || issued == null
                            || effectiveStart == null || effectiveEnd
                                    == null ) {

                        final List<String> missingFields = new ArrayList<>();
                        addMissing( missingFields, notamId.isBlank(), "id" );
                        addMissing( missingFields, notamNumber.isBlank(),
                                "number" );
                        addMissing( missingFields, notamType.isBlank(),
                                "type" );
                        addMissing( missingFields, notamText.isBlank(),
                                "text" );
                        addMissing( missingFields, issued == null, "issued" );
                        addMissing( missingFields, effectiveStart == null,
                                "effectiveStart" );
                        addMissing( missingFields, effectiveEnd == null,
                                "effectiveEnd" );

                        String notamInfoError;
                        String notamInfoLabel;
                        // If we skip a NOTAM, we still want to be able to identify which one was skipped
                        // Prefer NOTAM id first, then fall back to number, text, or formattedText.
                        if( !notamId.isBlank() ) {
                            notamInfoError = notamId;
                            notamInfoLabel = "NOTAM ID: ";
                        }
                        else if( !notamNumber.isBlank() ) {
                            notamInfoError = notamNumber;
                            notamInfoLabel = "NOTAM Number: ";
                        }
                        else if( !notamText.isBlank() ) {
                            notamInfoError = notamText;
                            notamInfoLabel = "NOTAM Text: ";
                        }
                        else if( formattedText != null && !formattedText
                                .isBlank() ) {
                            notamInfoError = formattedText;
                            notamInfoLabel = "NOTAM Formatted Text: ";
                        }
                        else {
                            notamInfoError = "NOTAM does not have ID, number, text, or formattedText available";
                            notamInfoLabel = "No identifying NOTAM information available: ";
                        }
                        // Print the missing fields followed by the NOTAM identifier 
                        logger.warn(
                                "Skipping NOTAM due to missing required fields: {}\nNOTAM info:\n{}{}\n",
                                missingFields, notamInfoLabel, notamInfoError );
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
                            .affectedFIR( affectedFIR ).formattedText(
                                    formattedText ).selectionCode(
                                            selectionCode ).traffic( traffic )
                            .purpose( purpose ).scope( scope ).build();

                    notamList.add( parsedNotam );
                }
                catch( final IllegalArgumentException |
                       NullPointerException e ) {
                    // Catch any bad NOTAM that got past our checks. Skip the broken/missing-info NOTAM, log it and move on
                    logger.error( "Skipped malformed NOTAM: ", e );
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
            return Instant.MAX;
        }
        try {
            return Instant.parse( dateStr );
        }
        catch( final DateTimeParseException e ) {
            logger.warn( "Could not parse timestamp: {}", dateStr, e );
            return null;
        }
    }

    /**
     * Helper to collect the names of any required fields that are missing.
     * 
     * @param missingFields
     *     list of missing required fields
     * @param isMissing
     *     whether the field is missing
     * @param fieldName
     *     name of the field to add
     */
    private void addMissing( List<String> missingFields,
                             boolean isMissing,
                             String fieldName )
    {
        if( isMissing ) {
            missingFields.add( fieldName );
        }
    }
}