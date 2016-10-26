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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import com.foreach.across.modules.web.thymeleaf.AcrossWebDialect;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementNodeBuilderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Creates Thymeleaf view resolver.
 */
@Configuration
@ConditionalOnProperty(value = "acrossWebModule.views.thymeleaf.enabled", matchIfMissing = true)
public class ThymeleafViewSupportConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebConfig.class );

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private AcrossWebModuleSettings settings;

	@Autowired
	private AcrossDevelopmentMode developmentMode;

	@Bean
	@Exposed
	@Qualifier("springTemplateEngine")
	public SpringTemplateEngine springTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.addDialect( new AcrossWebDialect() );
		engine.addTemplateResolver( templateResolver() );

		if ( developmentMode.isActive() ) {
			for ( ITemplateResolver resolver : developmentResolvers() ) {
				engine.addTemplateResolver( resolver );
			}
		}

		return engine;
	}

	@Bean
	@Exposed
	public ViewElementNodeBuilderRegistry thymeleafViewElementProcessorRegistry() {
		ViewElementNodeBuilderRegistry registry = new ViewElementNodeBuilderRegistry();
		//TODO: TH3
//		registry.registerNodeBuilder( TextViewElement.class, new TextViewElementThymeleafBuilder() );
//		registry.registerNodeBuilder( StandardViewElements.TEXT, new TextViewElementThymeleafBuilder() );
//		registry.registerNodeBuilder( ContainerViewElement.class, new ContainerViewElementThymeleafBuilder() );
//		registry.registerNodeBuilder( StandardViewElements.CONTAINER, new ContainerViewElementThymeleafBuilder() );
//		registry.registerNodeBuilder( ViewElementGenerator.class, new ViewElementGeneratorThymeleafBuilder() );
//		registry.registerNodeBuilder( StandardViewElements.GENERATOR, new ViewElementGeneratorThymeleafBuilder() );
//		registry.registerNodeBuilder( NodeViewElement.class, new HtmlViewElementThymeleafBuilder() );
//		registry.registerNodeBuilder( StandardViewElements.NODE, new HtmlViewElementThymeleafBuilder() );

		return registry;
	}

	@Bean
	@Exposed
	public ThymeleafViewResolver thymeleafViewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine( springTemplateEngine() );
		resolver.setOrder( 1 );
		resolver.setCharacterEncoding( "UTF-8" );
		resolver.setViewNames( new String[] {
				"th/*",
				ThymeleafViewResolver.REDIRECT_URL_PREFIX + "*",
				ThymeleafViewResolver.FORWARD_URL_PREFIX + "*" } );

		return resolver;
	}

	@SuppressWarnings("unchecked")
	private Collection<ITemplateResolver> developmentResolvers() {
		Collection<ITemplateResolver> resolvers = new LinkedList<>();

		if ( developmentMode.isActive() ) {
			Map<String, String> developmentViews = developmentMode.getDevelopmentLocations( "views" );
			developmentViews.putAll( settings.getDevelopmentViews() );

			for ( Map.Entry<String, String> views : developmentViews.entrySet() ) {
				String prefix = "file:" + views.getValue() + "/";
				String suffix = ".thtml";

				LOG.info( "Registering development Thymeleaf lookup for {} with physical path {}", views.getKey(),
				          views.getValue() );

				SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
				resolver.setOrder( 19 );
				resolver.setCharacterEncoding( "UTF-8" );
				resolver.setTemplateMode( "HTML5" );
				resolver.setCacheable( true );
				resolver.setCacheTTLMs( 1000L );
				resolver.setPrefix( prefix );
				resolver.setSuffix( suffix );

				applicationContext.getAutowireCapableBeanFactory().initializeBean( resolver,
				                                                                   "developmentResolver." + views
						                                                                   .getKey() );

				resolvers.add( resolver );
			}
		}

		return resolvers;
	}

	@Bean
	@Exposed
	public ITemplateResolver templateResolver() {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setCharacterEncoding( "UTF-8" );
		resolver.setTemplateMode( "HTML5" );
		resolver.setCacheable( true );

		if ( developmentMode.isActive() ) {
			resolver.setCacheTTLMs( 1000L );
		}

		resolver.setPrefix( "classpath:/views/" );
		resolver.setSuffix( ".thtml" );
		resolver.setOrder( 20 );

		return resolver;
	}
}
