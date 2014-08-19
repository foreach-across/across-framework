package com.foreach.across.core.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.MBassadorEventPublisher;
import com.foreach.across.core.events.SpringContextRefreshedEventListener;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepository;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepositoryImpl;
import com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockConfiguration;
import com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

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
