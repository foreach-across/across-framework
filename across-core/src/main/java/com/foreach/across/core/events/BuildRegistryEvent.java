package com.foreach.across.core.events;

/**
 * Fired when an instance representing some form of registry is being built.
 */
public class BuildRegistryEvent<T> implements AcrossEvent
{
	private T registry;

	public BuildRegistryEvent( T registry ) {
		this.registry = registry;
	}

	public T getRegistry() {
		return registry;
	}
}
