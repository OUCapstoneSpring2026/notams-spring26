package com.capstone.services;

import com.capstone.exceptions.AirportNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AirportValidatorTest
{
    private final String TEST_CSV = "airportCoordsTest.csv";
    private final String TEST_CSV_MALFORMED = "airportCoordsTest-malformed.csv";

    @Test
    public void parseIcaoInput_fourCharCode_returnsAsIs() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateIcaoInput( "KJFK" );
        assertEquals( "KJFK", result );
    }

    @Test
    public void parseIcaoInput_threeCharCode_whenOnlyPrefixedMatch_returnsPrefixedCode() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateIcaoInput( "JFK" );
        assertEquals( "KJFK", result );
    }

    @Test
    public void parseIcaoInput_threeCharCode_whenOnlyExactMatch_returnsExactCode() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateIcaoInput( "XYZ" );
        assertEquals( "XYZ", result );
    }

    @Test
    public void parseIcaoInput_threeCharCode_whenBothMatchesExist_throwsAirportNotFoundException() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );

        assertThrows( AirportNotFoundException.class, () -> validator
                .validateIcaoInput( "ABC" ) );
    }

    @Test
    public void parseIcaoInput_threeCharCode_whenNoMatchExists_throwsAirportNotFoundException() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );

        assertThrows( AirportNotFoundException.class, () -> validator
                .validateIcaoInput( "ZZZ" ) );
    }

    @Test
    public void parseIcaoInput_twoCharCode_returnsKPrefixedCode() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateIcaoInput( "LA" );
        assertEquals( "KLA", result );
    }

    @Test
    public void parseIcaoInput_invalidLength_throwsAirportNotFoundException() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        assertThrows( AirportNotFoundException.class, () -> validator
                .validateIcaoInput( "A" ) );
    }

    @Test
    public void parseIcaoInput_nullCode_throwsIllegalArgumentException() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        assertThrows( IllegalArgumentException.class, () -> validator
                .validateIcaoInput( null ) );
    }

    @Test
    public void constructor_malformedCoordsInCsv_throwsNumberFormatException()
    {
        assertThrows( NumberFormatException.class, () -> new AirportValidator(
                TEST_CSV_MALFORMED ) );
    }
}
