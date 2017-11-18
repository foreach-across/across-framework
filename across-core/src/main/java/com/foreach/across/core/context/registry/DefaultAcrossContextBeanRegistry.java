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

package com.foreach.across.core.context.registry;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.ModuleBeanOrderComparator;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.info.ConfigurableAcrossContextInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.util.*;

public class DefaultAcrossContextBeanRegistry implements AcrossContextBeanRegistry
{
	private ConfigurableAcrossContextInfo contextInfo;

	public DefaultAcrossContextBeanRegistry( ConfigurableAcrossContextInfo contextInfo ) {
		setContextInfo( contextInfo );
	}

	protected DefaultAcrossContextBeanRegistry() {
	}

	protected void setContextInfo( ConfigurableAcrossContextInfo contextInfo ) {
		this.contextInfo = contextInfo;
	}

	@Override
	public String getContextId() {
		return contextInfo.getId();
	}

	@Override
	public String getFactoryName() {
		return contextInfo.getId() + "@" + AcrossContextBeanRegistry.BEAN;
	}

	@Override
	public boolean containsBean( String beanName ) {
		return contextInfo.getApplicationContext().containsBean( beanName );
	}

	@Override
	public boolean moduleContainsLocalBean( String moduleName, String beanName ) {
		return contextInfo.getConfigurableModuleInfo( moduleName ).getApplicationContext().containsLocalBean(
				beanName );
	}

	@Override
	public Object getBean( String beanName ) {
		return contextInfo.getApplicationContext().getBean( beanName );
	}

	@Override
	public <T> T getBean( String beanName, Class<T> requiredType ) {
		return contextInfo.getApplicationContext().getBean( beanName, requiredType );
	}

	@Override
	public Class<?> getBeanType( String beanName ) {
		return contextInfo.getApplicationContext().getType( beanName );
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBeanFromModule( String moduleName, String beanName ) {
		if ( StringUtils.isEmpty( moduleName ) ) {
			return (T) getBean( beanName );
		}

		return (T) contextInfo.getConfigurableModuleInfo( moduleName ).getApplicationContext().getBean( beanName );
	}

	@Override
	public Class<?> getBeanTypeFromModule( String moduleName, String beanName ) {
		if ( StringUtils.isEmpty( moduleName ) ) {
			return getBeanType( beanName );
		}

		return contextInfo.getConfigurableModuleInfo( moduleName ).getApplicationContext().getType( beanName );
	}

	@Override
	public <T> T getBeanOfType( Class<T> requiredType ) {
		return contextInfo.getApplicationContext().getBean( requiredType );
	}

	@Override
	public <T> T getBeanOfTypeFromModule( String moduleName, Class<T> requiredType ) {
		if ( StringUtils.isEmpty( moduleName ) ) {
			return null;
		}

		return contextInfo.getConfigurableModuleInfo( moduleName ).getApplicationContext().getBean( requiredType );
	}

	@Override
	public <T> Optional<T> findBeanOfTypeFromModule( String moduleName, Class<T> requiredType ) {
		try {
			return Optional.ofNullable( getBeanOfTypeFromModule( moduleName, requiredType ) );
		}
		catch ( BeansException be ) {
			return Optional.empty();
		}
	}

	@Override
	public <T> List<T> getBeansOfType( Class<T> beanClass ) {
		return getBeansOfType( beanClass, false );
	}

	@Override
	public <T> Map<String, T> getBeansOfTypeAsMap( Class<T> beanClass ) {
		return getBeansOfTypeAsMap( beanClass, false );
	}

	@Override
	public <T> List<T> getBeansOfType( Class<T> beanClass, boolean includeModuleInternals ) {
		return getBeansOfType( ResolvableType.forClass( beanClass ), includeModuleInternals );
	}

	@Override
	public <T> Map<String, T> getBeansOfTypeAsMap( Class<T> beanClass, boolean includeModuleInternals ) {
		return getBeansOfTypeAsMap( ResolvableType.forClass( beanClass ), includeModuleInternals );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getBeansOfType( ResolvableType resolvableType, boolean includeModuleInternals ) {
		return new ArrayList<>(
				(Collection<T>) getBeansOfTypeAsMap( resolvableType, includeModuleInternals ).values()
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getBeansOfTypeAsMap( ResolvableType resolvableType, boolean includeModuleInternals ) {
		Set<T> beans = new LinkedHashSet<>();
		ModuleBeanOrderComparator comparator = new ModuleBeanOrderComparator();

		Map<T, String> beanNames = new HashMap<>();

		DependencyDescriptor dd = new ResolvableTypeDescriptor( resolvableType );
		ResolvableTypeAutowireCandidateResolver resolver = new ResolvableTypeAutowireCandidateResolver();
		AcrossListableBeanFactory beanFactory = beanFactory( contextInfo.getApplicationContext() );

		resolver.setBeanFactory( beanFactory );

		for ( String beanName : BeanFactoryUtils.beansOfTypeIncludingAncestors( beanFactory, resolvableType.getRawClass() ).keySet() ) {

			if ( beanFactory.isAutowireCandidate( beanName, dd, resolver ) ) {
				boolean isExposedNonSingleton = !beanFactory.isSingleton( beanName )
						&& beanFactory.getBeanDefinition( beanName ) instanceof ExposedBeanDefinition;

				// only include exposed non-singletons if we don't include module internals, else we will end up
				// with double entries
				if ( !includeModuleInternals || !isExposedNonSingleton ) {
					Object bean = beanFactory.getBean( beanName );
					comparator.register( bean, Ordered.HIGHEST_PRECEDENCE );

					beans.add( (T) bean );
					beanNames.put( (T) bean, beanName );
				}
			}
		}

		if ( includeModuleInternals ) {
			for ( AcrossModuleInfo module : contextInfo.getModules() ) {
				beanFactory = beanFactory( module.getApplicationContext() );

				if ( beanFactory != null ) {
					resolver.setBeanFactory( beanFactory );

					for ( String beanName : beanFactory.getBeansOfType( resolvableType.getRawClass() ).keySet() ) {
						if ( beanFactory.isAutowireCandidate( beanName, dd, resolver ) ) {
							if ( !( beanFactory.getBeanDefinition( beanName ) instanceof ExposedBeanDefinition ) ) {
								Object bean = beanFactory.getBean( beanName );
								comparator.register( bean, module.getIndex() );

								beans.add( (T) bean );
								beanNames.put( (T) bean, module.getName() + ":" + beanName );
							}
						}
					}
				}
			}
		}

		List<T> beanList = new ArrayList<>( beans );
		comparator.sort( beanList );

		LinkedHashMap<String, T> beansMap = new LinkedHashMap<>();
		for ( T bean : beanList ) {
			beansMap.put( beanNames.get( bean ), bean );
		}

		return beansMap;
	}

	private AcrossListableBeanFactory beanFactory( ApplicationContext applicationContext ) {
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) applicationContext;
		return ctx != null ? (AcrossListableBeanFactory) ctx.getBeanFactory() : null;
	}

	protected static class ResolvableTypeDescriptor extends DependencyDescriptor
	{
		private static final Field DUMMY_FIELD;

		private final ResolvableType resolvableType;

		static {
			try {
				DUMMY_FIELD = ResolvableTypeDescriptor.class.getDeclaredField( "resolvableType" );
			}
			catch ( NoSuchFieldException nsfe ) {
				throw new RuntimeException( nsfe );
			}
		}

		ResolvableTypeDescriptor( ResolvableType resolvableType ) {
			super( DUMMY_FIELD, false );
			this.resolvableType = resolvableType;
		}

		@Override
		public ResolvableType getResolvableType() {
			return resolvableType;
		}

		@Override
		public boolean fallbackMatchAllowed() {
			return false;
		}

		@Override
		public Class<?> getDependencyType() {
			return resolvableType.getRawClass();
		}

		@Override
		public boolean equals( Object other ) {
			return super.equals( other );
		}

	}
}
