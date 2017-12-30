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

package com.foreach.across.test.modules.module1;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.test.modules.EventPubSub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Exposed
@Configuration
@EnableAspectJAutoProxy
public class TestModule1Config
{
	@Bean
	public ConstructedBeanModule1 constructedBeanModule1() {
		return new ConstructedBeanModule1Impl( "helloFromModule1" );
	}

	@Bean(name = "refreshable")
	@Refreshable
	public ConstructedBeanModule1 refreshableConstructedBeanModule1() {
		return new ConstructedBeanModule1Impl( "helloFromModule1-refreshable" );
	}

	@Bean
	public SomeInterfaceImplOne someInterfaceImplOne() {
		return new SomeInterfaceImplOne();
	}

	@Bean
	public Interceptor interceptor() {
		return new Interceptor();
	}

	@Bean
	public EventPubSub publisherModuleOne( AcrossEventPublisher eventPublisher ) {
		return new EventPubSub( "moduleOne", eventPublisher );
	}
}
