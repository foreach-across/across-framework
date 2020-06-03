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
package test.development;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ActiveProfiles("dev")
@ContextConfiguration(classes = TestActivateDevModeByProfile.Config.class)
public class TestActivateDevModeByProfile
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void developmentModeShouldBeActive() {
		assertTrue( beanRegistry.getBeanOfType( AcrossDevelopmentMode.class ).isActive() );
	}

	@Test
	public void devModeOnlyBeanShouldBePresent() {
		assertTrue( beanRegistry.containsBean( "devModeOnlyBean" ) );
	}

	@Configuration
	@EnableAcrossContext
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new EmptyAcrossModule( "devMode", DevModeOnlyBeanConfiguration.class ) );
			assertTrue( context.isDevelopmentMode() );
		}
	}
}
