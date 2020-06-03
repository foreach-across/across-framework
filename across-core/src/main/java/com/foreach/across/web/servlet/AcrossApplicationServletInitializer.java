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
package com.foreach.across.web.servlet;

import com.foreach.across.AcrossApplicationRunnerBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * {@link SpringBootServletInitializer} extension which ensures an {@link com.foreach.across.AcrossApplicationRunner}
 * is being used instead of a regular {@link org.springframework.boot.SpringApplication}.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.AcrossApplicationRunner
 * @since 5.0.0
 */
@SuppressWarnings("unused")
public abstract class AcrossApplicationServletInitializer extends SpringBootServletInitializer
{
	@Override
	protected AcrossApplicationRunnerBuilder createSpringApplicationBuilder() {
		return new AcrossApplicationRunnerBuilder();
	}
}
