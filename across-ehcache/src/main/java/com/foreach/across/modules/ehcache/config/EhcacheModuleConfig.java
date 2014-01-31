package com.foreach.across.modules.ehcache.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.ehcache.EhcacheModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Declares the cache manager instance that is shared between all modules.
 */
@Configuration
@Exposed
public class EhcacheModuleConfig
{
	@Autowired
	private EhcacheModule ehcacheModule;

	@Bean
	public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
		EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
		ehCacheManagerFactoryBean.setConfigLocation( ehcacheModule.getConfigLocation() );
		return ehCacheManagerFactoryBean;
	}

	@Bean
	public CacheManager cacheManager() {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		cacheManager.setCacheManager( ehCacheManagerFactoryBean().getObject() );
		return cacheManager;
	}
}
