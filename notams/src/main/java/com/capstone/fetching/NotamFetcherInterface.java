package com.capstone.fetching;

import java.io.IOException;

public interface NotamFetcherInterface
{
	String fetchByIcao( String icaoCode )   throws IOException,
											InterruptedException;

	String fetchByCoordinates(  double latitude,
								double longitude,
								double radiusNm )   throws IOException,
													InterruptedException;
}
