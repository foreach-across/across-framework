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
package com.foreach.across.modules.web.config;

import com.foreach.across.config.AcrossServletContextInitializer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.context.ModuleBeanOrderComparator;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for executing the {@link AcrossWebDynamicServletConfigurer} instances
 * found throughout the {@link AcrossContext}.
 *
 * @author Arne Vandamme
 */
@Configuration
public class DynamicServletConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( DynamicServletConfiguration.class );

	private final ServletContext servletContext;
	private final AcrossContextInfo contextInfo;

	@Autowired
	public DynamicServletConfiguration( ServletContext servletContext,
	                                    AcrossContextInfo contextInfo ) {
		this.servletContext = servletContext;
		this.contextInfo = contextInfo;
	}

	@Event
	public void registerServletsAndFilters( AcrossContextBootstrappedEvent bootstrappedEvent ) {
		Collection<InitializerEntry> initializers = retrieveInitializers();

		try {
			if ( !initializers.isEmpty() ) {
				LOG.info( "Found {} ServletContextInitializer beans found in the Across context", initializers.size() );

				for ( InitializerEntry i : initializers ) {
					LOG.debug( "ServletContextInitializer {}: {} - {}", i.moduleName, i.beanName, i.initializerBean );
					i.initializerBean.onStartup( servletContext );
				}
			}
		}
		catch ( ServletException se ) {
			throw new AcrossException( se );
		}
	}

	private Collection<InitializerEntry> retrieveInitializers() {
		List<ServletContextInitializer> initializerBeans = new ArrayList<>();
		Map<ServletContextInitializer, InitializerEntry> initializers = new HashMap<>();

		ModuleBeanOrderComparator comparator = new ModuleBeanOrderComparator();

		retrieveInitializersFromParent()
				.forEach( ( k, v ) -> {
					if ( !( v instanceof AcrossServletContextInitializer ) && !initializerBeans.contains( v ) ) {
						initializers.put( v, new InitializerEntry( contextInfo.getId(), k, v ) );
						initializerBeans.add( v );
						comparator.register( v, Ordered.HIGHEST_PRECEDENCE );
					}
				} );

		contextInfo.getModules().forEach(
				m -> m.getApplicationContext().getBeansOfType( ServletContextInitializer.class )
				      .forEach( ( k, v ) -> {
					      if ( !initializerBeans.contains( v ) ) {
						      initializers.put( v, new InitializerEntry( m.getName(), k, v ) );
						      initializerBeans.add( v );
						      comparator.register( v, m.getIndex() );
					      }
				      } )
		);

		comparator.sort( initializerBeans );

		return initializerBeans.stream()
		                       .map( initializers::get )
		                       .collect( Collectors.toList() );
	}

	private Map<String, ServletContextInitializer> retrieveInitializersFromParent() {
		ApplicationContext applicationContext = contextInfo.getApplicationContext();

		if ( hasEmbeddedApplicationContextAsParent( applicationContext ) ) {
			// An embedded application context will initialize its own
			return applicationContext.getBeansOfType( ServletContextInitializer.class );
		}

		return BeanFactoryUtils.beansOfTypeIncludingAncestors( applicationContext,
		                                                       ServletContextInitializer.class );
	}

	private boolean hasEmbeddedApplicationContextAsParent( ApplicationContext applicationContext ) {
		if ( applicationContext instanceof EmbeddedWebApplicationContext ) {
			return true;
		}

		ApplicationContext parent = applicationContext.getParent();
		return parent != null && hasEmbeddedApplicationContextAsParent( parent );
	}

	private static class InitializerEntry
	{
		final String moduleName, beanName;
		final ServletContextInitializer initializerBean;

		InitializerEntry( String moduleName,
		                  String beanName,
		                  ServletContextInitializer initializerBean ) {
			this.moduleName = moduleName;
			this.beanName = beanName;
			this.initializerBean = initializerBean;
		}
	}
}
