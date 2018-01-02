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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicReference;

@Configuration("exposingConfiguration")
@Exposed
public class ExposingConfiguration
{
	@Autowired
	public void wireInternals( AtomicReference<Integer> integerReference, AtomicReference<String> stringReference ) {
		Assert.notNull( integerReference, "integerReference cannot be null" );
		Assert.notNull( stringReference, "stringReference cannot be null" );
	}

	@Bean
	public MyBean beanFromExposingConfiguration() {
		return new MyBean();
	}

	@Bean
	@Scope("prototype")
	public MyPrototypeBean myPrototypeBean() {
		return new MyPrototypeBean();
	}
}
