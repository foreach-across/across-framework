package com.foreach.across.modules.hibernate.unitofwork;

import java.util.concurrent.Callable;

public class CallableUnitOfWork<V> implements Callable<V>
{
	private final UnitOfWorkFactory unitOfWorkFactory;
	private final Callable<V> callable;

	public CallableUnitOfWork( UnitOfWorkFactory unitOfWorkFactory, Callable<V> callable ) {
		this.unitOfWorkFactory = unitOfWorkFactory;
		this.callable = callable;
	}

	@SuppressWarnings( "SignatureDeclareThrowsException" )
	public V call() throws Exception {
		try {
			unitOfWorkFactory.start();

			return callable.call();
		}
		finally {
			unitOfWorkFactory.stop();
		}
	}
}
