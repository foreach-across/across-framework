package com.foreach.across.modules.ehcache.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Dummy configuration class that enables caching in any ApplicationContext where it is loaded.
 */
@Configuration
@EnableCaching
public class EhcacheClientModuleConfig
{
}
