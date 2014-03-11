package com.foreach.across.testweb.sub;

import com.foreach.across.modules.web.ui.AbstractWebUiContextHandlerInterceptor;
import com.foreach.across.testweb.SpecificUiContext;
import com.foreach.across.testweb.SpecificUiContextImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class TestWebWebConfig extends WebMvcConfigurerAdapter
{
	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( contextInterceptorHandler() );
	}

	@Bean
	public AbstractWebUiContextHandlerInterceptor contextInterceptorHandler() {
		return new AbstractWebUiContextHandlerInterceptor<SpecificUiContext>()
		{
			@Override
			protected SpecificUiContext createWebUiContext( HttpServletRequest request,
			                                                HttpServletResponse response,
			                                                MessageSource messageSource ) {
				return new SpecificUiContextImpl( request, response );
			}
		};
	}
}
