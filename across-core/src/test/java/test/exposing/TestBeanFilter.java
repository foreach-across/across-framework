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
package test.exposing;

import com.foreach.across.AcrossPlatform;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.filters.*;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.math.BigInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 */
public class TestBeanFilter
{
	@Test
	public void empty() {
		assertNotNull( BeanFilter.empty() );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void annotations() {
		BeanFilter filter = BeanFilter.annotations( Exposed.class, null, Object.class, Controller.class );
		assertTrue( filter instanceof AnnotationBeanFilter );
		Class<Annotation>[] annotations = (Class<Annotation>[]) ReflectionTestUtils.getField( filter, "annotations" );
		assertNotNull( annotations );
		assertEquals( 2, annotations.length );
		assertEquals( Exposed.class, annotations[0] );
		assertEquals( Controller.class, annotations[1] );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void instances() {
		BeanFilter filter = BeanFilter.instances( String.class, null, BigInteger.class, Controller.class );
		assertTrue( filter instanceof ClassBeanFilter );
		Class[] classes = (Class[]) ReflectionTestUtils.getField( filter, "allowedItems" );
		assertNotNull( classes );
		assertEquals( 2, classes.length );
		assertEquals( String.class, classes[0] );
		assertEquals( BigInteger.class, classes[1] );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void beanNames() {
		BeanFilter filter = BeanFilter.beanNames( "one", null, "two" );
		assertTrue( filter instanceof NamedBeanFilter );
		String[] names = (String[]) ReflectionTestUtils.getField( filter, "allowedNames" );
		assertNotNull( names );
		assertEquals( 2, names.length );
		assertEquals( "one", names[0] );
		assertEquals( "two", names[1] );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void packageNames() {
		BeanFilter filter = BeanFilter.packages( "com.foreach.across", null, "java.lang" );
		assertTrue( filter instanceof PackageBeanFilter );
		String[] packageNames = (String[]) ReflectionTestUtils.getField( filter, "allowedItems" );
		assertNotNull( packageNames );
		assertEquals( 2, packageNames.length );
		assertEquals( "com.foreach.across", packageNames[0] );
		assertEquals( "java.lang", packageNames[1] );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void packageClasses() {
		BeanFilter filter = BeanFilter.packages( AcrossPlatform.class, null, String.class );
		assertTrue( filter instanceof PackageBeanFilter );
		String[] packageNames = (String[]) ReflectionTestUtils.getField( filter, "allowedItems" );
		assertNotNull( packageNames );
		assertEquals( 2, packageNames.length );
		assertEquals( "com.foreach.across", packageNames[0] );
		assertEquals( "java.lang", packageNames[1] );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void composite() {
		BeanFilter one = mock( BeanFilter.class );
		BeanFilter two = mock( BeanFilter.class );
		BeanFilter filter = BeanFilter.composite( one, null, two );
		assertTrue( filter instanceof BeanFilterComposite );
		BeanFilter[] filters = (BeanFilter[]) ReflectionTestUtils.getField( filter, "filters" );
		assertNotNull( filters );
		assertEquals( 2, filters.length );
		assertSame( one, filters[0] );
		assertSame( two, filters[1] );
	}
}
