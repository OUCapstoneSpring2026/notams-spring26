package com.capstone.parsing;

import com.capstone.models.Notam;
import java.util.List;

public interface NotamParserInterface
{
    /**
     * Parses the provided JSON string and returns a list of Notam objects.
     *
     * @param jsonResponse
     *     the raw JSON response body from the Notam API
     * 
     * @return list of parsed Notam objects
     */
    List<Notam> parseNotams( String jsonResponse );
}