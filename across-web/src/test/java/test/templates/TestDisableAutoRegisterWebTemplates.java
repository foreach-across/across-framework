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
import com.foreach.across.modules.web.AcrossWebModuleSettings;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestDisableAutoRegisterWebTemplates.Config.class)
public class TestDisableAutoRegisterWebTemplates
{
	@Autowired(required = false)
	private WebTemplateRegistry webTemplateRegistry;

	@Test
	public void namedWebTemplatesShouldBeRegistered() {
		assertNotNull( webTemplateRegistry );

		assertNull( webTemplateRegistry.get( "default" ) );
		assertNull( webTemplateRegistry.get( "other" ) );
	}

	@EnableAcrossContext
	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			AcrossWebModule webModule = new AcrossWebModule();
			webModule.setProperty( AcrossWebModuleSettings.TEMPLATES_AUTO_REGISTER, false );

			context.addModule( webModule );
		}

		@Bean
		TestAutoRegisterWebTemplates.DefaultWebTemplate defaultWebTemplate() {
			return new TestAutoRegisterWebTemplates.DefaultWebTemplate();
		}

		@Bean
		TestAutoRegisterWebTemplates.OtherWebTemplate otherWebTemplate() {
			return new TestAutoRegisterWebTemplates.OtherWebTemplate();
		}
	}
}
