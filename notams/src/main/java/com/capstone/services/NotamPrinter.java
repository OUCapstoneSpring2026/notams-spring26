package com.capstone.services;

import java.util.List;

import com.capstone.models.Notam;
import com.fasterxml.jackson.databind.JsonNode;

public class NotamPrinter
{
    private final String DIVIDER = "=".repeat( 80 );
    private final String FIELD = "  %-16s %s%n";
    private final int CONDITION_WIDTH = DIVIDER.length();

    public NotamPrinter()
    {

    }

    /**
     * Prints a compact table of NOTAMs to standard output.
     *
     * @param notams
     *     the list of {@link Notam} objects to print; if null or
     *     empty a message indicating no NOTAMs will be printed
     */
    public void printCompactNotamTable( final List<Notam> notams )
    {
        if( notams == null || notams.isEmpty() ) {
            System.out.println( "No NOTAMs found." );
            return;
        }

        int count = 1;
        for( Notam notam : notams ) {
            System.out.println( DIVIDER );
            System.out.printf( "  NOTAM #%d%n", count );
            System.out.println( DIVIDER );
            System.out.printf( FIELD, "Location:", notam.getLocation().orElse(
                    "N/A" ) );
            System.out.printf( FIELD, "Number:", notam.getNumber() != null ?
                    notam.getNumber() :
                    "N/A" );
            System.out.printf( FIELD, "Start UTC:", formatUtc( notam
                    .getEffectiveStart().toString() ) );
            System.out.printf( FIELD, "End UTC:", formatUtc( notam
                    .getEffectiveEnd().toString() ) );

            System.out.printf( "  Condition:%n" );
            String condition = extractCondition( notam.getText(), false );
            for( int i = 0; i < condition.length(); i += CONDITION_WIDTH ) {
                System.out.println( "  " + condition.substring( i, Math.min( i
                        + CONDITION_WIDTH, condition.length() ) ) );
            }

            count++;
        }
        System.out.println( DIVIDER );
    }

    /**
     * Prints a compact table of NOTAMs from a JSON node (expected to be an
     * array of GeoJSON-like feature objects) to standard output.
     *
     * @param notamsNode
     *     the JSON node containing NOTAM features; if not an
     *     array or if empty a message indicating no NOTAMs will
     *     be printed
     */
    public void printCompactNotamTable( final JsonNode notamsNode )
    {
        System.out.printf( "% -5s %-10s %-12s %-20s %-20s %s%n".replace( "% ",
                "%" ), "#", "Location", "Number", "Start Date UTC",
                "End Date UTC", "Condition" );

        if( !notamsNode.isArray() || notamsNode.isEmpty() ) {
            System.out.println( "No NOTAMs found." );
            return;
        }

        int count = 1;
        for( JsonNode feature : notamsNode ) {
            JsonNode notam = feature.path( "properties" ).path(
                    "coreNOTAMData" ).path( "notam" );

            String location = notam.path( "location" ).asText( "N/A" );
            String number = notam.path( "number" ).asText( "N/A" );
            String start = formatUtc( notam.path( "effectiveStart" ).asText(
                    "N/A" ) );
            String end = formatUtc( notam.path( "effectiveEnd" ).asText(
                    "N/A" ) );
            String condition = extractCondition( notam.path( "text" ).asText(
                    "N/A" ), true );

            System.out.printf( "% -5s %-10s %-12s %-20s %-20s %s%n".replace(
                    "% ", "%" ), "#" + count, location, number, start, end,
                    condition );
            count++;
        }
    }

    /**
     * Extracts the human-readable condition text from a NOTAM body.
     * <p>
     * The method looks for the first occurrence of the marker "E)" and
     * returns the substring that follows. Newlines are collapsed into
     * spaces and the result is trimmed. If the input is null or blank,
     * "N/A" is returned.
     *
     * @param text
     *     the raw NOTAM text
     * @param truncateCondition
     *     whether to truncate the condition text
     * 
     * @return the extracted condition text, or "N/A" when input is null
     *     or blank
     */
    public String extractCondition( final String text,
                                    final boolean truncateCondition )
    {
        if( text == null || text.isBlank() ) {
            return "N/A";
        }

        final int index = text.indexOf( "E)" );
        String condition = index != -1 ? text.substring( index + 2 ) : text;

        condition = condition.replaceAll( "\\r?\\n", " " );
        condition = condition.trim();

        if( truncateCondition && condition.length() > 100 ) {
            condition = condition.substring( 0, 97 ) + "...";
        }

        return condition;
    }

    /**
     * Formats an ISO datetime string to a more human-friendly UTC form.
     * <p>
     * This replaces the 'T' separator with a space and strips the trailing
     * 'Z' designator. If the input is null, blank, or equal to "N/A",
     * the method returns "N/A".
     *
     * @param iso
     *     the ISO datetime string (e.g. "2023-04-01T12:00:00Z")
     * 
     * @return a formatted datetime string or "N/A" when the input is
     *     missing/invalid
     */
    public String formatUtc( final String iso )
    {
        if( iso == null || iso.isBlank() || iso.equals( "N/A" ) ) {
            return "N/A";
        }

        return iso.replace( "T", " " ).replace( "Z", "" );
    }
}
