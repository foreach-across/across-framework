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

package com.foreach.across.core.context;

import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an exposed BeanDefinition.  An exposed BeanDefinition always uses the original
 * ApplicationContext as a source for the original beans.
 *
 * @author Arne Vandamme
 */
public class ExposedBeanDefinition extends RootBeanDefinition
{
	public static final int ROLE_EXPOSED = 10;

	@Getter
	private final String contextId;
	@Getter
	private final String moduleName;
	@Getter
	private final String fullyQualifiedBeanName;
	@Getter
	private final String originalBeanName;
	@Getter
	private final Integer moduleIndex;
	@Getter
	private String preferredBeanName;

	private final Set<String> aliases = new HashSet<>();

	private RootBeanDefinition originalRootBeanDefinition;
	@Getter
	private final AcrossContextBeanRegistry acrossContextBeanRegistry;

	public ExposedBeanDefinition( ExposedBeanDefinition original ) {
		super( original );

		originalRootBeanDefinition = original.originalRootBeanDefinition;
		acrossContextBeanRegistry = original.getAcrossContextBeanRegistry();

		contextId = original.contextId;
		moduleName = original.moduleName;
		originalBeanName = original.originalBeanName;
		fullyQualifiedBeanName = original.fullyQualifiedBeanName;
		preferredBeanName = original.preferredBeanName;
		moduleIndex = original.moduleIndex;
		aliases.addAll( original.aliases );

		setOriginatingBeanDefinition( original.getOriginatingBeanDefinition() );
		setRole( ROLE_EXPOSED );
	}

	public ExposedBeanDefinition( AcrossContextBeanRegistry contextBeanRegistry,
	                              String moduleName,
	                              Integer moduleIndex,
	                              String originalBeanName,
	                              Class beanClass,
	                              String[] aliases ) {
		this.contextId = contextBeanRegistry.getContextId();
		this.moduleName = moduleName;
		this.moduleIndex = moduleIndex;
		this.acrossContextBeanRegistry = contextBeanRegistry;

		setSynthetic( true );
		setScope( SCOPE_SINGLETON );

		if ( beanClass != null ) {
			setBeanClassName( beanClass.getName() );
			setBeanClass( beanClass );
			setTargetType( beanClass );
		}

		setAutowireMode( AUTOWIRE_NO );
		setAutowireCandidate( true );
		setDependencyCheck( DEPENDENCY_CHECK_NONE );

		addQualifier( new AutowireCandidateQualifier( Module.class.getName(), moduleName ) );
		//addQualifier( new AutowireCandidateQualifier( Context.class.getName(), contextId ) );

		this.originalBeanName = originalBeanName;
		fullyQualifiedBeanName = contextId + "." + moduleName + "@" + originalBeanName;

		setPreferredBeanName( originalBeanName );

		Collections.addAll( this.aliases, aliases );

		setRole( ROLE_EXPOSED );
	}

	public ExposedBeanDefinition( AcrossContextBeanRegistry contextBeanRegistry,
	                              String moduleName,
	                              Integer moduleIndex,
	                              String originalBeanName,
	                              BeanDefinition original,
	                              Class<?> beanClass,
	                              String[] aliases ) {
		this( contextBeanRegistry, moduleName, moduleIndex, originalBeanName, beanClass, aliases );

		setLazyInit( original.isLazyInit() );
		setOriginatingBeanDefinition( original );
		setPrimary( original.isPrimary() );
		setDescription( original.getDescription() );
		setRole( original.getRole() );
		setScope( original.getScope() );

		setAutowireCandidate( original.isAutowireCandidate() );

		if ( original instanceof RootBeanDefinition ) {
			originalRootBeanDefinition = (RootBeanDefinition) original;
			if ( ResolvableType.forClass( beanClass ).getGenerics().length > 0 ) {
				Method method = getResolvedFactoryMethod();
				if ( method != null ) {
					setTargetType( ResolvableType.forMethodReturnType( method ) );
				}
			}
		}

		// Add detailed information
		if ( original instanceof AbstractBeanDefinition ) {
			AbstractBeanDefinition originalAbstract = (AbstractBeanDefinition) original;

			for ( AutowireCandidateQualifier qualifier : originalAbstract.getQualifiers() ) {
				addQualifier( qualifier );
			}
		}

		setRole( ROLE_EXPOSED );
	}

	public void setPreferredBeanName( String preferredBeanName ) {
		this.preferredBeanName = preferredBeanName;
		addQualifier( new AutowireCandidateQualifier( Qualifier.class.getName(), preferredBeanName ) );
	}

	public Set<String> getAliases() {
		return Collections.unmodifiableSet( aliases );
	}

	public void addAlias( String alias ) {
		aliases.add( alias );
	}

	public void removeAlias( String alias ) {
		aliases.remove( alias );
	}

	@Override
	public Method getResolvedFactoryMethod() {
		if ( originalRootBeanDefinition != null ) {
			return originalRootBeanDefinition.getResolvedFactoryMethod();
		}
		return super.getResolvedFactoryMethod();
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		if ( !super.equals( o ) ) {
			return false;
		}
		ExposedBeanDefinition that = (ExposedBeanDefinition) o;
		return Objects.equals( contextId, that.contextId ) &&
				Objects.equals( moduleName, that.moduleName ) &&
				Objects.equals( fullyQualifiedBeanName, that.fullyQualifiedBeanName ) &&
				Objects.equals( originalBeanName, that.originalBeanName );
	}

	@Override
	public int hashCode() {
		return Objects.hash( super.hashCode(), contextId, moduleName, fullyQualifiedBeanName, originalBeanName );
	}
}
