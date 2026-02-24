package com.capstone.exceptions;

public class NotamApiException extends RuntimeException
{
	private final int statusCode;

	public NotamApiException( int statusCode, String message )
	{
		super( message );
		this.statusCode = statusCode;
	}

	public int getStatusCode()
	{
		return statusCode;
	}
}
