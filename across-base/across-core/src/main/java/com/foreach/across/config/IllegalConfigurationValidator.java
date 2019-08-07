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

import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Validation class that checks wrong use of @Configuration classes, either in a module
 * or on the application configuration level. This can be used to provide hints to the end user
 * for standard Spring (Boot) configurations that require a different approach in an Across setup.
 * <p/>
 * Disable the configuration validation by setting the property {@code across.configuration.validate} to {@code false}.
 * <p/>
 * This validator also loads its configuration settings from the 'META-INF/across.configuration' file.
 * The key <strong>com.foreach.across.IllegalConfiguration</strong> can list illegal entries of the form:
 * {@code CLASS_NAME(->MODULE_MATCHER)(:MESSAGE_KEY)}.
 * <ul>
 * <li>CLASS_NAME: name of the type that should not be present as bean (usually a `@Configuration` class)</li>
 * <li>MODULE_MATCHER: a format specifying how the class should be checked. <br/><br/>One or more specific rules can be set, separated
 * by a |. Any rule can be reversed by adding a !. Examples:
 * <ul>
 * <li>AcrossContext: can not be present on the context</li>
 * <li>AcrossModule: can not be present on any module</li>
 * <li>MODULE_NAME: can not be present on the module with MODULE_NAME</li>
 * <li>!AcrossContext: can be used on the context</li>
 * <li>!AcrossModule: can be used on any module</li>
 * <li>!MODULE_NAME: can be used on the module with MODULE_NAME</li>
 * </ul>
 * <br/><br/>
 * If no matcher specified the type can not be used on the context level, but it can be used inside a module. As soon as a single matcher
 * is specified, the default settings are that the type can not be used anywhere except in the rules specified.
 * </li>
 * <li>MESSAGE_KEY: key to be used for looking up an additional description and action message. When a key is specified, it is
 * expected that 'META-INF/across.configuration' contains entries of the form: <strong>com.foreach.across.IllegalConfiguration[MESSAGE_KEY].description</strong>
 * and <strong>com.foreach.across.IllegalConfiguration[MESSAGE_KEY].description</strong>. These will be added to the thrown execption.</li>
 * </ul>
 *
 * @author Arne Vandamme
 * @see com.foreach.across.core.diagnostics.AcrossConfigurationExceptionFailureAnalyzer
 * @see AcrossConfiguration
 * @since 3.0.0
 */
@ConditionalOnProperty(value = "across.configuration.validate", havingValue = "true", matchIfMissing = true)
@Component("across.illegalConfigurationValidator")
@Import(IllegalConfigurationValidator.IllegalConfigurationDetector.class)
public class IllegalConfigurationValidator implements AcrossBootstrapConfigurer, BeanClassLoaderAware
{
	private final List<IllegalConfigurationEntry> illegalEntries = new ArrayList<>();

	@Override
	public void setBeanClassLoader( ClassLoader classLoader ) {
		val entries = AcrossConfiguration.get( classLoader ).getIllegalConfigurations();

		entries.forEach( configuration -> {
			configuration.getConfigurations()
			             .forEach( classEntry -> {
				             Class<?> clazz = resolveClass( classEntry.getClassName(), classLoader );

				             if ( clazz != null ) {
					             Predicate<AcrossModuleInfo> matcher = new ModuleMatcher( classEntry.getAllowed(), classEntry.getIllegal() );
					             illegalEntries.add( new IllegalConfigurationEntry( clazz, matcher, configuration ) );
				             }
			             } );
		} );
	}

	@Override
	public void configureModule( ModuleBootstrapConfig moduleConfiguration ) {
		moduleConfiguration.addApplicationContextConfigurer( true, IllegalConfigurationDetector.class );
	}

	static class ModuleMatcher implements Predicate<AcrossModuleInfo>
	{
		//private final String[] allowedModules
		private final boolean illegalOnApplication;
		private final boolean illegalOnModule;
		private final Set<String> allowedModules = new HashSet<>();
		private final Set<String> illegalModules = new HashSet<>();

		ModuleMatcher( String allowed, String illegal ) {
			Boolean illegalOnApplication = null;
			Boolean illegalOnModule = null;

			String a = StringUtils.defaultString( allowed, "" );
			String i = StringUtils.defaultString( illegal, "" );

			if ( i.isEmpty() && a.isEmpty() ) {
				illegalOnApplication = true;
				illegalOnModule = false;
			}
			else if ( i.isEmpty() ) {
				illegalOnApplication = true;
				illegalOnModule = true;
			}
			else if ( a.isEmpty() ) {
				illegalOnApplication = false;
				illegalOnModule = false;
			}

			for ( String allowedModule : a.split( "," ) ) {
				if ( "*".equals( allowedModule ) ) {
					illegalOnApplication = false;
					illegalOnModule = false;
				}
				else if ( "AcrossContext".equals( allowedModule ) ) {
					illegalOnApplication = false;
				}
				else if ( "AcrossModule".equals( allowedModule ) ) {
					illegalOnModule = false;
				}
				else {
					allowedModules.add( allowedModule );
				}
			}

			for ( String illegalModule : i.split( "," ) ) {
				if ( "*".equals( illegalModule ) ) {
					illegalOnApplication = true;
					illegalOnModule = true;
				}
				else if ( "AcrossContext".equals( illegalModule ) ) {
					illegalOnApplication = true;
				}
				else if ( "AcrossModule".equals( illegalModule ) ) {
					illegalOnModule = true;
				}
				else {
					illegalModules.add( illegalModule );
				}
			}

			this.illegalOnApplication = Boolean.TRUE.equals( illegalOnApplication );
			this.illegalOnModule = Boolean.TRUE.equals( illegalOnModule );
		}

		@Override
		public boolean test( AcrossModuleInfo moduleInfo ) {
			if ( moduleInfo != null ) {
				for ( String illegalModule : illegalModules ) {
					if ( moduleInfo.matchesModuleName( illegalModule ) ) {
						return true;
					}
				}
				for ( String allowedModule : allowedModules ) {
					if ( moduleInfo.matchesModuleName( allowedModule ) ) {
						return false;
					}
				}

				return illegalOnModule;
			}

			return illegalOnApplication;
		}
	}

	private IllegalConfigurationEntry isIllegalUse( Class<?> clazz, AcrossModuleInfo moduleInfo ) {
		for ( IllegalConfigurationEntry illegalToUse : illegalEntries ) {
			if ( illegalToUse.illegalType.isAssignableFrom( clazz ) && illegalToUse.moduleMatcher.test( moduleInfo ) ) {
				return illegalToUse;
			}
		}
		return null;
	}

	@SneakyThrows
	private static Class<?> resolveClass( String className, ClassLoader classLoader ) {
		if ( ClassUtils.isPresent( className, classLoader ) ) {
			return classLoader.loadClass( className );
		}

		return null;
	}

	@RequiredArgsConstructor
	private static class IllegalConfigurationEntry
	{
		private final Class<?> illegalType;
		private final Predicate<AcrossModuleInfo> moduleMatcher;
		private final AcrossConfiguration.IllegalConfiguration context;
	}

	static class IllegalConfigurationDetector implements BeanDefinitionRegistryPostProcessor, BeanClassLoaderAware
	{
		private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		@Override
		public void setBeanClassLoader( ClassLoader classLoader ) {
			this.classLoader = classLoader;
		}

		@Override
		public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException {
			IllegalConfigurationValidator illegalConfigurationValidator
					= ( (BeanFactory) registry ).getBean( "across.illegalConfigurationValidator", IllegalConfigurationValidator.class );
			AcrossModuleInfo moduleInfo = retrieveModuleInfo( registry );

			Stream.of( registry.getBeanDefinitionNames() )
			      .forEach( beanName -> {
				      BeanDefinition beanDefinition = registry.getBeanDefinition( beanName );

				      if ( !( beanDefinition instanceof ExposedBeanDefinition ) ) {
					      Class<?> beanType = resolveBeanType( beanDefinition );
					      if ( beanType != null ) {
						      IllegalConfigurationEntry illegal = illegalConfigurationValidator.isIllegalUse( beanType, moduleInfo );
						      if ( illegal != null ) {
							      String description = String.format(
									      "A bean definition of type '%s' was detected.%n - Bean name: '%s'%n - Bean type: '%s'",
									      illegal.illegalType.getName(), beanName, beanType.getName()
							      );

							      String contextDescription = illegal.context.getDescription();

							      if ( contextDescription != null ) {
								      description = String.format( "%s%n%n%s", description, contextDescription );
							      }

							      throw new AcrossConfigurationException( description, illegal.context.getAction() );
						      }
					      }
				      }
			      } );
		}

		private AcrossModuleInfo retrieveModuleInfo( BeanDefinitionRegistry registry ) {
			try {
				AcrossContextInfo contextInfo = ( (BeanFactory) registry ).getBean( AcrossContextInfo.BEAN, AcrossContextInfo.class );
				return contextInfo.getModuleBeingBootstrapped();
			}
			catch ( Exception e ) {
				return null;
			}
		}

		@Override
		public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
		}

		@SneakyThrows
		private Class<?> resolveBeanType( BeanDefinition beanDefinition ) {
			return resolveClass( beanDefinition.getBeanClassName(), classLoader );
		}
	}
}
