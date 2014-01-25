package com.foreach.across.testweb.sub;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheableServiceTwoImpl implements CacheableServiceTwo
{
	private int number = 0;

	@Cacheable( "numberCache" )
	public int getNumber() {
		return getNumberNotCached();
	}

	public int getNumberNotCached() {
		return ++number;
	}
}
