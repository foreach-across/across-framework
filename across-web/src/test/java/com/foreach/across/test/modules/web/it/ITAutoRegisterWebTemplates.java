package com.foreach.across.test.modules.web.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.template.NamedWebTemplateProcessor;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITAutoRegisterWebTemplates.Config.class)
public class ITAutoRegisterWebTemplates
{
	@Autowired(required = false)
	private WebTemplateRegistry webTemplateRegistry;

	@Autowired
	private DefaultWebTemplate defaultWebTemplate;

	@Autowired
	private OtherWebTemplate otherWebTemplate;

	@Test
	public void namedWebTemplatesShouldBeRegistered() {
		assertNotNull( webTemplateRegistry );

		assertSame( defaultWebTemplate, webTemplateRegistry.get( "default" ) );
		assertSame( otherWebTemplate, webTemplateRegistry.get( "other" ) );
	}

	public static class DefaultWebTemplate implements NamedWebTemplateProcessor
	{
		@Override
		public String getName() {
			return "default";
		}

		@Override
		public void prepareForTemplate( HttpServletRequest request, HttpServletResponse response, Object handler ) {
		}

		@Override
		public void applyTemplate( HttpServletRequest request,
		                           HttpServletResponse response,
		                           Object handler,
		                           ModelAndView modelAndView ) {
		}
	}

	public static class OtherWebTemplate extends DefaultWebTemplate
	{
		@Override
		public String getName() {
			return "other";
		}
	}

	@EnableAcrossContext
	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			AcrossWebModule webModule = new AcrossWebModule();
			//webModule.setProperty( AcrossWebSettings.TEMPLATES_ENABLED, false );

			context.addModule( webModule );
		}

		@Bean
		DefaultWebTemplate defaultWebTemplate() {
			return new DefaultWebTemplate();
		}

		@Bean
		OtherWebTemplate otherWebTemplate() {
			return new OtherWebTemplate();
		}
	}
}
