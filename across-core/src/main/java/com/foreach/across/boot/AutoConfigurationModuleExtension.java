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
package com.foreach.across.boot;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Collection;

/**
 * Holds the list of auto-configuration classes that have been moved to this module.
 * The {@link Importer} can be used to inject them into a current {@link org.springframework.context.ApplicationContext}.
 *
 * @author Arne Vandamme
 * @see ExtendModuleAutoConfiguration
 * @since 3.0.0
 */
class AutoConfigurationModuleExtension
{
	static final String BEAN = AutoConfigurationModuleExtension.class.getSimpleName();

	@Getter
	private final String[] classNames;

	AutoConfigurationModuleExtension( Collection<String> classNames ) {
		this.classNames = classNames.toArray( new String[0] );
	}

	@Import(Importer.class)
	static class Registrar
	{
	}

	static class Importer implements DeferredImportSelector, BeanFactoryAware
	{
		private AutoConfigurationModuleExtension extension;

		@Override
		public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
			extension = (AutoConfigurationModuleExtension) ( (AcrossListableBeanFactory) beanFactory ).getSingleton( AutoConfigurationModuleExtension.BEAN );
		}

		@Override
		public String[] selectImports( AnnotationMetadata importingClassMetadata ) {
			return extension != null ? extension.getClassNames() : new String[0];
		}
	}
}
