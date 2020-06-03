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
package test.exposing;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.ExposedBeanDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that exposed factory beans keep type information as detailed as possible.
 *
 * @author Arne Vandamme
 * @since 3.2.0
 */
@SuppressWarnings("all")
public class TestExposingFactoryBeans
{
	private AcrossContext context;

	@BeforeEach
	public void startAcrossContext() {
		context = new AcrossContext();
		context.addModule( new EmptyAcrossModule( "FactoryBeanHolder", BeanConfiguration.class ) );
		context.addModule( new EmptyAcrossModule( "OtherAcrossModule" ) );
		context.bootstrap();
	}

	@AfterEach
	public void destroyAcrossContext() {
		context.shutdown();
	}

	@Test
	public void originalFactoryBean() {
		AcrossListableBeanFactory originalBeans = (AcrossListableBeanFactory) AcrossContextUtils.getContextInfo( context )
		                                                                                        .getModuleInfo( "FactoryBeanHolder" )
		                                                                                        .getApplicationContext()
		                                                                                        .getAutowireCapableBeanFactory();

		BeanDefinition originalBean = originalBeans.getBeanDefinition( "myFactoryBean" );
		assertThat( originalBean ).isNotNull().isNotInstanceOf( ExposedBeanDefinition.class );
		assertThat( originalBeans.getBean( "myFactoryBean" ) ).isEqualTo( "myBean" );

		assertThat( originalBeans.containsBeanDefinition( "&myFactoryBean" ) ).isFalse();
		assertThat( originalBeans.getBean( "&myFactoryBean" ) ).isInstanceOf( MyFactoryBeanImpl.class );

		assertThat( originalBeans.isFactoryBean( "&myFactoryBean" ) ).isTrue();
		assertThat( originalBeans.isFactoryBean( "myFactoryBean" ) ).isTrue();
		assertThat( originalBeans.getType( "myFactoryBean" ) ).isEqualTo( String.class );
		assertThat( originalBeans.getType( "&myFactoryBean" ) ).isEqualTo( MyFactoryBeanImpl.class );

		assertThat( originalBeans.isTypeMatch( "myFactoryBean", String.class ) ).isTrue();
		assertThat( originalBeans.isTypeMatch( "&myFactoryBean", MyFactoryBeanImpl.class ) ).isTrue();

		String[] beanNames = originalBeans.getBeanNamesForType( FactoryBean.class, false, false );
		assertThat( beanNames ).containsExactly( "&myFactoryBean" );
		beanNames = originalBeans.getBeanNamesForType( String.class, false, false );
		assertThat( beanNames ).containsExactly( "myFactoryBean" );

	}

	@Test
	public void exposedFactoryBean() {
		AcrossListableBeanFactory exposedBeans = (AcrossListableBeanFactory) AcrossContextUtils.getContextInfo( context )
		                                                                                       .getModuleInfo( "OtherAcrossModule" )
		                                                                                       .getApplicationContext()
		                                                                                       .getAutowireCapableBeanFactory();

		BeanDefinition exposedBean = exposedBeans.getBeanDefinition( "myFactoryBean" );
		assertThat( exposedBean ).isNotNull().isInstanceOf( ExposedBeanDefinition.class );
		assertThat( exposedBeans.getBean( "myFactoryBean" ) ).isEqualTo( "myBean" );

		assertThat( exposedBeans.containsBeanDefinition( "&myFactoryBean" ) ).isFalse();
		assertThat( exposedBeans.getBean( "&myFactoryBean" ) ).isInstanceOf( MyFactoryBeanImpl.class );

		assertThat( exposedBeans.isFactoryBean( "&myFactoryBean" ) ).isTrue();
		assertThat( exposedBeans.isFactoryBean( "myFactoryBean" ) ).isTrue();
		assertThat( exposedBeans.getType( "myFactoryBean" ) ).isEqualTo( String.class );
		assertThat( exposedBeans.getType( "&myFactoryBean" ) ).isEqualTo( MyFactoryBeanImpl.class );

		assertThat( exposedBeans.isTypeMatch( "myFactoryBean", String.class ) ).isTrue();
		assertThat( exposedBeans.isTypeMatch( "&myFactoryBean", MyFactoryBeanImpl.class ) ).isTrue();

		String[] beanNames = exposedBeans.getBeanNamesForType( FactoryBean.class, false, false );
		assertThat( beanNames ).containsExactly( "&myFactoryBean" );
		beanNames = exposedBeans.getBeanNamesForType( String.class, false, false );
		assertThat( beanNames ).containsExactly( "myFactoryBean" );

	}

	@Configuration
	static class BeanConfiguration
	{
		@Bean
		@Exposed
		public MyFactoryBean myFactoryBean() {
			return new MyFactoryBeanImpl();
		}
	}

	interface MyFactoryBean extends FactoryBean<String>
	{
	}

	static class MyFactoryBeanImpl extends AbstractFactoryBean<String> implements MyFactoryBean
	{
		@Override
		public Class<?> getObjectType() {
			return String.class;
		}

		@Override
		protected String createInstance() throws Exception {
			return "myBean";
		}
	}
}
