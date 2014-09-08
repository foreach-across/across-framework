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

package com.foreach.across.modules.web.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.bootstrap.AnnotationConfigBootstrapApplicationContextFactory;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.WebApplicationContext;

/**
 * Creates WebApplicationContext versions of the standard ApplicationContext.
 */
public class WebBootstrapApplicationContextFactory extends AnnotationConfigBootstrapApplicationContextFactory
{
	@Override
	public AbstractApplicationContext createApplicationContext() {
		return new AcrossWebApplicationContext();
	}

	@Override
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            ApplicationContext parentApplicationContext ) {
		if ( parentApplicationContext == null || parentApplicationContext instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parentApplicationContext;
			AcrossWebApplicationContext applicationContext = new AcrossWebApplicationContext();
			applicationContext.setDisplayName( "[" + across.getId() + "]" );

			if ( parentApplicationContext != null ) {
				applicationContext.setParent( parentApplicationContext );
				applicationContext.setServletContext( parentWebContext.getServletContext() );

				if ( parentApplicationContext.getEnvironment() instanceof ConfigurableEnvironment ) {
					applicationContext.getEnvironment().merge(
							(ConfigurableEnvironment) parentApplicationContext.getEnvironment() );
				}
			}

			return applicationContext;
		}

		return super.createApplicationContext( across, parentApplicationContext );
	}

	@Override
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            ModuleBootstrapConfig moduleBootstrapConfig,
	                                                            AcrossApplicationContextHolder parentContext ) {
		if ( parentContext.getApplicationContext() instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parentContext.getApplicationContext();
			AcrossWebApplicationContext child = new AcrossWebApplicationContext();

			child.setDisplayName( moduleBootstrapConfig.getModuleName() );
			child.setServletContext( parentWebContext.getServletContext() );
			child.setParent( parentContext.getApplicationContext() );
			child.getEnvironment().merge( parentContext.getApplicationContext().getEnvironment() );

			return child;
		}

		return super.createApplicationContext( across, moduleBootstrapConfig, parentContext );
	}
}
