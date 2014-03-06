package com.foreach.across.modules.hibernate.unitofwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * <p>A unit of work factory facilitates managing one or more sessions, without having
 * to explicitly know about the sessions themselves.</p>
 * <p>A UnitOfWorkFactory can be used to simulate OpenSessionInView behavior outside
 * a web request context.</p>
 *
 * @see org.springframework.orm.hibernate4.support.OpenSessionInViewInterceptor
 */
public interface UnitOfWorkFactory
{
	static final Logger LOG = LoggerFactory.getLogger( UnitOfWorkFactory.class );

	/**
	 * Wraps a Runnable into a unit of work.
	 *
	 * @param runnable Original runnable instance.
	 * @return Wrapped Runnable.
	 */
	Runnable create( Runnable runnable );

	/**
	 * Wraps a Callable into a unit of work.
	 *
	 * @param callable Original callable instance.
	 * @param <T>      the result type of the method call
	 * @return Wrapped Callable.
	 */
	<T> Callable<T> create( Callable<T> callable );

	/**
	 * Starts a new unit of work: opens all Sessions.
	 */
	void start();

	/**
	 * Stops the unit of work: closes all Sessions.
	 */
	void stop();

	/**
	 * When called, this will close and reopen all Sessions attached
	 * to the current thread.
	 */
	void restart();
}
