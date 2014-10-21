/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.core.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.cache.AcrossCompositeCacheManager;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.MBassadorEventPublisher;
import com.foreach.across.core.events.SpringContextRefreshedEventListener;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepository;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepositoryImpl;
import com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockConfiguration;
import com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;

/**
 * Installs the common beans that are always available.
 */
@Configuration
public class AcrossConfig
{

	@Bean
	public AcrossEventPublisher eventPublisher() {
		return new MBassadorEventPublisher();
	}

	@Bean
	public SpringContextRefreshedEventListener refreshedEventListener() {
		return new SpringContextRefreshedEventListener();
	}

	@Bean
	// TODO fix this, because it doesn't seem to work
	// @Lazy
	// TODO currently this does not work either, but we hardcoded it in the AcrossBootstrapper
	@Exposed
	public CacheManager cacheManager( AcrossContext acrossContext ) {
		return new AcrossCompositeCacheManager( acrossContext.isDisableNoOpCacheManager() );
	}

	@Bean
	@Lazy
	@Primary
	public AcrossDevelopmentMode acrossDevelopmentMode() {
		return new AcrossDevelopmentMode();
	}

	@Bean
	@Lazy
	@DependsOn({ "sqlBasedDistributedLockManager" })
	public DistributedLockRepository distributedLockRepository( SqlBasedDistributedLockManager sqlBasedDistributedLockManager ) {
		return new DistributedLockRepositoryImpl( sqlBasedDistributedLockManager );
	}

	@Bean(destroyMethod = "close")
	@Lazy
	@DependsOn({ "acrossCoreSchemaInstaller", AcrossContext.DATASOURCE })
	public SqlBasedDistributedLockManager sqlBasedDistributedLockManager( DataSource acrossDataSource ) {
		if ( acrossDataSource == null ) {
			throw new AcrossException(
					"Unable to create the DistributedLockRepository because there is no DataSource configured.  " +
							"A DataSource is required to install the core schema."
			);
		}

		return new SqlBasedDistributedLockManager( acrossDataSource, sqlBasedDistributedLockConfiguration() );
	}

	@Bean
	public SqlBasedDistributedLockConfiguration sqlBasedDistributedLockConfiguration() {
		return new SqlBasedDistributedLockConfiguration( "across_locks" );
	}
}
