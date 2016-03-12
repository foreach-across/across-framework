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

import com.foreach.across.core.AcrossException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Configuration that automatically adds a {@link AcrossDynamicModulesConfigurer} that uses the importing class
 * as a base for the dynamic modules.
 *
 * @author Arne Vandamme
 * @see AcrossDynamicModulesConfigurer
 * @see AcrossApplication
 * @since 1.1.2
 */
@Configuration
public class AcrossDynamicModulesConfiguration extends AcrossDynamicModulesConfigurer implements ImportAware
{
	@Override
	public void setImportMetadata( AnnotationMetadata importMetadata ) {
		try {
			setApplicationClass( Class.forName( importMetadata.getClassName() ) );
		}
		catch ( ClassNotFoundException cnfe ) {
			throw new AcrossException( "Unable to configure dynamic application modules", cnfe );
		}
	}
}
