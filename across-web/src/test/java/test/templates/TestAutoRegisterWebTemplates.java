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

package test.templates;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.template.NamedWebTemplateProcessor;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestAutoRegisterWebTemplates.Config.class)
public class TestAutoRegisterWebTemplates
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
