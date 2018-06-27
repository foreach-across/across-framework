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

import static com.foreach.across.config.AcrossConfigurationLoader.loadSingleValue;

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
 * @since 3.0.0
 */
@ConditionalOnProperty(value = "across.configuration.validate", havingValue = "true", matchIfMissing = true)
@Component("across.illegalConfigurationValidator")
@Import(IllegalConfigurationValidator.IllegalConfigurationDetector.class)
public class IllegalConfigurationValidator implements AcrossBootstrapConfigurer, BeanClassLoaderAware
{
	private final List<IllegalConfigurationEntry> illegalEntries = new ArrayList<>();

	private ClassLoader classLoader;

	@Override
	public void setBeanClassLoader( ClassLoader classLoader ) {
		this.classLoader = classLoader;

		val entries = AcrossConfigurationLoader.loadValues( "com.foreach.across.IllegalConfiguration", classLoader );

		entries.stream()
		       .map( String::trim )
		       .forEach( e -> {
			       String[] definitionAndMessageKey = e.split( ":" );
			       String[] classNameAndMatcher = definitionAndMessageKey[0].split( "->" );

			       Class<?> clazz = resolveClass( classNameAndMatcher[0], classLoader );
			       if ( clazz != null ) {
				       String moduleNames = classNameAndMatcher.length > 1 ? classNameAndMatcher[1] : null;
				       Predicate<AcrossModuleInfo> matcher = new ModuleMatcher( moduleNames );

				       String messageKey = definitionAndMessageKey.length > 1 ? definitionAndMessageKey[1] : null;
				       illegalEntries.add( new IllegalConfigurationEntry( clazz, matcher, messageKey ) );
			       }
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

		ModuleMatcher( String moduleSpecifier ) {
			Boolean illegalOnApplication = null;
			Boolean illegalOnModule = null;

			if ( moduleSpecifier != null ) {
				for ( String moduleName : moduleSpecifier.split( "\\|" ) ) {
					boolean negative = moduleName.startsWith( "!" );
					String transformedName = negative ? moduleName.substring( 1 ) : moduleName;

					if ( "AcrossModule".equals( transformedName ) ) {
						illegalOnModule = !negative;
					}
					else if ( "AcrossContext".equals( transformedName ) ) {
						illegalOnApplication = !negative;
					}
					else if ( negative ) {
						allowedModules.add( transformedName );
					}
					else {
						illegalModules.add( transformedName );
					}
				}
			}
			else {
				illegalOnApplication = true;
			}

			if ( illegalOnModule == null ) {
				illegalOnModule = ( illegalOnApplication == null || !illegalOnApplication ) && illegalModules.isEmpty();
			}

			if ( illegalOnApplication == null ) {
				illegalOnApplication = true;
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

	private IllegalConfigurationMessage getMessage( IllegalConfigurationEntry entry ) {
		if ( entry.messageKey != null ) {
			return new IllegalConfigurationMessage(
					loadSingleValue( "com.foreach.across.IllegalConfiguration[" + entry.messageKey + "].description", classLoader ),
					loadSingleValue( "com.foreach.across.IllegalConfiguration[" + entry.messageKey + "].action", classLoader )
			);
		}

		return new IllegalConfigurationMessage( null, null );
	}

	@SneakyThrows
	private static Class<?> resolveClass( String className, ClassLoader classLoader ) {
		if ( ClassUtils.isPresent( className, classLoader ) ) {
			return classLoader.loadClass( className );
		}

		return null;
	}

	@RequiredArgsConstructor
	private static class IllegalConfigurationMessage
	{
		private final String description;
		private final String action;
	}

	@RequiredArgsConstructor
	private static class IllegalConfigurationEntry
	{
		private final Class<?> illegalType;
		private final Predicate<AcrossModuleInfo> moduleMatcher;
		private final String messageKey;
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

							      IllegalConfigurationMessage message = illegalConfigurationValidator.getMessage( illegal );

							      if ( message.description != null ) {
								      description = String.format( "%s%n%n%s", description, message.description );
							      }

							      throw new AcrossConfigurationException( description, message.action );
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
