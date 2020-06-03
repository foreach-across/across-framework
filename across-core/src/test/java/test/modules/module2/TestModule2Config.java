/*
 * Copyright 2019 the original author or authors
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

package test.modules.module2;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import test.modules.EventPubSub;

@Configuration
public class TestModule2Config
{
	@Bean
	@Exposed
	public ConstructedBeanModule2 constructedBeanModule2() {
		return new ConstructedBeanModule2( "helloFromModule2" );
	}

	@Bean
	@Exposed
	public SomeInterfaceImplTwo someInterfaceImplTwo() {
		return new SomeInterfaceImplTwo();
	}

	@Bean
	@Exposed
	public CustomEventHandlers customEventHandlers() {
		return new CustomEventHandlers();
	}

	@Bean
	public EventPubSub publisherModuleTwo( ApplicationEventPublisher eventPublisher ) {
		return new EventPubSub( "moduleTwo", eventPublisher );
	}
}
