package com.foreach.across.testweb.sub;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheableServiceTwoImpl implements CacheableServiceTwo
{
	private int number = 0;

	@Cacheable("numberCache")
	public int getOtherNumber() {
		return getOtherNumberNotCached();
	}

	@Cacheable("numberCache")
	public int getNumberWithId( int id ) {
		return getOtherNumberNotCached();
	}

	public int getOtherNumberNotCached() {
		return ++number;
	}
}
