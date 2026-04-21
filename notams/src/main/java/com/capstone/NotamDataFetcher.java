package com.capstone;

import java.io.IOException;

public interface NotamDataFetcher
{
	String fetchByIcao( String icaoCode )
			throws IOException, InterruptedException;

	String fetchByLocation( double latitude, double longitude, double radiusNm )
			throws IOException, InterruptedException;
}
