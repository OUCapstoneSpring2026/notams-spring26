package com.capstone.services;

import com.capstone.exceptions.AirportNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AirportValidatorTest
{
    private final String TEST_CSV = "airportCoordsTest.csv";
    private final String TEST_CSV_MALFORMED = "airportCoordsTest-malformed.csv";

    @Test
    public void parseAirportCodeInput_fourCharCode_returnsAsIs() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateAirportCodeInput( "KJFK" );
        assertEquals( "KJFK", result );
    }

    @Test
    public void parseAirportCodeInput_threeCharCode_whenOnlyPrefixedMatch_returnsPrefixedCode() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateAirportCodeInput( "JFK" );
        assertEquals( "KJFK", result );
    }

    @Test
    public void parseAirportCodeInput_threeCharCode_whenOnlyExactMatch_returnsExactCode() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateAirportCodeInput( "XYZ" );
        assertEquals( "XYZ", result );
    }

    @Test
    public void parseAirportCodeInput_threeCharCode_whenBothMatchesExist_throwsAirportNotFoundException() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );

        assertThrows( AirportNotFoundException.class, () -> validator
                .validateAirportCodeInput( "ABC" ) );
    }

    @Test
    public void parseAirportCodeInput_threeCharCode_whenNoMatchExists_throwsAirportNotFoundException() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );

        assertThrows( AirportNotFoundException.class, () -> validator
                .validateAirportCodeInput( "ZZZ" ) );
    }

    @Test
    public void parseAirportCodeInput_twoCharCode_returnsKPrefixedCode() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateAirportCodeInput( "LA" );
        assertEquals( "KLA", result );
    }

    @Test
    public void parseAirportCodeInput_threeCharCode_whenOnlyArptIdExists_returnsArptId() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        String result = validator.validateAirportCodeInput( "BOS" );
        assertEquals( "BOS", result );
    }

    @Test
    public void parseAirportCodeInput_invalidLength_throwsAirportNotFoundException() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        assertThrows( AirportNotFoundException.class, () -> validator
                .validateAirportCodeInput( "A" ) );
    }

    @Test
    public void parseAirportCodeInput_nullCode_throwsIllegalArgumentException() throws Exception
    {
        AirportValidator validator = new AirportValidator( TEST_CSV );
        assertThrows( IllegalArgumentException.class, () -> validator
                .validateAirportCodeInput( null ) );
    }

    @Test
    public void constructor_malformedCoordsInCsv_skipsMalformedRows()
    {
        AirportValidator validator = assertDoesNotThrow(
                () -> new AirportValidator( TEST_CSV_MALFORMED ) );
        assertThrows( AirportNotFoundException.class, () -> validator
                .validateAirportCodeInput( "BAD" ) );
    }
}
