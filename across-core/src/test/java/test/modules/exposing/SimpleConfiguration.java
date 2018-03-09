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

package test.modules.exposing;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import test.modules.module1.SomeInterface;

import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class SimpleConfiguration
{
	@Bean
	public MyBean nonExposedBean() {
		return new MyBean();
	}

	@Bean(name = { "exposedBean", "aliasedExposedBean" })
	@Exposed
	public MyBean exposedBean() {
		return new MyBean();
	}

	@Bean
	public FactoryBean<SomeInterface> someInterfaceFactory() {
		return new FactoryBean<SomeInterface>()
		{
			@Override
			public SomeInterface getObject() throws Exception {
				return () -> 0;
			}

			@Override
			public Class<?> getObjectType() {
				return SomeInterface.class;
			}

			@Override
			public boolean isSingleton() {
				return false;
			}
		};
	}

	@Bean
	@Exposed
	public SomeOtherInterfaceFactory someOtherInterfaceBean() {
		return new SomeOtherInterfaceFactory();
	}

	@Bean
	@Exposed
	public AtomicReference<Integer> integerAtomicReference() {
		return new AtomicReference<>( 1 );
	}

	@Bean
	@Exposed
	public AtomicReference<String> stringAtomicReference() {
		return new AtomicReference<>( "value" );
	}

	/**
	 * Interface implemented by the factory itself.
	 */
	public interface SomeFactoryInterface extends FactoryBean<SomeOtherInterface>
	{

	}

	/**
	 * Interface implemented by the target type of SomeFactoryInterface
	 */
	public interface SomeOtherInterface
	{
	}

	public static class SomeOtherInterfaceFactory implements SomeFactoryInterface
	{
		private SomeOtherInterface otherInterface = new SomeOtherInterface()
		{
		};

		@Override
		public SomeOtherInterface getObject() throws Exception {
			return otherInterface;
		}

		@Override
		public Class<?> getObjectType() {
			return SomeOtherInterface.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}
	}
}
