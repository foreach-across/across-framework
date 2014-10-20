package com.foreach.across.test;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author niels
 * @since 20/10/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestAcrossCompositeCacheManager.Config.class)
public class TestAcrossCompositeCacheManager
{
	@Autowired
	private CacheManager cacheManager;

	@Test
	public void defaultCompositeCacheManagerShouldHaveBeenInitializedWithNoOp() {
		assertNotNull( cacheManager );
		Cache cache = cacheManager.getCache( "anyDummyCacheShouldWork" );
		assertNotNull( cache );
		String dummyCached = "imadummy";
		cache.put( dummyCached, new Object() );
		assertNull( cache.get( dummyCached ) );
	}

	@Configuration
	@AcrossTestConfiguration
	static class Config implements AcrossContextConfigurer
	{

		@Override
		public void configure( AcrossContext context ) {
		}
	}
}
