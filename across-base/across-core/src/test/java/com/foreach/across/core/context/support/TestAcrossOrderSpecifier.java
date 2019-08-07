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
package com.foreach.across.core.context.support;

import com.foreach.across.core.OrderedInModule;
import com.foreach.across.core.annotations.OrderInModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestAcrossOrderSpecifier
{
	@Test
	public void runtimeOrderReturnedIfOrderedInterface() {
		final AcrossOrderSpecifier specifier = AcrossOrderSpecifier.builder().order( 123 ).build();
		assertEquals( Integer.valueOf( 123 ), specifier.getOrder() );
		assertEquals( Integer.valueOf( 123 ), specifier.getOrder( null ) );
		assertEquals( Integer.valueOf( 123 ), specifier.getOrder( "some string" ) );
		assertEquals( Integer.valueOf( 999 ), specifier.getOrder( (Ordered) () -> 999 ) );
	}

	@Test
	public void runtimeOrderInModuleReturnedIfOrderedInModuleInterface() {
		final AcrossOrderSpecifier specifier = AcrossOrderSpecifier.builder().orderInModule( 123 ).build();
		assertEquals( Integer.valueOf( 123 ), specifier.getOrderInModule() );
		assertEquals( Integer.valueOf( 123 ), specifier.getOrderInModule( null ) );
		assertEquals( Integer.valueOf( 123 ), specifier.getOrderInModule( (Ordered) () -> 999 ) );
		assertEquals( Integer.valueOf( 999 ), specifier.getOrderInModule( (OrderedInModule) () -> 999 ) );
	}

	@Test
	public void nonOrderedBean() {
		AcrossOrderSpecifier specifier = specifier( String.class );
		assertOrders( specifier, null, null );
	}

	@Test
	public void orderedBean() {
		AcrossOrderSpecifier specifier = specifier( OrderedBean.class );
		assertOrders( specifier, 100, null );
	}

	@Test
	@SneakyThrows
	public void orderedMethod() {
		AcrossOrderSpecifier specifier = specifier( OrderedBean.class.getMethod( "orderedMethod" ) );
		assertOrders( specifier, 200, null );
	}

	@Test
	@SneakyThrows
	public void orderedFirstObjectSourceWinsIfMultiple() {
		AcrossOrderSpecifier specifier = specifier( OrderedBean.class, OrderedBean.class.getMethod( "orderedMethod" ) );
		assertOrders( specifier, 100, null );

		specifier = specifier( OrderedBean.class.getMethod( "orderedMethod" ), OrderedBean.class );
		assertOrders( specifier, 200, null );
	}

	@Test
	public void moduleOrderedBean() {
		AcrossOrderSpecifier specifier = specifier( ModuleOrderedBean.class );
		assertOrders( specifier, null, 300 );
	}

	@Test
	@SneakyThrows
	public void moduleOrderedMethod() {
		AcrossOrderSpecifier specifier = specifier( ModuleOrderedBean.class.getMethod( "orderedMethod" ) );
		assertOrders( specifier, null, 400 );
	}

	@Test
	@SneakyThrows
	public void moduleOrderedFirstObjectSourceWinsIfMultiple() {
		AcrossOrderSpecifier specifier = specifier( ModuleOrderedBean.class, ModuleOrderedBean.class.getMethod( "orderedMethod" ) );
		assertOrders( specifier, null, 300 );

		specifier = specifier( ModuleOrderedBean.class.getMethod( "orderedMethod" ), ModuleOrderedBean.class );
		assertOrders( specifier, null, 400 );
	}

	@Test
	@SneakyThrows
	public void firstObjectSourceWithOrderDeterminesValues() {
		AcrossOrderSpecifier specifier = specifier( String.class, ModuleOrderedBean.class, OrderedBean.class );
		assertOrders( specifier, null, 300 );

		specifier = specifier( String.class, OrderedBean.class, ModuleOrderedBean.class );
		assertOrders( specifier, 100, null );
	}

	@Test
	public void unsetPriorityValue() {
		AcrossOrderSpecifier specifier = AcrossOrderSpecifier.builder().build();
		assertEquals( 1000000000, specifier.toPriority() );
		assertEquals( 1000000000, specifier.toPriority( "string" ) );
		assertEquals( 1, specifier.toPriority( (Ordered) () -> 1 ) );
		assertEquals( Ordered.HIGHEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.HIGHEST_PRECEDENCE ) );
		assertEquals( Ordered.LOWEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.LOWEST_PRECEDENCE ) );
		assertEquals( 1000000001, specifier.toPriority( (OrderedInModule) () -> 1 ) );
	}

	@Test
	public void defaultPriorityValueIsBasedOnTheModuleIndex() {
		AcrossOrderSpecifier specifier = AcrossOrderSpecifier.builder().moduleIndex( 1 ).build();
		assertEquals( 1002000000, specifier.toPriority() );
		assertEquals( 1002000000, specifier.toPriority( "string" ) );
		assertEquals( 2, specifier.toPriority( (Ordered) () -> 2 ) );
		assertEquals( Ordered.HIGHEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.HIGHEST_PRECEDENCE ) );
		assertEquals( Ordered.LOWEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.LOWEST_PRECEDENCE ) );
		assertEquals( 1002000001, specifier.toPriority( (OrderedInModule) () -> 1 ) );
		assertEquals( 1001999999, specifier.toPriority( (OrderedInModule) () -> -1 ) );
		assertEquals( 1001000000, specifier.toPriority( (OrderedInModule) () -> Ordered.HIGHEST_PRECEDENCE ) );
		assertEquals( 1002999999, specifier.toPriority( (OrderedInModule) () -> Ordered.LOWEST_PRECEDENCE ) );
	}

	@Test
	public void priorityWhenOrderSpecified() {
		AcrossOrderSpecifier specifier = AcrossOrderSpecifier.builder().order( 13 ).moduleIndex( 1 ).build();
		assertEquals( 13, specifier.toPriority() );
		assertEquals( 13, specifier.toPriority( "string" ) );
		assertEquals( 2, specifier.toPriority( (Ordered) () -> 2 ) );
		assertEquals( Ordered.HIGHEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.HIGHEST_PRECEDENCE ) );
		assertEquals( Ordered.LOWEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.LOWEST_PRECEDENCE ) );
		assertEquals( 13, specifier.toPriority( (OrderedInModule) () -> 1 ) );
	}

	@Test
	public void priorityWhenOrderInModuleSpecified() {
		AcrossOrderSpecifier specifier = AcrossOrderSpecifier.builder().orderInModule( 5 ).build();
		assertEquals( 1000000005, specifier.toPriority() );
		assertEquals( 1000000005, specifier.toPriority( "string" ) );
		assertEquals( 2, specifier.toPriority( (Ordered) () -> 2 ) );
		assertEquals( Ordered.HIGHEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.HIGHEST_PRECEDENCE ) );
		assertEquals( Ordered.LOWEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.LOWEST_PRECEDENCE ) );
		assertEquals( 1000000001, specifier.toPriority( (OrderedInModule) () -> 1 ) );
		assertEquals( 999999999, specifier.toPriority( (OrderedInModule) () -> -1 ) );
		assertEquals( 999000000, specifier.toPriority( (OrderedInModule) () -> Ordered.HIGHEST_PRECEDENCE ) );
		assertEquals( 1000999999, specifier.toPriority( (OrderedInModule) () -> Ordered.LOWEST_PRECEDENCE ) );
	}

	@Test
	public void priorityWhenOrderInModuleAndModuleIndexSpecified() {
		AcrossOrderSpecifier specifier = AcrossOrderSpecifier.builder().moduleIndex( 2 ).orderInModule( 5 ).build();
		assertEquals( 1004000005, specifier.toPriority() );
		assertEquals( 1004000005, specifier.toPriority( "string" ) );
		assertEquals( 2, specifier.toPriority( (Ordered) () -> 2 ) );
		assertEquals( Ordered.HIGHEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.HIGHEST_PRECEDENCE ) );
		assertEquals( Ordered.LOWEST_PRECEDENCE, specifier.toPriority( (Ordered) () -> Ordered.LOWEST_PRECEDENCE ) );
		assertEquals( 1004000001, specifier.toPriority( (OrderedInModule) () -> 1 ) );
		assertEquals( 1003999999, specifier.toPriority( (OrderedInModule) () -> -1 ) );
		assertEquals( 1003000000, specifier.toPriority( (OrderedInModule) () -> Ordered.HIGHEST_PRECEDENCE ) );
		assertEquals( 1004999999, specifier.toPriority( (OrderedInModule) () -> Ordered.LOWEST_PRECEDENCE ) );
	}

	private AcrossOrderSpecifier specifier( Object... sources ) {
		return AcrossOrderSpecifier.forSources( Arrays.asList( sources ) ).build();
	}

	private void assertOrders( AcrossOrderSpecifier specifier, Integer order, Integer orderInModule ) {
		assertEquals( order, specifier.getOrder() );
		assertEquals( orderInModule, specifier.getOrderInModule() );
	}

	@SuppressWarnings("all")
	@Order(100)
	static class OrderedBean
	{
		@Order(200)
		public String orderedMethod() {
			return null;
		}
	}

	@SuppressWarnings("all")
	@OrderInModule(300)
	static class ModuleOrderedBean
	{
		@OrderInModule(400)
		public String orderedMethod() {
			return null;
		}
	}
}
