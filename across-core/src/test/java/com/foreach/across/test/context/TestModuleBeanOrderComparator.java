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

package com.foreach.across.test.context;

import com.foreach.across.core.OrderedInModule;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.context.ModuleBeanOrderComparator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestModuleBeanOrderComparator
{
	private final Object orderFour = new BeanOrderFour();
	private final Object orderFive = new BeanOrderFive();
	private final Object beanOne = new Object();
	private final Object beanTwo = new Object();
	private final Object beanThree = new Object();
	private final Object moduleOrderTen = new BeanModuleOrderTen();
	private final Object moduleOrderEleven = new BeanModuleOrderEleven();
	private final Object moduleOrderedOne = new ModuleOrderedBean( 1 );
	private final Object moduleOrderedTwo = new ModuleOrderedBean( 2 );
	private final Object moduleOrderedThree = new ModuleOrderedBean( 3 );
	private final Object moduleOrderedZeroAndOrderInModuleTwelve = new ModuleOrderedBeanOrderInModuleTwelve( 0 );
	private final Object orderedOne = new OrderedBean( 1 );
	private final Object orderedTwo = new OrderedBean( 2 );
	private final Object orderedThree = new OrderedBean( 3 );
	private final Object orderedMinusOneAndOrderThree = new OrderedBeanOrderThree( -1 );

	private List<Object> beans;
	private ModuleBeanOrderComparator comparator;

	@Before
	public void reset() {
		beans = new ArrayList<>();
		comparator = new ModuleBeanOrderComparator();

		comparator.register( orderedOne, 0 );
		comparator.register( orderedTwo, 0 );
		comparator.register( orderedThree, 0 );
		comparator.register( orderedMinusOneAndOrderThree, 0 );
		comparator.register( orderFour, 0 );
		comparator.register( orderFive, 0 );
		comparator.register( beanOne, 0 );
		comparator.register( beanTwo, 0 );
		comparator.register( beanThree, 0 );
		comparator.register( moduleOrderTen, 0 );
		comparator.register( moduleOrderEleven, 0 );
		comparator.register( moduleOrderedOne, 0 );
		comparator.register( moduleOrderedTwo, 0 );
		comparator.register( moduleOrderedThree, 0 );
		comparator.register( moduleOrderedZeroAndOrderInModuleTwelve, 0 );
	}

	@Test
	public void beansWithOrderedInterface() {
		list( orderedTwo, orderedThree, orderedOne );

		assertOrder( orderedOne, orderedTwo, orderedThree );
	}

	@Test
	public void beansWithOrderAnnotation() {
		list( orderFive, orderFour );

		assertOrder( orderFour, orderFive );
	}

	@Test
	public void beansWithOrderAnnotationAndOrderedInterface() {
		list( orderedTwo, orderedThree, orderedOne, orderFour, orderedMinusOneAndOrderThree, orderFive );

		assertOrder( orderedMinusOneAndOrderThree, orderedOne, orderedTwo, orderedThree, orderFour, orderFive );
	}

	@Test
	public void beansWithOrderedInModuleInterface() {
		list( moduleOrderedOne, moduleOrderedThree, moduleOrderedTwo );

		assertOrder( moduleOrderedOne, moduleOrderedTwo, moduleOrderedThree );
	}

	@Test
	public void beansWithOrderInModule() {
		list( moduleOrderEleven, moduleOrderTen );

		assertOrder( moduleOrderTen, moduleOrderEleven );
	}

	@Test
	public void beansWithOrderInModuleAnnotationAndOrderedInModuleInterface() {
		list( moduleOrderedOne, moduleOrderedThree, moduleOrderedTwo, moduleOrderTen,
		      moduleOrderedZeroAndOrderInModuleTwelve, moduleOrderEleven );

		assertOrder( moduleOrderedZeroAndOrderInModuleTwelve, moduleOrderedOne, moduleOrderedTwo, moduleOrderedThree,
		             moduleOrderTen, moduleOrderEleven );
	}

	@Test
	public void beansAccordingToModuleIndex() {
		list( beanOne, beanThree, beanTwo );

		comparator.register( beanOne, 50 );
		comparator.register( beanTwo, 10 );
		comparator.register( beanThree, -1 );

		assertOrder( beanThree, beanTwo, beanOne );
	}

	@Test
	public void orderPrecedesModuleIndex() {
		list( orderedTwo, orderedThree, orderedOne, orderFour, orderedMinusOneAndOrderThree, orderFive );

		comparator.register( orderedOne, 1 );
		comparator.register( orderedTwo, 2 );
		comparator.register( orderedThree, 3 );
		comparator.register( orderFour, 4 );
		comparator.register( orderFive, 5 );
		comparator.register( orderedMinusOneAndOrderThree, 6 );

		assertOrder( orderedMinusOneAndOrderThree, orderedOne, orderedTwo, orderedThree, orderFour, orderFive );
	}

	@Test
	public void moduleIndexPrecedesOrderInModule() {
		list( moduleOrderedOne, moduleOrderedThree, moduleOrderedTwo, moduleOrderTen,
		      moduleOrderedZeroAndOrderInModuleTwelve, moduleOrderEleven );

		comparator.register( moduleOrderedOne, 1 );
		comparator.register( moduleOrderedTwo, 2 );
		comparator.register( moduleOrderedThree, 3 );
		comparator.register( moduleOrderEleven, 4 );
		comparator.register( moduleOrderTen, 5 );
		comparator.register( moduleOrderedZeroAndOrderInModuleTwelve, 6 );

		assertOrder( moduleOrderedOne, moduleOrderedTwo, moduleOrderedThree, moduleOrderEleven, moduleOrderTen,
		             moduleOrderedZeroAndOrderInModuleTwelve );
	}

	private void list( Object... beans ) {
		this.beans = Arrays.asList( beans );
	}

	private void assertOrder( Object... expected ) {
		Collections.sort( beans, comparator );
		assertEquals( Arrays.asList( expected ), beans );
	}

	@Order(4)
	static class BeanOrderFour
	{

	}

	@Order(5)
	static class BeanOrderFive
	{

	}

	@Order(3)
	static class OrderedBeanOrderThree extends OrderedBean
	{
		OrderedBeanOrderThree( int order ) {
			super( order );
		}
	}

	@OrderInModule(10)
	static class BeanModuleOrderTen
	{

	}

	@OrderInModule(11)
	static class BeanModuleOrderEleven
	{

	}

	static class ModuleOrderedBean implements OrderedInModule
	{
		private final int order;

		ModuleOrderedBean( int order ) {
			this.order = order;
		}

		@Override
		public int getOrderInModule() {
			return order;
		}
	}

	@OrderInModule(12)
	static class ModuleOrderedBeanOrderInModuleTwelve extends ModuleOrderedBean
	{
		ModuleOrderedBeanOrderInModuleTwelve( int order ) {
			super( order );
		}
	}

	static class OrderedBean implements Ordered
	{
		private final int order;

		OrderedBean( int order ) {
			this.order = order;
		}

		@Override
		public int getOrder() {
			return order;
		}
	}
}
