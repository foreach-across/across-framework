package com.foreach.across.core.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

import java.util.*;

/**
 * This implementation mimics a {@link org.springframework.cache.support.CompositeCacheManager CompositeCacheManager},
 * but overrides its default behaviour regarding the noop cache fallback as well as the internal ordering of the cache managers.
 * It keeps its cache manager delegates in a {@link java.util.LinkedList LinkedList} and, if not overruled by the
 * {@link com.foreach.across.core.AcrossContext#disableNoOpCacheManager AcrossContext}, puts a {@link org.springframework.cache.support.NoOpCacheManager NoOpCacheManager}
 * as a last resort caching implementation.
 *
 * @author niels
 * @since 14/10/2014
 */
public class AcrossCompositeCacheManager implements CacheManager
{

	private final boolean noOpCacheManagerEnabled;

	private LinkedList<CacheManager> cacheManagers = new LinkedList<>();

	public AcrossCompositeCacheManager( boolean disableNoOpCacheManager ) {
		this.noOpCacheManagerEnabled = !disableNoOpCacheManager;
		if ( this.noOpCacheManagerEnabled ) {
			this.cacheManagers.addLast( new NoOpCacheManager() );
		}
	}

	/**
	 * Construct an AcrossCompositeCacheManager from the given delegate CacheManagers.
	 * @param cacheManagers the CacheManagers to delegate to
	 */
	public AcrossCompositeCacheManager( boolean disableNoOpCacheManager, CacheManager... cacheManagers) {
		this( disableNoOpCacheManager );
		setCacheManagers( Arrays.asList( cacheManagers ) );
	}


	/**
	 * Specify the CacheManagers to delegate to.
	 */
	public void setCacheManagers(Collection<CacheManager> cacheManagers) {
		if ( this.noOpCacheManagerEnabled ) {
			this.cacheManagers.addAll( this.cacheManagers.size() - 1, cacheManagers );
		} else {
			this.cacheManagers.addAll( cacheManagers );
		}
	}

	@Override
	public Cache getCache( String name ) {
		for (CacheManager cacheManager : this.cacheManagers) {
			Cache cache = cacheManager.getCache(name);
			if (cache != null) {
				return cache;
			}
		}
		return null;
	}

	@Override
	public Collection<String> getCacheNames() {
		Set<String> names = new LinkedHashSet<>();
		for (CacheManager manager : this.cacheManagers) {
			names.addAll(manager.getCacheNames());
		}
		return Collections.unmodifiableSet(names);
	}
}
