package com.foreach.across.modules.spring.security.config;

import com.foreach.across.core.annotations.AcrossDepends;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.annotation.PostConstruct;

@Configuration
@AcrossDepends(required = "AcrossWebModule")
public class SpringSecurityThymeleafConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( SpringSecurityThymeleafConfiguration.class );

	private static final String CLASS_THYMELEAF_TEMPLATE_ENGINE = "org.thymeleaf.spring4.SpringTemplateEngine";
	private static final String CLASS_SPRING_SECURITY_DIALECT =
			"org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect";

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void registerDialect() {
		if ( shouldRegisterThymeleafDialect() ) {
			LOG.debug( "Registering Thymeleaf Spring security dialect" );

			Object springTemplateEngine = applicationContext.getBean( "springTemplateEngine" );

			if ( springTemplateEngine instanceof SpringTemplateEngine ) {
				( (SpringTemplateEngine) springTemplateEngine ).addDialect( new SpringSecurityDialect() );
				LOG.debug( "Thymeleaf Spring security dialect registered successfully." );
			}
			else {
				LOG.warn(
						"Unable to register Thymeleaf Spring security dialect as bean springTemplateEngine is not of the right type." );
			}
		}
	}

	private boolean shouldRegisterThymeleafDialect() {
		if ( applicationContext.containsBean( "springTemplateEngine" ) ) {
			ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();

			if ( ClassUtils.isPresent( CLASS_THYMELEAF_TEMPLATE_ENGINE, threadClassLoader ) && ClassUtils.isPresent(
					CLASS_SPRING_SECURITY_DIALECT, threadClassLoader ) ) {
				return true;
			}

		}

		return false;
	}
}

