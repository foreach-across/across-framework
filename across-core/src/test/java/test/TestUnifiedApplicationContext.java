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
package test;

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestUnifiedApplicationContext
{
	@Test
	public void startAcrossContextInSingleApplicationContext() {
		AcrossApplicationContext applicationContext = new AcrossApplicationContext();
		applicationContext.register( AcrossApplicationConfig.class );
		applicationContext.refresh();
		applicationContext.start();

		assertModuleIndex( applicationContext, "moduleOne", -1 );
		assertModuleIndex( applicationContext, "moduleTwo", -1 );
		assertModuleIndex( applicationContext, "configOne", 1 );
		assertModuleIndex( applicationContext, "configTwo", 2 );
		assertModuleIndex( applicationContext, "componentOne", 1 );
		assertModuleIndex( applicationContext, "componentTwo", 2 );

		applicationContext.stop();
	}

	private void assertModuleIndex( AcrossApplicationContext applicationContext, String beanName, int expectedIndex ) {
		assertThat( applicationContext.getBeanDefinition( beanName ).getAttribute( "moduleIndex" ) ).isEqualTo( expectedIndex );
	}

	@EnableAcrossContext
	static class AcrossApplicationConfig
	{
		@Bean
		public EmptyAcrossModule moduleOne() {
			return new EmptyAcrossModule( "one", ConfigOne.class );
		}

		@Bean
		public EmptyAcrossModule moduleTwo() {
			EmptyAcrossModule two = new EmptyAcrossModule( "two", ConfigTwo.class );
			two.addRuntimeDependency( "one" );
			return two;
		}
	}

	@Configuration("configOne")
	static class ConfigOne
	{
		@Bean
		String componentOne() {
			return "one";
		}
	}

	@Configuration("configTwo")
	static class ConfigTwo
	{
		@Bean
		String componentTwo() {
			return "two";
		}
	}
}
