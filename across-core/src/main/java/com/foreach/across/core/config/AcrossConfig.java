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

import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.cache.AcrossCompositeCacheManager;
import com.foreach.across.core.context.support.AcrossContextOrderedMessageSource;
import com.foreach.across.core.context.support.MessageSourceBuilder;
import com.foreach.across.core.convert.StringToDateConverter;
import com.foreach.across.core.convert.StringToDateTimeConverter;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import com.foreach.across.core.events.AcrossContextApplicationEventMulticaster;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.DefaultAcrossEventPublisher;
import com.foreach.across.core.events.SpringContextRefreshedEventListener;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepository;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepositoryImpl;
import com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockConfiguration;
import com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Installs the common Across Context beans that are always available.
 */
@Slf4j
@Configuration
@Import(ValidationAutoConfiguration.class)
public class AcrossConfig
{
	/**
	 * @return central Across event publisher
	 */
	@Primary
	@Bean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
	public AcrossContextApplicationEventMulticaster acrossEventMulticaster( BeanFactory beanFactory ) {
		return new AcrossContextApplicationEventMulticaster( beanFactory );
	}

	@Bean
	@Exposed
	public AcrossEventPublisher acrossEventPublisher( ApplicationContext applicationContext ) {
		return new DefaultAcrossEventPublisher( applicationContext );
	}

	@Bean
	public SpringContextRefreshedEventListener refreshedEventListener() {
		return new SpringContextRefreshedEventListener();
	}

	@Bean(name = AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME)
	public MessageSource messageSource( ApplicationContext applicationContext ) {
		ApplicationContext parent = applicationContext.getParent();
		HierarchicalMessageSource endpoint = null;

		// Put the parent message sources *before* the AcrossContext source,
		// allows for easy message overriding in the configured application
		if ( parent != null && parent.containsLocalBean( AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME ) ) {
			endpoint = MessageSourceBuilder.findHighestAvailableMessageSource(
					parent.getBean( AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME, MessageSource.class )
			);
		}

		return new AcrossContextOrderedMessageSource( endpoint );
	}

	@Bean
	@Lazy
	@Exposed
	public AcrossCompositeCacheManager cacheManager( AcrossContext acrossContext ) {
		return new AcrossCompositeCacheManager( acrossContext.isDisableNoOpCacheManager() );
	}

	/**
	 * A default ConversionService gets created if no other is provided through the parent context.
	 */
	@Bean(name = ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME)
	@Exposed
	@ConditionalOnMissingBean(name = ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME)
	public DefaultFormattingConversionService conversionService() {
		LOG.info( "Creating a default ConversionService as no valid bean '{}' is present",
		          ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME );

		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		conversionService.addConverter( defaultDateTimeConverter( conversionService ) );

		return conversionService;
	}

	@Bean
	public StringToDateConverter defaultDateConverter() {
		return new StringToDateConverter();
	}

	@Bean
	public StringToDateTimeConverter defaultDateTimeConverter( ConversionService conversionService ) {
		return new StringToDateTimeConverter( conversionService );
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
	public DistributedLockRepository distributedLockRepository( SqlBasedDistributedLockManager sqlBasedDistributedLockManager, AcrossContext acrossContext ) {
		String ownerId =
				StringUtils.substring( acrossContext.getDisplayName() + "@" + StringUtils.defaultString( getHostNameFromServer(), "unknown-host" ), 0, 80 )
						+ "[" + UUID.randomUUID().toString() + "]";

		LOG.info( "Creating distributed lock owner id: {}", ownerId );
		return new DistributedLockRepositoryImpl( sqlBasedDistributedLockManager, ownerId );
	}

	@Bean(destroyMethod = "close")
	@Lazy
	@DependsOn({ "acrossCoreSchemaInstaller", AcrossContext.DATASOURCE })
	public SqlBasedDistributedLockManager sqlBasedDistributedLockManager( DataSource acrossDataSource ) {
		if ( acrossDataSource == null ) {
			throw new AcrossConfigurationException(
					"Unable to create the DistributedLockRepository because there is no DataSource configured.",
					"Define a datasource for Across. If you have multiple datasources mark one as @Primary or name the bean 'acrossDataSource'."
			);
		}

		return new SqlBasedDistributedLockManager(
				acrossDataSource,
				sqlBasedDistributedLockConfiguration( schemaConfigurationHolder() )
		);
	}

	@Bean
	@Lazy
	public SqlBasedDistributedLockConfiguration sqlBasedDistributedLockConfiguration(
			CoreSchemaConfigurationHolder schemaConfigurationHolder ) {
		String tablePrefix = "";
		String defaultSchema = schemaConfigurationHolder.getDefaultSchema();
		if ( !StringUtils.isBlank( defaultSchema ) ) {
			tablePrefix = defaultSchema + ".";
		}
		return new SqlBasedDistributedLockConfiguration( tablePrefix + "across_locks" );
	}

	@Bean
	@Lazy
	public CoreSchemaConfigurationHolder schemaConfigurationHolder() {
		return new CoreSchemaConfigurationHolder();
	}

	private String getHostNameFromServer() {
		try {
			String result = InetAddress.getLocalHost().getHostName();
			if ( StringUtils.isNotEmpty( result ) ) {
				return result;
			}
		}
		catch ( UnknownHostException e ) {
			// failed;  try alternate means.
		}

		// try environment properties.
		String host = System.getenv( "COMPUTERNAME" );
		if ( host != null ) {
			return host;
		}
		host = System.getenv( "HOSTNAME" );
		if ( host != null ) {
			return host;
		}

		// undetermined.
		return null;
	}
}
