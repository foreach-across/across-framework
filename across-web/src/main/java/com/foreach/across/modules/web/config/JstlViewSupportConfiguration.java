package com.foreach.across.modules.web.config;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Creates a JSP/JSTL view resolver.
 */
@Configuration
public class JstlViewSupportConfiguration
{
	@Bean
	@Exposed
	public ViewResolver jstlViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix( "/WEB-INF/jsp/" );
		resolver.setSuffix( ".jsp" );
		resolver.setOrder( 2 );
		return resolver;
	}
}
