package com.capstone.parsing;

import com.capstone.models.Notam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotamParserTest
{
  private NotamParser parser;

  @BeforeEach
  void setUp()
  {
    parser = new NotamParser();
  }

  @Test
  void parseValidNotam()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "NOTAM_1_77677009",
                    "series": "A",
                    "number": "A1441/25",
                    "type": "N",
                    "issued": "2025-08-19T17:55:00.000Z",
                    "affectedFIR": "KZFW",
                    "selectionCode": "QMNHW",
                    "minimumFL": "000",
                    "maximumFL": "999",
                    "location": "OKC",
                    "effectiveStart": "2025-08-19T17:47:00.000Z",
                    "effectiveEnd": "2026-04-30T22:00:00.000Z",
                    "text": "OKC APRON FIXED BASE OPR WEST RAMP W 1085FT WIP CONST ADJ S END\\nLGTD AND BARRICADED",
                    "classification": "INTL",
                    "accountId": "KOKC",
                    "lastUpdated": "2025-08-19T17:56:00.000Z",
                    "icaoLocation": "KOKC",
                    "coordinates": "3524N09736W",
                    "radius": "005"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "A1441/25 NOTAMN\\nQ) KZFW/QMNHW////000/999/3524N09736W005\\nA) KOKC\\nB) 2508191747\\nC) 2604302200 EST\\nE) OKC APRON FIXED BASE OPR WEST RAMP W 1085FT WIP CONST ADJ S END\\nLGTD AND BARRICADED"
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

    List<Notam> notams = parser.parseNotams( json );
    assertEquals( 1, notams.size() );
    Notam n = notams.get( 0 );
    assertEquals( "NOTAM_1_77677009", n.getId() );
    assertEquals( "A1441/25", n.getNumber() );
    assertEquals( "N", n.getType() );
    assertEquals( Instant.parse( "2025-08-19T17:55:00.000Z" ), n.getIssued() );
    assertEquals( Instant.parse( "2025-08-19T17:47:00.000Z" ), n
        .getEffectiveStart() );
    assertEquals( Instant.parse( "2026-04-30T22:00:00.000Z" ), n
        .getEffectiveEnd() );
    assertEquals(
        "OKC APRON FIXED BASE OPR WEST RAMP W 1085FT WIP CONST ADJ S END\nLGTD AND BARRICADED",
        n.getText() );
    assertEquals( "OKC", n.getLocation().orElseThrow() );
    assertEquals( "INTL", n.getClassification().orElseThrow() );
    assertEquals( "KOKC", n.getIcaoLocation().orElseThrow() );
    assertEquals( "3524N09736W", n.getCoordinates().orElseThrow() );
    assertEquals( "005", n.getRadius().orElseThrow() );
    assertEquals( "A", n.getSeries().orElseThrow() );

    // Q-line parsing
    assertEquals( "KZFW", n.getAffectedFIR().orElseThrow() );
    assertEquals( "QMNHW", n.getSelectionCode().orElseThrow() );

    // The Q-line uses ////, so these optional fields should remain empty.
    assertTrue( n.getTraffic().isEmpty() );
    assertTrue( n.getPurpose().isEmpty() );
    assertTrue( n.getScope().isEmpty() );

    assertTrue( n.getFormattedText().isPresent() );
  }

  @Test
  void skipsNotamMissingRequiredField()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "",
                    "series": "A",
                    "number": "A1441/25",
                    "type": "N",
                    "issued": "2025-08-19T17:55:00.000Z",
                    "affectedFIR": "KZFW",
                    "selectionCode": "QMNHW",
                    "minimumFL": "000",
                    "maximumFL": "999",
                    "location": "OKC",
                    "effectiveStart": "2025-08-19T17:47:00.000Z",
                    "effectiveEnd": "2026-04-30T22:00:00.000Z",
                    "text": "OKC APRON FIXED BASE OPR WEST RAMP W 1085FT WIP CONST ADJ S END\\nLGTD AND BARRICADED",
                    "classification": "INTL",
                    "accountId": "KOKC",
                    "lastUpdated": "2025-08-19T17:56:00.000Z",
                    "icaoLocation": "KOKC",
                    "coordinates": "3524N09736W",
                    "radius": "005"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "A1441/25 NOTAMN\\nQ) KZFW/QMNHW////000/999/3524N09736W005\\nA) KOKC\\nB) 2508191747\\nC) 2604302200 EST\\nE) OKC APRON FIXED BASE OPR WEST RAMP W 1085FT WIP CONST ADJ S END\\nLGTD AND BARRICADED"
                    }
                  ]
                }
              }
            }
          ]
        }
        """;
    // Missing id
    List<Notam> result = parser.parseNotams( json );
    assertTrue( result.isEmpty() );
  }

  @Test
  void skipsInvalidNotam_And_ParsesValidNotam()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "",
                    "series": "A",
                    "number": "",
                    "type": "",
                    "issued": "2025-08-19T17:55:00.000Z",
                    "affectedFIR": "KZFW",
                    "selectionCode": "QMNHW",
                    "minimumFL": "000",
                    "maximumFL": "999",
                    "location": "OKC",
                    "effectiveStart": "",
                    "effectiveEnd": "2026-04-30T22:00:00.000Z",
                    "text": "OKC APRON FIXED BASE OPR WEST RAMP W 1085FT WIP CONST ADJ S END\\nLGTD AND BARRICADED",
                    "classification": "INTL",
                    "accountId": "KOKC",
                    "lastUpdated": "2025-08-19T17:56:00.000Z",
                    "icaoLocation": "KOKC",
                    "coordinates": "3524N09736W",
                    "radius": "005"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "A1441/25 NOTAMN\\nQ) KZFW/QMNHW////000/999/3524N09736W005\\nA) KOKC\\nB) 2508191747\\nC) 2604302200 EST\\nE) OKC APRON FIXED BASE OPR WEST RAMP W 1085FT WIP CONST ADJ S END\\nLGTD AND BARRICADED"
                    }
                  ]
                }
              }
            },
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "NOTAM_1_77921818",
                    "series": "A",
                    "number": "09/050",
                    "type": "N",
                    "issued": "2025-09-12T22:55:00.000Z",
                    "affectedFIR": "KZFW",
                    "selectionCode": "QMXLC",
                    "minimumFL": "000",
                    "maximumFL": "999",
                    "location": "OKC",
                    "effectiveStart": "2025-09-12T22:55:00.000Z",
                    "effectiveEnd": "2026-09-12T22:55:00.000Z",
                    "text": "TWY L BTN TWY A AND FIXED BASE OPR WEST RAMP CLSD",
                    "classification": "INTL",
                    "accountId": "KOKC",
                    "lastUpdated": "2025-09-12T22:56:00.000Z",
                    "icaoLocation": "KOKC",
                    "coordinates": "3524N09736W",
                    "radius": "005"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "09/050 NOTAMN\\nQ) KZFW/QMXLC////000/999/3524N09736W005\\nA) KOKC\\nB) 2509122255\\nC) 2609122255\\nE) TWY L BTN TWY A AND FIXED BASE OPR WEST RAMP CLSD"
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

    List<Notam> result = parser.parseNotams( json );

    assertEquals( 1, result.size() );

    Notam n = result.get( 0 );
    assertEquals( "NOTAM_1_77921818", n.getId() );
    assertEquals( "09/050", n.getNumber() );
    assertEquals( "N", n.getType() );
    assertEquals( Instant.parse( "2025-09-12T22:55:00.000Z" ), n.getIssued() );
    assertEquals( Instant.parse( "2025-09-12T22:55:00.000Z" ), n
        .getEffectiveStart() );
    assertEquals( Instant.parse( "2026-09-12T22:55:00.000Z" ), n
        .getEffectiveEnd() );
    assertEquals( "TWY L BTN TWY A AND FIXED BASE OPR WEST RAMP CLSD", n
        .getText() );
    assertEquals( "A", n.getSeries().orElseThrow() );
    assertEquals( "OKC", n.getLocation().orElseThrow() );
    assertEquals( "INTL", n.getClassification().orElseThrow() );
    assertEquals( "KOKC", n.getIcaoLocation().orElseThrow() );
    assertEquals( "3524N09736W", n.getCoordinates().orElseThrow() );
    assertEquals( "005", n.getRadius().orElseThrow() );
    assertEquals( "KZFW", n.getAffectedFIR().orElseThrow() );
    assertEquals( "QMXLC", n.getSelectionCode().orElseThrow() );
  }

  @Test
  void testQLineLessThanFiveParts()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "NOTAM_1_78870879",
                    "number": "A2007/25",
                    "type": "N",
                    "issued": "2025-12-04T06:10:00.000Z",
                    "effectiveStart": "2025-12-04T06:02:00.000Z",
                    "effectiveEnd": "2026-05-29T22:00:00.000Z",
                    "text": "OKC TWY A6 CLSD EXC ACFT WINGSPAN LESS THAN 118FT"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "A2007/25 NOTAMN\\nQ) KZFW/QMXLC/IV\\nA) KOKC\\nB) 2512040602\\nC) 2605292200\\nE) OKC TWY A6 CLSD EXC ACFT WINGSPAN LESS THAN 118FT"
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

    List<Notam> result = parser.parseNotams( json );

    Notam n = result.get( 0 );
    assertEquals( "KZFW", n.getAffectedFIR().orElseThrow() );
    assertEquals( "QMXLC", n.getSelectionCode().orElseThrow() );
    assertEquals( "IV", n.getTraffic().orElseThrow() );
    assertTrue( n.getPurpose().isEmpty() );
    assertTrue( n.getScope().isEmpty() );
  }

  @Test
  void parsesNotamWithoutQLine()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "NOTAM_1_77699840",
                    "series": "A",
                    "number": "A1470/25",
                    "type": "N",
                    "issued": "2025-08-21T14:46:00.000Z",
                    "affectedFIR": "KZFW",
                    "selectionCode": "QPICH",
                    "traffic": "I",
                    "purpose": "NBO",
                    "scope": "A",
                    "minimumFL": "000",
                    "maximumFL": "999",
                    "location": "OKC",
                    "effectiveStart": "2025-08-21T14:44:00.000Z",
                    "effectiveEnd": "2026-04-02T14:44:00.000Z",
                    "text": "OKC IAP OKC WILL ROGERS INTL",
                    "classification": "INTL",
                    "accountId": "KOKC",
                    "lastUpdated": "2025-08-21T14:46:00.000Z",
                    "icaoLocation": "KOKC"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "A1470/25 NOTAMN\\nA) KOKC\\nB) 2508211444\\nC) 2604021444 EST\\nE) OKC IAP OKC WILL ROGERS INTL,\\nOKLAHOMA CITY, OK.\\nILS RWY 35R (CAT II), AMDT 10F ...\\nS-ILS 35R CAT II NA EXCEPT FOR AIRCRAFT EQUIPPED WITH RADAR\\nALTIMETER. I-RGR INNER MARKER OUT OF SERVICE."
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

    List<Notam> result = parser.parseNotams( json );

    Notam n = result.get( 0 );
    assertEquals( "NOTAM_1_77699840", n.getId() );
    assertEquals( "A1470/25", n.getNumber() );
    assertTrue( n.getAffectedFIR().isEmpty() );
    assertTrue( n.getSelectionCode().isEmpty() );
    assertTrue( n.getTraffic().isEmpty() );
    assertTrue( n.getPurpose().isEmpty() );
    assertTrue( n.getScope().isEmpty() );
    assertTrue( n.getFormattedText().isPresent() );
  }

  @Test
  void parsesNotamWithOnlyLocalTranslation()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "NOTAM_1_78709389",
                    "number": "11/047",
                    "type": "N",
                    "issued": "2025-11-25T04:29:00.000Z",
                    "effectiveStart": "2025-11-25T04:29:00.000Z",
                    "effectiveEnd": "2026-04-30T22:00:00.000Z",
                    "text": "TWY A BTN TWY A4 AND TWY A6 WIP ADJ EAST SIDE LGTD AND BARRICADED",
                    "classification": "DOM",
                    "accountId": "OKC",
                    "icaoLocation": "KOKC",
                    "coordinates": "3523N09736W",
                    "radius": "5"
                  },
                  "notamTranslation": [
                    {
                      "type": "LOCAL_FORMAT",
                      "simpleText": "!OKC 11/047 OKC TWY A BTN TWY A4 AND TWY A6 WIP ADJ EAST SIDE LGTD AND BARRICADED 2511250429-2604302200"
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

    List<Notam> result = parser.parseNotams( json );

    assertEquals( 1, result.size() );

    Notam n = result.get( 0 );
    assertEquals( "NOTAM_1_78709389", n.getId() );
    assertEquals( "11/047", n.getNumber() );
    assertEquals( "N", n.getType() );
    assertEquals( Instant.parse( "2025-11-25T04:29:00.000Z" ), n.getIssued() );
    assertEquals( Instant.parse( "2025-11-25T04:29:00.000Z" ), n
        .getEffectiveStart() );
    assertEquals( Instant.parse( "2026-04-30T22:00:00.000Z" ), n
        .getEffectiveEnd() );
    assertEquals(
        "TWY A BTN TWY A4 AND TWY A6 WIP ADJ EAST SIDE LGTD AND BARRICADED", n
            .getText() );

    // No ICAO translation means no formattedText and no Q-line fields.
    assertTrue( n.getFormattedText().isEmpty() );
    assertTrue( n.getAffectedFIR().isEmpty() );
    assertTrue( n.getSelectionCode().isEmpty() );
    assertTrue( n.getTraffic().isEmpty() );
    assertTrue( n.getPurpose().isEmpty() );
    assertTrue( n.getScope().isEmpty() );
  }

  @Test
  void parsesFirstPartQLine()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "NOTAM_1_79625432",
                    "series": "A",
                    "number": "A0112/26",
                    "type": "N",
                    "issued": "2026-01-24T02:11:00.000Z",
                    "affectedFIR": "KZFW",
                    "selectionCode": "QLPAS",
                    "minimumFL": "000",
                    "maximumFL": "999",
                    "location": "OKC",
                    "effectiveStart": "2026-01-24T02:03:00.000Z",
                    "effectiveEnd": "2026-02-27T22:00:00.000Z",
                    "text": "OKC RWY 13 PAPI U/S",
                    "classification": "INTL",
                    "accountId": "KOKC",
                    "lastUpdated": "2026-01-24T02:11:00.000Z",
                    "icaoLocation": "KOKC",
                    "coordinates": "3524N09736W",
                    "radius": "005"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "A0112/26 NOTAMN\\nQ)\\nA) KOKC\\nB) 2601240203\\nC) 2602272200\\nE) OKC RWY 13 PAPI U/S"
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

    List<Notam> result = assertDoesNotThrow( () -> parser.parseNotams( json ) );

    Notam n = result.get( 0 );
    assertEquals( "NOTAM_1_79625432", n.getId() );
    assertEquals( "A0112/26", n.getNumber() );

    // Because the Q-line is only "Q)", substring(2).trim() becomes ""
    // and the builder turns blank optional strings to empty Optional.
    assertTrue( n.getAffectedFIR().isEmpty() );
    assertTrue( n.getSelectionCode().isEmpty() );
    assertTrue( n.getTraffic().isEmpty() );
    assertTrue( n.getPurpose().isEmpty() );
    assertTrue( n.getScope().isEmpty() );

    assertTrue( n.getFormattedText().isPresent() );
  }

  @Test
  void throwsRuntimeExceptionForInvalidJson()
  {
    String invalidJson = "{ this is not valid json }";

    RuntimeException ex = assertThrows( RuntimeException.class, () -> parser
        .parseNotams( invalidJson ) );

    assertTrue( ex.getMessage().contains( "Failed to parse NOTAM JSON" ) );
  }

  @Test
  void throwsIllegalArgumentExceptionForNullJsonResponse()
  {
    assertThrows( IllegalArgumentException.class, () -> parser.parseNotams(
        null ) );
  }

  @Test
  void emptyResultsFromEmptyItemsArray()
  {
    String json = """
        {
          "items": []
        }
        """;

    List<Notam> result = parser.parseNotams( json );

    assertTrue( result.isEmpty() );
  }

  @Test
  void skipsItemWhenNotamIsMissing()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                }
              }
            }
          ]
        }
        """;

    List<Notam> result = parser.parseNotams( json );

    assertTrue( result.isEmpty() );
  }

  @Test
  void parsesPermAsInstantMax()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "NOTAM_1_77921818",
                    "series": "A",
                    "number": "09/050",
                    "type": "N",
                    "issued": "2025-09-12T22:55:00.000Z",
                    "affectedFIR": "KZFW",
                    "selectionCode": "QMXLC",
                    "minimumFL": "000",
                    "maximumFL": "999",
                    "location": "OKC",
                    "effectiveStart": "PERM",
                    "effectiveEnd": "PERM",
                    "text": "TWY L BTN TWY A AND FIXED BASE OPR WEST RAMP CLSD",
                    "classification": "INTL",
                    "accountId": "KOKC",
                    "lastUpdated": "2025-09-12T22:56:00.000Z",
                    "icaoLocation": "KOKC",
                    "coordinates": "3524N09736W",
                    "radius": "005"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "09/050 NOTAMN\\nQ) KZFW/QMXLC////000/999/3524N09736W005\\nA) KOKC\\nB) 2509122255\\nC) PERM\\nE) TWY L BTN TWY A AND FIXED BASE OPR WEST RAMP CLSD"
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

    List<Notam> result = parser.parseNotams( json );
    assertEquals( Instant.MAX, result.get( 0 ).getEffectiveEnd() );
    assertEquals( Instant.MAX, result.get( 0 ).getEffectiveStart() );
  }

  @Test
  void skipsNotamWithInvalidTimestamp()
  {
    String json = """
        {
          "items": [
            {
              "properties": {
                "coreNOTAMData": {
                  "notam": {
                    "id": "NOTAM_1_77921818",
                    "series": "A",
                    "number": "09/050",
                    "type": "N",
                    "issued": "2025-09-12T22:55:00.000Z",
                    "affectedFIR": "KZFW",
                    "selectionCode": "QMXLC",
                    "minimumFL": "000",
                    "maximumFL": "999",
                    "location": "OKC",
                    "effectiveStart": "this is not an effective start time",
                    "effectiveEnd": "PERM",
                    "text": "TWY L BTN TWY A AND FIXED BASE OPR WEST RAMP CLSD",
                    "classification": "INTL",
                    "accountId": "KOKC",
                    "lastUpdated": "2025-09-12T22:56:00.000Z",
                    "icaoLocation": "KOKC",
                    "coordinates": "3524N09736W",
                    "radius": "005"
                  },
                  "notamTranslation": [
                    {
                      "type": "ICAO",
                      "formattedText": "09/050 NOTAMN\\nQ) KZFW/QMXLC////000/999/3524N09736W005\\nA) KOKC\\nB) 2509122255\\nC) PERM\\nE) TWY L BTN TWY A AND FIXED BASE OPR WEST RAMP CLSD"
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

    List<Notam> result = parser.parseNotams( json );
    assertTrue( result.isEmpty() ); // effectiveStart is a required field. If it cannot be parsed as a valid Instant, the Notam is skipped.
  }
}