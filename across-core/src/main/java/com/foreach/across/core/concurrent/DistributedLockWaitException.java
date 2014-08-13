package com.foreach.across.core.concurrent;

public class DistributedLockWaitException extends RuntimeException
{
	public DistributedLockWaitException() {
		super();
	}

	public DistributedLockWaitException( String message ) {
		super( message );
	}

	public DistributedLockWaitException( String message, Throwable cause ) {
		super( message, cause );
	}

	public DistributedLockWaitException( Throwable cause ) {
		super( cause );
	}

	protected DistributedLockWaitException( String message,
	                                        Throwable cause,
	                                        boolean enableSuppression,
	                                        boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}
}
