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
package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.stream.Stream;

/**
 * Responsible for adding the configurations of the current module to the bean factory.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Import(AcrossModuleConfigurationRegistrar.Imports.class)
class AcrossModuleConfigurationRegistrar
{
	/**
	 * Registers the different imports and annotated classes.
	 */
	static class Imports implements ImportSelector, BeanFactoryAware
	{
		private AcrossModuleInfo currentModule;

		@Override
		public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
			currentModule = beanFactory.getBean( AcrossContextInfo.BEAN, AcrossContextInfo.class ).getModuleBeingBootstrapped();
		}

		@Override
		public String[] selectImports( AnnotationMetadata importingClassMetadata ) {
			return currentModule.getBootstrapConfiguration()
			                    .getApplicationContextConfigurers()
			                    .stream()
			                    .flatMap( cfg -> Stream.of( cfg.annotatedClasses() ) )
			                    .map( Class::getName )
			                    .distinct()
			                    .toArray( String[]::new );
		}
	}
}
