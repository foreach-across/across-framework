package com.foreach.across.testweb.other;

public interface CacheableServiceOne
{
	int getNumber();

	int getNumberWithId( int id );

	int getNumberNotCached();
}
