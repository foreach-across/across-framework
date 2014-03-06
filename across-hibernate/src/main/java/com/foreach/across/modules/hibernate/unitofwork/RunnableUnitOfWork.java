package com.foreach.across.modules.hibernate.unitofwork;

public class RunnableUnitOfWork implements Runnable
{
	private final UnitOfWorkFactory unitOfWorkFactory;
	private final Runnable runnable;

	public RunnableUnitOfWork( UnitOfWorkFactory unitOfWorkFactory, Runnable runnable ) {
		this.unitOfWorkFactory = unitOfWorkFactory;
		this.runnable = runnable;
	}

	public void run() {
		try {
			unitOfWorkFactory.start();

			runnable.run();
		}
		finally {
			unitOfWorkFactory.stop();
		}
	}
}
