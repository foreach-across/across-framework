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
package test.spring;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.AnnotationMetadata;
import test.scan.overriding.MyComponent;

import java.util.ArrayDeque;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestImportSelectorBehaviour
{
	@Test
	public void testWithImportConfigurationOrdering() {
		AcrossListableBeanFactory beanFactory = new AcrossListableBeanFactory();
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext( beanFactory );
		applicationContext.addBeanFactoryPostProcessor( new CustomConfigurationPostProcessor() );
		applicationContext.refresh();
		applicationContext.start();

		assertThat( beanFactory.getBean( MyComponent.class ) ).isNotNull();
		assertThat( beanFactory.getBeanDefinition( "one" ).getAttribute( "moduleIndex" ) ).isEqualTo( 1 );
		assertThat( beanFactory.getBeanDefinition( "two" ).getAttribute( "moduleIndex" ) ).isEqualTo( 2 );
		assertThat( beanFactory.getBeanDefinition( "three" ).getAttribute( "moduleIndex" ) ).isEqualTo( 3 );
		assertThat( beanFactory.getBeanDefinition( "myComponent" ).getAttribute( "moduleIndex" ) ).isEqualTo( 3 );

		applicationContext.close();
	}

	private static class CustomConfigurationPostProcessor
			implements BeanDefinitionRegistryPostProcessor, PriorityOrdered
	{

		@Override
		public int getOrder() {
			// Must happen before the ConfigurationClassPostProcessor is created
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory )
				throws BeansException {
		}

		@Override
		public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry )
				throws BeansException {
			register( registry );
			configureConfigurationClassPostProcessor( registry );
		}

		private void register( BeanDefinitionRegistry registry ) {
		}

		private void configureConfigurationClassPostProcessor(
				BeanDefinitionRegistry registry ) {
			try {
				RootBeanDefinition beanDefinition = (RootBeanDefinition) registry.getBeanDefinition(
						AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME );
				beanDefinition.setBeanClass( MyConfigurationClassPostProcessor.class );

			}
			catch ( NoSuchBeanDefinitionException ex ) {
			}
		}
	}

	@Import(ModuleConfigImporter.class)
	static class AcrossModuleConfig
	{
	}

	static class ModuleConfigImporter implements ImportSelector
	{
		@Override
		public String[] selectImports( AnnotationMetadata importingClassMetadata ) {
			return new String[] { MyConfigurationClassPostProcessor.classesNames.pop().getName() };
		}
	}

	static class MyConfigurationClassPostProcessor extends ConfigurationClassPostProcessor
	{
		int executed = 0;
		private static ArrayDeque<Class<?>> classesNames = new ArrayDeque<>( Arrays.asList( Config1.class, Config2.class, Config3.class ) );

		@Override
		public void processConfigBeanDefinitions( BeanDefinitionRegistry registry ) {
			super.processConfigBeanDefinitions( registry );

			while ( !classesNames.isEmpty() ) {
				if ( registry.containsBeanDefinition( "my-module-config" ) ) {
					registry.removeBeanDefinition( "my-module-config" );
				}
				//Class<?> clazz = classesNames.pop();
				registry.registerBeanDefinition( "my-module-config", new AnnotatedGenericBeanDefinition( AcrossModuleConfig.class ) );
				( (AcrossListableBeanFactory) registry ).setModuleIndex( ++executed );
				super.processConfigBeanDefinitions( registry );
			}
		}
	}

	@Configuration
	static class Config1
	{
		@Bean
		String one() {
			return "one";
		}
	}

	@Configuration
	static class Config2
	{
		@Bean
		String two() {
			return "two";
		}
	}

	@Configuration
	@ComponentScan(basePackageClasses = MyComponent.class)
	static class Config3
	{
		@Bean
		String three() {
			return "three";
		}
	}
}
