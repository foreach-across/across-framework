package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebViewSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;

/**
 * Creates an AcrossContext with
 */
@Configuration
@Import(AcrossTestContextConfiguration.class)
public class AcrossTestWebContextConfiguration implements AcrossTestContextConfigurer
{
	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	void verifyWebApplicationContext() {
		Assert.isTrue( applicationContext instanceof WebApplicationContext,
		               "The test ApplicationContext is not of the required WebApplicationContext type.  " +
				               "Try annotation your test class with @WebAppConfiguration." );
	}

	@Override
	public void configure( AcrossContext context ) {
		context.addModule( acrossWebModule() );
	}

	@Bean
	public AcrossWebModule acrossWebModule() {
		AcrossWebModule acrossWebModule = new AcrossWebModule();
		acrossWebModule.setSupportViews( AcrossWebViewSupport.THYMELEAF );

		return acrossWebModule;
	}
}
