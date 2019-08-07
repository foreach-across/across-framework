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
package test.context;

import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.support.BeanFactoryAnnotationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = { TestBeanFactoryAnnotationUtils.Config.class,
                                  TestBeanFactoryAnnotationUtils.OtherConfig.class })
public class TestBeanFactoryAnnotationUtils
{
	@Autowired
	private BeanFactory beanFactory;

	@Test
	public void notAConfigurableListableBeanFactory() {
		Assertions.assertThrows( IllegalArgumentException.class, () -> {
			BeanFactoryAnnotationUtils.findAnnotationOnBean( mock( BeanFactory.class ), "bean", Module.class );
		} );
	}

	@Test
	public void noAnnotation() {
		Optional<AnnotationAttributes> attributes
				= BeanFactoryAnnotationUtils.findAnnotationOnBean( beanFactory, "someBean", Module.class );

		assertNotNull( beanFactory.getBean( "someBean" ) );
		assertFalse( attributes.isPresent() );
	}

	@Test
	public void annotationOnClass() {
		assertAnnotationValue( "item", "two" );
	}

	@Test
	public void annotationOnFactoryMethod() {
		assertAnnotationValue( "otherBean", "one" );
	}

	@Test
	public void annotationOnFactoryClass() {
		assertAnnotationValue( "thirdBean", "three" );
	}

	private void assertAnnotationValue( String beanName, String annotationValue ) {
		assertNotNull( beanFactory.getBean( beanName ) );

		Optional<AnnotationAttributes> attributes
				= BeanFactoryAnnotationUtils.findAnnotationOnBean( beanFactory, beanName, Module.class );
		assertTrue( attributes.isPresent(), "Annotation not found on bean " + beanName );
		assertEquals( annotationValue, attributes.get().getString( "value" ) );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public String someBean() {
			return "someBean";
		}

		@Module("one")
		@Bean
		public String otherBean() {
			return "otherBean";
		}

		@Bean
		public Item item() {
			return new Item();
		}
	}

	@Module("three")
	protected static class OtherConfig
	{
		@Bean
		public String thirdBean() {
			return "thirdBean";
		}
	}

	@Module("two")
	protected static class Item
	{
	}
}
