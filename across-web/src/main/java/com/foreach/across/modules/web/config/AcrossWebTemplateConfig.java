package com.foreach.across.modules.web.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import com.foreach.across.modules.web.template.NamedWebTemplateProcessor;
import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Collection;

/**
 * Configures web template support with automatic registration of named web templates.
 */
@Configuration
@AcrossCondition("${" + AcrossWebModuleSettings.TEMPLATES_ENABLED + ":true}")
public class AcrossWebTemplateConfig extends WebMvcConfigurerAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebTemplateConfig.class );

	@Autowired
	private Environment environment;

	@Autowired
	private AcrossContext context;

	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( webTemplateInterceptor() );
	}

	@Bean
	@Exposed
	public WebTemplateRegistry webTemplateRegistry() {
		return new WebTemplateRegistry();
	}

	@Bean
	public WebTemplateInterceptor webTemplateInterceptor() {
		return new WebTemplateInterceptor( webTemplateRegistry() );
	}

	@PostRefresh
	public void registerNamedWebTemplateProcessors() {
		boolean autoRegister = environment.getProperty( AcrossWebModuleSettings.TEMPLATES_AUTO_REGISTER, Boolean.class,
		                                                true );

		if ( autoRegister ) {
			LOG.info( "Scanning modules for NamedWebTemplateProcessor instances" );

			Collection<NamedWebTemplateProcessor> namedProcessors = AcrossContextUtils.getBeansOfType( context,
			                                                                                           NamedWebTemplateProcessor.class,
			                                                                                           true );
			WebTemplateRegistry registry = webTemplateRegistry();

			for ( NamedWebTemplateProcessor webTemplateProcessor : namedProcessors ) {
				registry.register( webTemplateProcessor );
			}
		}
	}
}
