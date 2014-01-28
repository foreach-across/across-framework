package com.foreach.across.testweb.other;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheableServiceOneImpl implements CacheableServiceOne
{
	private int number = 0;

	@Cacheable( "numberCache" )
	public int getNumber() {
		return getNumberNotCached();
	}

	@Cacheable( "numberCache" )
	public int getNumberWithId( int id ) {
		return getNumberNotCached();
	}

	public int getNumberNotCached() {
		return ++number;
	}
}
