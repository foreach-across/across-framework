package com.foreach.across.core;

public class AcrossException extends RuntimeException
{
	public AcrossException() {
	}

	public AcrossException( String message ) {
		super( message );
	}

	public AcrossException( String message, Throwable cause ) {
		super( message, cause );
	}

	public AcrossException( Throwable cause ) {
		super( cause );
	}

	public AcrossException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}
}
