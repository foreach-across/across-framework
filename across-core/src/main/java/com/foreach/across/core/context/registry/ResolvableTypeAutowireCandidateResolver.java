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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * @author Arne Vandamme
 */
public class ResolvableTypeAutowireCandidateResolver extends GenericTypeAwareAutowireCandidateResolver
{
	/**
	 * Overridden method to allow forced resolving of a specified ResolvableType.
	 * Used to get all beans matching a ResolvableType from the
	 * {@link com.foreach.across.core.context.registry.AcrossContextBeanRegistry}.
	 */
	@Override
	protected boolean checkGenericTypeMatch( BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor ) {
		ResolvableType dependencyType = descriptor.getResolvableType();

		ResolvableType targetType = null;
		RootBeanDefinition rbd = null;
		if ( bdHolder.getBeanDefinition() instanceof RootBeanDefinition ) {
			rbd = (RootBeanDefinition) bdHolder.getBeanDefinition();
		}
		if ( rbd != null ) {
			// First, check factory method return type, if applicable
			targetType = getReturnTypeForFactoryMethod( rbd, descriptor );
			if ( targetType == null ) {
				RootBeanDefinition dbd = getResolvedDecoratedDefinition( rbd );
				if ( dbd != null ) {
					targetType = getReturnTypeForFactoryMethod( dbd, descriptor );
				}
			}
		}
		if ( targetType == null ) {
			// Regular case: straight bean instance, with BeanFactory available.
			if ( getBeanFactory() != null ) {
				Class<?> beanType = getBeanFactory().getType( bdHolder.getBeanName() );
				if ( beanType != null ) {
					targetType = ResolvableType.forClass( ClassUtils.getUserClass( beanType ) );
				}
			}
			// Fallback: no BeanFactory set, or no type resolvable through it
			// -> best-effort match against the target class if applicable.
			if ( targetType == null && rbd != null && rbd.hasBeanClass() && rbd.getFactoryMethodName() == null ) {
				Class<?> beanClass = rbd.getBeanClass();
				if ( !FactoryBean.class.isAssignableFrom( beanClass ) ) {
					targetType = ResolvableType.forClass( ClassUtils.getUserClass( beanClass ) );
				}
			}
		}
		if ( targetType == null || ( descriptor.fallbackMatchAllowed() && targetType.hasUnresolvableGenerics() ) ) {
			return true;
		}
		// Full check for complex generic type match...
		return dependencyType.isAssignableFrom( targetType );
	}
}
