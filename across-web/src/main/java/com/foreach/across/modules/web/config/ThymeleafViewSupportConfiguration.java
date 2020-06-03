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
import com.foreach.across.core.annotations.PostRefresh;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.MimeType;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.*;
import java.util.stream.Stream;

/**
 * Registers Thymeleaf support for all files with .html or .thtml extension.
 */
@Configuration
@ConditionalOnProperty(value = "across.web.views.thymeleaf.enabled", matchIfMissing = true)
@EnableConfigurationProperties(ThymeleafProperties.class)
public class ThymeleafViewSupportConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebConfiguration.class );

	private boolean refreshed = false;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private AcrossWebModuleSettings settings;

	@Autowired
	private AcrossDevelopmentMode developmentMode;

	@Autowired
	private ThymeleafProperties thymeleafProperties;

	@Bean
	@Exposed
	@Qualifier("springTemplateEngine")
	public SpringTemplateEngine springTemplateEngine( PrefixingPathRegistry prefixingPathRegistry ) {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setLinkBuilder( new PrefixingSupportingLinkBuilder( prefixingPathRegistry ) );
		return engine;
	}

	@PostRefresh
	public void refreshTemplateResolvers( SpringTemplateEngine engine ) {
		if ( !refreshed ) {
			refreshed = true;

			applicationContext.getBeansOfType( IDialect.class )
			                  .values()
			                  .stream()
			                  .filter( d -> !engine.getDialects().contains( d ) )
			                  .forEach( engine::addDialect );

			List<ITemplateResolver> resolvers = new ArrayList<>( developmentResolvers() );
			resolvers.addAll( applicationContext.getBeansOfType( ITemplateResolver.class ).values() );

			resolvers.stream()
			         .sorted( Comparator.comparingInt( ITemplateResolver::getOrder ) )
			         .filter( r -> !engine.getTemplateResolvers().contains( r ) )
			         .forEach( engine::addTemplateResolver );
		}
	}

	@Bean
	public AcrossWebDialect acrossWebDialect() {
		return new AcrossWebDialect();
	}

	@Bean
	public Java8TimeDialect java8TimeDialect() {
		return new Java8TimeDialect();
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
	public ViewElementAttributeConverter viewElementAttributeConverter( ObjectMapper objectMapper ) {
		return new DefaultViewElementAttributeConverter( objectMapper );
	}

	@Bean
	@Exposed
	@ConditionalOnMissingBean(name = "thymeleafViewResolver")
	public ThymeleafViewResolver thymeleafViewResolver( SpringTemplateEngine springTemplateEngine ) {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine( springTemplateEngine );
		resolver.setCharacterEncoding( thymeleafProperties.getEncoding().name() );
		resolver.setContentType(
				appendCharset( thymeleafProperties.getServlet().getContentType(), resolver.getCharacterEncoding() ) );
		resolver.setExcludedViewNames( thymeleafProperties.getExcludedViewNames() );
		resolver.setViewNames( thymeleafProperties.getViewNames() );
		// This resolver acts as a fallback resolver (e.g. like a
		// InternalResourceViewResolver) so it needs to have low precedence
		resolver.setOrder( Ordered.LOWEST_PRECEDENCE - 5 );
		resolver.setCache( thymeleafProperties.isCache() );
		return resolver;
	}

	private String appendCharset( MimeType type, String charset ) {
		if ( type.getCharset() != null ) {
			return type.toString();
		}
		LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
		parameters.put( "charset", charset );
		parameters.putAll( type.getParameters() );
		return new MimeType( type, parameters ).toString();
	}

	/*
	@Bean
	@Exposed
	public ThymeleafViewResolver thymeleafViewResolver( SpringTemplateEngine springTemplateEngine ) {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine( springTemplateEngine );
		resolver.setOrder( 1 );
		resolver.setCharacterEncoding( "UTF-8" );
		resolver.setContentType();
		resolver.setViewNames( new String[] {
				"th/*",
				ThymeleafViewResolver.REDIRECT_URL_PREFIX + "*",
				ThymeleafViewResolver.FORWARD_URL_PREFIX + "*" } );

		return resolver;
	}*/

	@SuppressWarnings("unchecked")
	private Collection<ITemplateResolver> developmentResolvers() {
		Collection<ITemplateResolver> resolvers = new LinkedList<>();

		if ( developmentMode.isActive() ) {
			Map<String, String> developmentViews = developmentMode.getDevelopmentLocations( "views" );
			developmentViews.putAll( settings.getDevelopmentViews() );

			developmentViews.forEach( ( resourceKey, path ) -> {
				Stream.of( ".html", ".thtml" )
				      .forEach( suffix -> {
					      String prefix = "file:" + path + "/";

					      LOG.info( "Registering development Thymeleaf lookup for {} with physical path {}", resourceKey, path );

					      SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
					      resolver.setOrder( 19 );
					      resolver.setCharacterEncoding( "UTF-8" );
					      resolver.setTemplateMode( "HTML" );
					      resolver.setCacheable( true );
					      resolver.setCacheTTLMs( 1000L );
					      resolver.setPrefix( prefix );
					      resolver.setSuffix( suffix );
					      resolver.setCheckExistence( true );

					      applicationContext.getAutowireCapableBeanFactory()
					                        .initializeBean( resolver, "developmentResolver." + resourceKey + suffix );

					      resolvers.add( resolver );
				      } );
			} );
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
		resolver.setSuffix( ".html" );
		resolver.setCheckExistence( true );
		resolver.setOrder( 20 );

		return resolver;
	}

	@Bean
	@Exposed
	public ITemplateResolver thtmlTemplateResolver() {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setCharacterEncoding( "UTF-8" );
		resolver.setTemplateMode( "HTML" );
		resolver.setCacheable( true );

		if ( developmentMode.isActive() ) {
			resolver.setCacheTTLMs( 1000L );
		}

		resolver.setPrefix( "classpath:/views/" );
		resolver.setSuffix( ".thtml" );
		resolver.setOrder( 21 );

		return resolver;
	}
}
