package com.foreach.across.modules.ehcache.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.ehcache.EhcacheModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the cache manager instance that is shared between all modules.
 */
@Configuration
@Exposed
public class EhcacheModuleConfig
{
	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private EhcacheModule ehcacheModule;

	@Bean
	public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
		EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
		ehCacheManagerFactoryBean.setConfigLocation( ehcacheModule.getConfigLocation() );
		return ehCacheManagerFactoryBean;
	}

	@Bean
	public CacheManager cacheManager( net.sf.ehcache.CacheManager ehCacheCacheManager ) {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		cacheManager.setCacheManager( ehCacheCacheManager );
		return cacheManager;
	}
}
