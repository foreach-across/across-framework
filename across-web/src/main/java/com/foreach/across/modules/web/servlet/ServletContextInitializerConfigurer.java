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
package com.foreach.across.modules.web.servlet;

import com.foreach.across.core.context.info.AcrossContextInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Ensures that filters and servlets defined in modules will be registered, if dynamic configuration is allowed.
 * Scans for {@link org.springframework.boot.context.embedded.ServletContextInitializer} beans inside each module,
 * or the context itself (without including parent initializers).
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class ServletContextInitializerConfigurer extends AcrossWebDynamicServletConfigurer
{
	private static final Logger LOG = LoggerFactory.getLogger( ServletContextInitializerConfigurer.class );

	private final AcrossContextInfo contextInfo;

	@Autowired
	public ServletContextInitializerConfigurer( AcrossContextInfo contextInfo ) {
		this.contextInfo = contextInfo;
	}

	@Override
	protected void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException {
		Collection<InitializerEntry> initializers = retrieveInitializers();

		if ( !initializers.isEmpty() ) {
			LOG.info( "Found {} ServletContextInitializer beans found in the Across context", initializers.size() );

			for ( InitializerEntry i : initializers ) {
				LOG.debug( "ServletContextInitializer {}: {} - {}", i.moduleName, i.beanName, i.initializerBean );
				i.initializerBean.onStartup( servletContext );
			}
		}
		else {
			LOG.debug( "No ServletContextInitializer beans found in the Across context" );
		}
	}

	@Override
	protected void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException {
		Collection<InitializerEntry> initializers = retrieveInitializers();

		if ( !initializers.isEmpty() ) {
			LOG.error(
					"Found {} ServletContextInitializer beans in the Across context - but dynamic configuration of the ServletContext is not allowed",
					initializers.size()
			);

			if ( LOG.isDebugEnabled() ) {
				initializers.forEach(
						i -> LOG.debug( "ServletContextInitializer {}: {} - {}",
						                i.moduleName, i.beanName, i.initializerBean )
				);
			}
		}
	}

	private Collection<InitializerEntry> retrieveInitializers() {
		Set<ServletContextInitializer> initializerBeans = new HashSet<>();
		Collection<InitializerEntry> initializers = new ArrayDeque<>();

		contextInfo.getApplicationContext().getBeansOfType( ServletContextInitializer.class )
		           .forEach( ( k, v ) -> {
			           if ( !initializerBeans.contains( v ) ) {
				           initializers.add( new InitializerEntry( contextInfo.getId(), k, v ) );
				           initializerBeans.add( v );
			           }
		           } );

		contextInfo.getModules().forEach(
				m -> m.getApplicationContext().getBeansOfType( ServletContextInitializer.class )
				      .forEach( ( k, v ) -> {
					      if ( !initializerBeans.contains( v ) ) {
						      initializers.add( new InitializerEntry( m.getName(), k, v ) );
						      initializerBeans.add( v );
					      }
				      } )
		);

		return initializers;
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
