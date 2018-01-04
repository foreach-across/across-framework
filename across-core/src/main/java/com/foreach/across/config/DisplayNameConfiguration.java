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
package com.foreach.across.config;

import com.foreach.across.core.AcrossContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author Arne Vandamme
 * @since 2.1.0
 */
@Configuration
class DisplayNameConfiguration implements AcrossContextConfigurer, ImportAware, EnvironmentAware
{
	private Environment environment;
	private AnnotationMetadata importMetadata;

	@Override
	public void setEnvironment( Environment environment ) {
		this.environment = environment;
	}

	@Override
	public void setImportMetadata( AnnotationMetadata importMetadata ) {
		this.importMetadata = importMetadata;
	}

	@Override
	public void configure( AcrossContext context ) {
		if ( context.getId().equals( context.getDisplayName() ) ) {
			context.setDisplayName( determineDisplayName() );
		}
	}

	private String determineDisplayName() {
		String displayName = (String) importMetadata.getAnnotationAttributes( AcrossApplication.class.getName() )
		                                            .getOrDefault( "displayName", "" );

		if ( StringUtils.isEmpty( displayName ) ) {
			RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver( environment, "across." );
			displayName = propertyResolver.getProperty( "displayName" );
		}

		if ( StringUtils.isEmpty( displayName ) ) {
			displayName = StringUtils.defaultIfEmpty( StringUtils.substringAfterLast( importMetadata.getClassName(), "." ),
			                                          importMetadata.getClassName() );
		}

		return displayName;
	}
}
