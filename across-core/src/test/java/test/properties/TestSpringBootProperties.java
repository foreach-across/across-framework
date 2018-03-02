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
package test.properties;

import com.foreach.across.config.EnableAcrossContext;
import test.properties.boot.SpringBootPropertiesModule;
import test.properties.boot.config.BeanWithProps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.properties.boot.SpringBootPropertiesModule;
import test.properties.boot.config.BeanWithProps;

import static org.junit.Assert.assertEquals;

/**
 * TODO: original bootProperties have been changed to boot.properties for Spring Boot 2 support - check behaviour
 *
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@SpringBootTest(classes = TestSpringBootProperties.Config.class, properties = {
		"boot.properties.directValue=parent",
		"boot.properties.yaml.two=parentTwo"
})
@ActiveProfiles("props")
public class TestSpringBootProperties
{
	@Autowired
	private BeanWithProps beanWithProps;

	@Test
	public void verifyPropertyValues() {
		assertEquals( "code", beanWithProps.getDirectValue() );
		assertEquals( "yamlOne", beanWithProps.getYamlOne() );
		assertEquals( "parentTwo", beanWithProps.getYamlTwo() );
	}

	@Configuration
	@EnableAcrossContext
	protected static class Config
	{
		@Bean
		public SpringBootPropertiesModule springBootPropertiesModule() {
			SpringBootPropertiesModule springBootPropertiesModule = new SpringBootPropertiesModule();
			springBootPropertiesModule.setProperty( "boot.properties.directValue", "code" );
			return springBootPropertiesModule;
		}
	}
}
