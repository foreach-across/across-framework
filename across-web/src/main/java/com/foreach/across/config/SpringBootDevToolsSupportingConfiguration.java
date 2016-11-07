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
package com.foreach.across.config;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * Adds auto-configuration of Spring Boot devtools if it is on the class path..
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class SpringBootDevToolsSupportingConfiguration implements ImportSelector
{
	private static final String DEVTOOLS_CONFIGURATION
			= "org.springframework.boot.devtools.autoconfigure.LocalDevToolsAutoConfiguration";

	@Override
	public String[] selectImports( AnnotationMetadata importingClassMetadata ) {
		boolean devToolsPresent = ClassUtils.isPresent(
				DEVTOOLS_CONFIGURATION,
				Thread.currentThread().getContextClassLoader()
		);

		if ( devToolsPresent ) {
			return new String[] { DEVTOOLS_CONFIGURATION };
		}

		return new String[0];
	}
}
