package com.capstone.services;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import com.capstone.models.Notam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotamPrinterTest
{
    private final PrintStream originalOut = System.out;

    @AfterEach
    void restoreSystemOut()
    {
        System.setOut( originalOut );
    }

    @Test
    void extractCondition_nullOrBlank_returnsNa()
    {
        final NotamPrinter printer = new NotamPrinter();

        assertEquals( "N/A", printer.extractCondition( null, false ) );
        assertEquals( "N/A", printer.extractCondition( "   ", false ) );
    }

    @Test
    void extractCondition_withESectionAndNewline_returnsTrimmedCondition()
    {
        final NotamPrinter printer = new NotamPrinter();
        final String text = "A) KOKC\nB) 2601010000 C) 2601020000\nE) RWY 17L CLSD\nDUE WIP";

        assertEquals( "RWY 17L CLSD DUE WIP", printer.extractCondition( text,
                false ) );
    }

    @Test
    void formatUtc_formatsIsoAndHandlesNaInputs()
    {
        final NotamPrinter printer = new NotamPrinter();

        assertEquals( "2026-04-22 15:30:00", printer.formatUtc(
                "2026-04-22T15:30:00Z" ) );
        assertEquals( "N/A", printer.formatUtc( null ) );
        assertEquals( "N/A", printer.formatUtc( "N/A" ) );
        assertEquals( "N/A", printer.formatUtc( "   " ) );
    }

    @Test
    void printCompactNotamTable_listNull_printsNoNotamsMessage()
    {
        final NotamPrinter printer = new NotamPrinter();

        final String output = captureOutput( () -> printer
                .printCompactNotamTable( (List<Notam>) null ) );

        assertEquals( "No NOTAMs found." + System.lineSeparator(), output );
    }

    @Test
    void printCompactNotamTable_listWithSingleNotam_printsExpectedFields()
    {
        final NotamPrinter printer = new NotamPrinter();
        final Notam notam = Notam.builder().id( "id-1" ).number( "A0123/26" )
                .type( "N" ).issued( Instant.parse( "2026-04-22T00:00:00Z" ) )
                .location( "KOKC" ).effectiveStart( Instant.parse(
                        "2026-04-22T15:30:00Z" ) ).effectiveEnd( Instant.parse(
                                "2026-04-23T16:45:00Z" ) ).text(
                                        "A) KOKC E) TWY B CLSD FOR MAINT" )
                .build();

        final String output = captureOutput( () -> printer
                .printCompactNotamTable( List.of( notam ) ) );

        assertTrue( output.contains( "NOTAM #1" ) );
        assertTrue( output.contains( "Location:" ) );
        assertTrue( output.contains( "KOKC" ) );
        assertTrue( output.contains( "Number:" ) );
        assertTrue( output.contains( "A0123/26" ) );
        assertTrue( output.contains( "Start UTC:" ) );
        assertTrue( output.contains( "2026-04-22 15:30:00" ) );
        assertTrue( output.contains( "End UTC:" ) );
        assertTrue( output.contains( "2026-04-23 16:45:00" ) );
        assertTrue( output.contains( "Condition:" ) );
        assertTrue( output.contains( "TWY B CLSD FOR MAINT" ) );
    }

    @Test
    void printCompactNotamTable_jsonArray_printsHeaderAndNotamRow() throws Exception
    {
        final NotamPrinter printer = new NotamPrinter();
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree(
                "[{\"properties\":{\"coreNOTAMData\":{\"notam\":{\"location\":\"KDFW\",\"number\":\"A0456/26\",\"effectiveStart\":\"2026-04-22T10:00:00Z\",\"effectiveEnd\":\"2026-04-22T12:00:00Z\",\"text\":\"A) KDFW E) RWY 18R CLSD\"}}}}]" );

        final String output = captureOutput( () -> printer
                .printCompactNotamTable( node ) );

        assertTrue( output.contains( "#     Location" ) );
        assertTrue( output.contains( "#1" ) );
        assertTrue( output.contains( "KDFW" ) );
        assertTrue( output.contains( "A0456/26" ) );
        assertTrue( output.contains( "2026-04-22 10:00:00" ) );
        assertTrue( output.contains( "2026-04-22 12:00:00" ) );
        assertTrue( output.contains( "RWY 18R CLSD" ) );
    }

    private String captureOutput( final Runnable action )
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream capture = new PrintStream( out, true,
                StandardCharsets.UTF_8 );
        System.setOut( capture );
        action.run();
        capture.flush();
        return out.toString( StandardCharsets.UTF_8 );
    }
}
