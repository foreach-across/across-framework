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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import com.foreach.across.modules.web.thymeleaf.AcrossWebDialect;
import com.foreach.across.modules.web.thymeleaf.PrefixingSupportingLinkBuilder;
import com.foreach.across.modules.web.ui.DefaultViewElementAttributeConverter;
import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.ViewElementAttributeConverter;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.ViewElementGenerator;
import com.foreach.across.modules.web.ui.elements.thymeleaf.ContainerViewElementModelWriter;
import com.foreach.across.modules.web.ui.elements.thymeleaf.HtmlViewElementModelWriter;
import com.foreach.across.modules.web.ui.elements.thymeleaf.TextViewElementModelWriter;
import com.foreach.across.modules.web.ui.elements.thymeleaf.ViewElementGeneratorModelWriter;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriterRegistry;
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
	public SpringTemplateEngine springTemplateEngine( PrefixingPathRegistry prefixingPathRegistry ) {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.addDialect( new AcrossWebDialect() );
		engine.addTemplateResolver( templateResolver() );
		engine.setLinkBuilder( new PrefixingSupportingLinkBuilder( prefixingPathRegistry ) );

		if ( developmentMode.isActive() ) {
			for ( ITemplateResolver resolver : developmentResolvers() ) {
				engine.addTemplateResolver( resolver );
			}
		}

		return engine;
	}

	@Bean
	@Exposed
	public ViewElementModelWriterRegistry thymeleafViewElementProcessorRegistry() {
		ViewElementModelWriterRegistry registry = new ViewElementModelWriterRegistry();

		registry.registerModelWriter( TextViewElement.class, new TextViewElementModelWriter() );
		registry.registerModelWriter( StandardViewElements.TEXT, new TextViewElementModelWriter() );
		registry.registerModelWriter( ContainerViewElement.class, new ContainerViewElementModelWriter() );
		registry.registerModelWriter( StandardViewElements.CONTAINER, new ContainerViewElementModelWriter() );
		registry.registerModelWriter( ViewElementGenerator.class, new ViewElementGeneratorModelWriter() );
		registry.registerModelWriter( StandardViewElements.GENERATOR, new ViewElementGeneratorModelWriter() );
		registry.registerModelWriter( NodeViewElement.class, new HtmlViewElementModelWriter() );
		registry.registerModelWriter( StandardViewElements.NODE, new HtmlViewElementModelWriter() );

		return registry;
	}

	@Bean
	@Exposed
	public ViewElementAttributeConverter viewElementAttributeConverter() {
		return new DefaultViewElementAttributeConverter( viewElementAttributeObjectMapper() );
	}

	@Bean(ViewElementAttributeConverter.OBJECT_MAPPER_BEAN)
	@Exposed
	public ObjectMapper viewElementAttributeObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	@Exposed
	public ThymeleafViewResolver thymeleafViewResolver( SpringTemplateEngine springTemplateEngine ) {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine( springTemplateEngine );
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
				resolver.setTemplateMode( "HTML" );
				resolver.setCacheable( true );
				resolver.setCacheTTLMs( 1000L );
				resolver.setPrefix( prefix );
				resolver.setSuffix( suffix );
				resolver.setCheckExistence( true );

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
		resolver.setTemplateMode( "HTML" );
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
