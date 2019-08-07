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
package com.foreach.across.core.config;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Responsible for importing {@link com.foreach.across.core.annotations.ModuleConfiguration} classes and other
 * configuration classes that have been added from other modules.
 * <p/>
 * Should be added to an Across module after the initial module configuration has been registered.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Configuration
@Import(ModuleConfigurationImportSelector.Registrar.class)
public class ModuleConfigurationImportSelector
{
	/**
	 * Returns the actual imports.
	 */
	static class Registrar implements ImportSelector, BeanFactoryAware
	{
		private AcrossModuleInfo moduleInfo;

		@Override
		public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
			moduleInfo = beanFactory.getBean( AcrossContextInfo.BEAN, AcrossContextInfo.class ).getModuleBeingBootstrapped();
		}

		@Override
		public String[] selectImports( AnnotationMetadata importingClassMetadata ) {
			val configurationsToImport = moduleInfo.getBootstrapConfiguration().getConfigurationsToImport();
			return configurationsToImport.isEmpty() ? new String[0] : configurationsToImport.toArray( new String[0] );
		}
	}
}
