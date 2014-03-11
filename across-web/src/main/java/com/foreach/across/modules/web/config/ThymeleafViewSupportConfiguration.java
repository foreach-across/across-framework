package com.foreach.across.modules.web.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.AcrossWebModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Creates Thymeleaf view resolver.
 */
@Configuration
public class ThymeleafViewSupportConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebConfig.class );

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AcrossWebModule acrossWebModule;

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public SpringTemplateEngine springTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.addTemplateResolver( templateResolver() );

		if ( acrossWebModule.isDevelopmentMode() ) {
			for ( TemplateResolver resolver : developmentResolvers() ) {
				engine.addTemplateResolver( resolver );
			}
		}

		return engine;
	}

	@Bean
	@Exposed
	public ThymeleafViewResolver thymeleafViewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine( springTemplateEngine() );
		resolver.setOrder( 1 );
		resolver.setViewNames( new String[] { "th/*" } );

		return resolver;
	}

	private Collection<TemplateResolver> developmentResolvers() {
		Collection<TemplateResolver> resolvers = new LinkedList<TemplateResolver>();

		if ( acrossWebModule.isDevelopmentMode() ) {
			for ( Map.Entry<String, String> views : acrossWebModule.getDevelopmentViews().entrySet() ) {
				String prefix = "file:" + views.getValue();
				String suffix = ".thtml";

				LOG.debug( "Registering development Thymeleaf lookup with prefix {} and suffix {}", prefix, suffix );

				TemplateResolver resolver = new SpringResourceTemplateResolver();
				resolver.setOrder( 19 );
				resolver.setTemplateMode( "HTML5" );
				resolver.setCacheable( false );
				resolver.setPrefix( prefix );
				resolver.setSuffix( suffix );

				applicationContext.getAutowireCapableBeanFactory().initializeBean( resolver,
				                                                                  "developmentResolver." + views.getKey() );

				resolvers.add( resolver );
			}
		}

		return resolvers;
	}

	@Bean
	public TemplateResolver templateResolver() {
		TemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setTemplateMode( "HTML5" );
		resolver.setCacheable( !acrossWebModule.isDevelopmentMode() );
		resolver.setPrefix( "classpath:/views/" );
		resolver.setSuffix( ".thtml" );
		resolver.setOrder( 20 );

		return resolver;
	}
}
