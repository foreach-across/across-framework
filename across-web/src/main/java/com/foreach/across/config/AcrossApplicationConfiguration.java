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

/**
 * Configures dynamic application modules for an {@link AcrossApplication}.
 *
 * @author Arne Vandamme
 */
public class AcrossApplicationConfiguration implements ImportSelector
{
	@Override
	public String[] selectImports( AnnotationMetadata importingClassMetadata ) {
		if ( (Boolean) importingClassMetadata.getAnnotationAttributes( AcrossApplication.class.getName() )
		                                     .getOrDefault( "enableDynamicModules", true ) ) {
			return new String[] { AcrossDynamicModulesConfiguration.class.getName() };
		}
		return new String[0];
	}
}