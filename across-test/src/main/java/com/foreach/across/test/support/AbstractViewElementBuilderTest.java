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
package com.foreach.across.test.support;

import com.foreach.across.modules.web.ui.*;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Base unit test for {@link com.foreach.across.modules.web.ui.ViewElementBuilderSupport} implementations.  Mainly
 * verifies that all implemented methods return the same strongly typed builder instances - unless they are deliberately
 * excepted.
 *
 * @param <T> ViewElementBuilder implementation extending ViewElementBuilderSupport
 * @param <U> ViewElement type that is generated by the builder
 */
public abstract class AbstractViewElementBuilderTest<T extends ViewElementBuilderSupport<U, T>, U extends ViewElement>
{
	protected T builder;
	protected U element;

	protected ViewElementBuilderFactory builderFactory;
	protected ViewElementBuilderContext builderContext;

	@Before
	public void reset() {
		builderFactory = mock( ViewElementBuilderFactory.class );
		builderContext = mock( ViewElementBuilderContext.class );

		builder = createBuilder( builderFactory );
		element = null;
	}

	@Test
	public void commonProperties() {
		assertSame( builder, builder.name( "componentName" ).customTemplate( "custom/template" ) );

		build();

		assertEquals( "componentName", element.getName() );
		assertEquals( "custom/template", element.getCustomTemplate() );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void postProcessors() {
		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor three = mock( ViewElementPostProcessor.class );

		assertSame( builder, builder.postProcessor( one ).postProcessor( two ).postProcessor( three ) );

		build();

		verify( one ).postProcess( eq( builderContext ), any( ViewElement.class ) );
		verify( two ).postProcess( eq( builderContext ), any( ViewElement.class ) );
		verify( three ).postProcess( eq( builderContext ), any( ViewElement.class ) );
	}

	@Test
	public void methodsShouldReturnBuilderInstance() throws Exception {
		Class<?> c = builder.getClass();

		Collection<String> methodExceptions = Arrays.asList( "^build$", "^wait$", "^equals$", "^toString$",
		                                                     "^hashCode$", "^notify$", "^notifyAll$", "^get[A-Z].+",
		                                                     "^is[A-Z].+", "^has[A-Z].+" );
		methodExceptions.addAll( nonBuilderReturningMethods() );

		for ( Method method : c.getMethods() ) {
			if ( Modifier.isPublic( method.getModifiers() )
					&& !isExceptionMethod( method.getName(), methodExceptions ) ) {
				Method declared = c.getDeclaredMethod( method.getName(), method.getParameterTypes() );

				assertEquals( "Method [" + method + "] does not return same builder type",
				              c,
				              declared.getReturnType() );
			}
		}
	}

	private boolean isExceptionMethod( String methodName, Collection<String> methodExceptions ) {
		for ( String exception : methodExceptions ) {
			if ( Pattern.matches( exception, methodName ) ) {
				return true;
			}
		}
		return false;
	}

	protected abstract T createBuilder( ViewElementBuilderFactory builderFactory );

	protected Collection<String> nonBuilderReturningMethods() {
		return Collections.emptyList();
	}

	protected void build() {
		element = builder.build( builderContext );
	}
}
