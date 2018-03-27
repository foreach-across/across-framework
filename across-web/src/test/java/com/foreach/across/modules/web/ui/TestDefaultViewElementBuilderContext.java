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
package com.foreach.across.modules.web.ui;

import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.modules.web.support.LocalizedTextResolver;
import com.foreach.across.modules.web.support.MessageCodeSupportingLocalizedTextResolver;
import org.junit.After;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
public class TestDefaultViewElementBuilderContext
{
	@After
	public void after() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void defaultMessages() {
		DefaultViewElementBuilderContext ctx = new DefaultViewElementBuilderContext(  );
		assertEquals( "my.code", ctx.getMessage( "my.code" ) );
		assertEquals( "default message", ctx.getMessage( "my.code", "default message" ) );
	}

	@Test
	public void withoutParent() {
		DefaultViewElementBuilderContext ctx = new DefaultViewElementBuilderContext( false );
		assertFalse( ctx.hasAttribute( "test" ) );

		ctx.setAttribute( "test", "one" );
		assertTrue( ctx.hasAttribute( "test" ) );

		assertEquals( "one", ctx.getAttribute( "test" ) );

		assertArrayEquals( new String[] { "test" }, ctx.attributeNames() );
		assertEquals( Collections.singletonMap( "test", "one" ), ctx.attributeMap() );

		ctx.removeAttribute( "test" );
		assertFalse( ctx.hasAttribute( "test" ) );
	}

	@Test
	public void withoutRequestDefaultsAreReturned() {
		DefaultViewElementBuilderContext ctx = new DefaultViewElementBuilderContext();

		assertThat( ctx.getLocalizedTextResolver() ).isNotNull();
		assertThat( ctx.resolveText( "test" ) ).isEqualTo( "test" );
		assertThat( ctx.resolveText( "#{test}" ) ).isEqualTo( "test" );
		assertThat( ctx.resolveText( "#{test=My test}" ) ).isEqualTo( "My test" );

		assertThat( ctx.getWebAppLinkBuilder() ).isNotNull();
		assertThat( ctx.buildLink( "my link" ) ).isEqualTo( "my link" );
		assertThat( ctx.getMessageSource() ).isNotNull();
		assertThat( ctx.getMessage( "message code" ) ).isEqualTo( "message code" );
	}

	@Test
	public void withParent() {
		ModelMap map = new ModelMap( "one", 1 );
		DefaultViewElementBuilderContext ctx = new DefaultViewElementBuilderContext( map );

		assertTrue( ctx.hasAttribute( "one" ) );
		assertFalse( ctx.hasAttribute( "two" ) );

		map.put( "two", 2 );
		assertTrue( ctx.hasAttribute( "two" ) );

		assertEquals( 1, ctx.getAttribute( "one" ) );
		assertEquals( 2, ctx.getAttribute( "two" ) );

		ctx.setAttribute( "two", "two" );
		assertEquals( "two", ctx.getAttribute( "two" ) );
		assertEquals( 2, map.get( "two" ) );

		ctx.setAttribute( "three", 3 );

		assertEquals( 2, map.size() );
		assertFalse( map.containsAttribute( "three" ) );

		assertArrayEquals( new String[] { LocalizedTextResolver.class.getName(), "one", "three", "two" }, ctx.attributeNames() );

		Map<String, Object> expected = new HashMap<>();
		expected.put( LocalizedTextResolver.class.getName(), new MessageCodeSupportingLocalizedTextResolver() );
		expected.put( "one", 1 );
		expected.put( "two", "two" );
		expected.put( "three", 3 );
		assertEquals( expected, ctx.attributeMap() );
	}

	@Test
	public void webResourceRegistryShouldBeRegisteredIfNoParent() {
		WebResourceRegistry registry = mock( WebResourceRegistry.class );

		RequestAttributes attributes = mock( RequestAttributes.class );
		when( attributes.getAttribute( WebResourceUtils.REGISTRY_ATTRIBUTE_KEY, RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( registry );
		RequestContextHolder.setRequestAttributes( attributes );

		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();
		assertSame( registry, builderContext.getAttribute( WebResourceRegistry.class ) );
	}

	@Test
	public void webResourceRegistryShouldBeRegisteredIfParentDoesNotContain() {
		WebResourceRegistry registry = mock( WebResourceRegistry.class );

		RequestAttributes attributes = mock( RequestAttributes.class );
		when( attributes.getAttribute( WebResourceUtils.REGISTRY_ATTRIBUTE_KEY, RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( registry );
		RequestContextHolder.setRequestAttributes( attributes );

		ViewElementBuilderContext parent = new DefaultViewElementBuilderContext();
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext( parent );
		assertSame( registry, builderContext.getAttribute( WebResourceRegistry.class ) );
	}

	@Test
	public void webResourceRegistryFromParentShouldBeKept() {
		WebResourceRegistry registry = mock( WebResourceRegistry.class );

		RequestAttributes attributes = mock( RequestAttributes.class );
		when( attributes.getAttribute( WebResourceUtils.REGISTRY_ATTRIBUTE_KEY, RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( registry );
		RequestContextHolder.setRequestAttributes( attributes );

		ViewElementBuilderContext parent = new DefaultViewElementBuilderContext();
		WebResourceRegistry other = mock( WebResourceRegistry.class );
		parent.setAttribute( WebResourceRegistry.class, other );

		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext( parent );
		assertNotSame( registry, other );
		assertSame( other, builderContext.getAttribute( WebResourceRegistry.class ) );
	}
}

