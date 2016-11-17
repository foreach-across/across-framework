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

package com.foreach.across.test.modules.exposing;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.test.modules.module1.SomeInterface;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
				return new SomeInterface()
				{
					@Override
					public int getOrder() {
						return 0;
					}
				};
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
	public AtomicReference<Integer> integerReference() {
		return new AtomicReference<>( 1 );
	}

	@Bean
	@Exposed
	public AtomicReference<String> stringReference() {
		return new AtomicReference<>( "value" );
	}
}
