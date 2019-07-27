/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.modules.web.mvc.condition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class TestCompositeCustomRequestCondition
{
	@Mock
	private CustomOne one;

	@Mock
	private CustomTwo two;

	@Mock
	private HttpServletRequest request;

	private CompositeCustomRequestCondition composite;

	@Test
	public void emptyMatchesAgainstEverything() {
		composite = new CompositeCustomRequestCondition();
		assertSame( composite, composite.getMatchingCondition( request ) );
	}

	@Test
	public void sameConditionTypesAreNotAllowed() {
		Assertions.assertThrows( IllegalArgumentException.class, () -> {
			composite = new CompositeCustomRequestCondition( Arrays.asList( one, two, mock( CustomOne.class ) ) );
		} );
	}

	@Test
	public void matchingFailsIfOneDoesNotMatch() {
		composite = new CompositeCustomRequestCondition( Arrays.asList( one, two ) );
		assertNull( composite.getMatchingCondition( request ) );

		verify( two, never() ).getMatchingCondition( request );

		when( one.getMatchingCondition( request ) ).thenReturn( one );
		assertNull( composite.getMatchingCondition( request ) );

		verify( one, times( 2 ) ).getMatchingCondition( request );
		verify( two ).getMatchingCondition( request );
	}

	@Test
	public void matchingReturnsNewInstanceWithSameContent() {
		composite = new CompositeCustomRequestCondition( Arrays.asList( one, two ) );
		when( one.getMatchingCondition( request ) ).thenReturn( one );
		when( two.getMatchingCondition( request ) ).thenReturn( two );

		assertEquals( composite, composite.getMatchingCondition( request ) );
	}

	@Test
	public void combineWhenOneIsEmpty() {
		CompositeCustomRequestCondition empty = new CompositeCustomRequestCondition();
		CompositeCustomRequestCondition notEmpty = new CompositeCustomRequestCondition( Collections.singleton( one ) );

		assertSame( notEmpty, empty.combine( notEmpty ) );
		assertSame( notEmpty, notEmpty.combine( empty ) );
		assertSame( empty, empty.combine( new CompositeCustomRequestCondition() ) );
	}

	@Test
	public void combineAddsMissingAndCombinesExisting() {
		composite = new CompositeCustomRequestCondition( Arrays.asList( one, two ) );
		CompositeCustomRequestCondition other = new CompositeCustomRequestCondition( Collections.singleton( two ) );
		when( two.combine( two ) ).thenReturn( two );

		CompositeCustomRequestCondition combined = composite.combine( other );
		assertEquals( Arrays.asList( one, two ), combined.getContent() );

		assertEquals( combined, other.combine( composite ) );

		verify( two, times( 2 ) ).combine( two );
	}

	@Test
	public void compareWhenOneIsEmpty() {
		CompositeCustomRequestCondition empty = new CompositeCustomRequestCondition();
		CompositeCustomRequestCondition notEmpty = new CompositeCustomRequestCondition( Collections.singleton( one ) );

		assertEquals( 0, empty.compareTo( new CompositeCustomRequestCondition(), request ) );
		assertEquals( 0, ( new CompositeCustomRequestCondition() ).compareTo( empty, request ) );
		assertEquals( 1, empty.compareTo( notEmpty, request ) );
		assertEquals( -1, notEmpty.compareTo( empty, request ) );
	}

	@Test
	public void compareToSameConditionTypeAndCount() {
		CustomOne other = mock( CustomOne.class );

		CompositeCustomRequestCondition left = new CompositeCustomRequestCondition( Collections.singleton( one ) );
		CompositeCustomRequestCondition right = new CompositeCustomRequestCondition( Collections.singleton( other ) );

		when( one.compareTo( other, request ) ).thenReturn( 0 );
		assertEquals( 0, left.compareTo( right, request ) );

		when( one.compareTo( other, request ) ).thenReturn( 1 );
		assertEquals( 1, left.compareTo( right, request ) );

		when( one.compareTo( other, request ) ).thenReturn( -1 );
		assertEquals( -1, left.compareTo( right, request ) );
	}

	@Test
	public void compareToDifferentCondition() {
		CompositeCustomRequestCondition left = new CompositeCustomRequestCondition( Collections.singleton( one ) );
		CompositeCustomRequestCondition right = new CompositeCustomRequestCondition( Collections.singleton( two ) );

		assertNotEquals( 0, left.compareTo( right, request ) );
		assertEquals( right.compareTo( left, request ), -left.compareTo( right, request ) );
	}

	@Test
	public void compareWithDifferentLength() {
		CompositeCustomRequestCondition left = new CompositeCustomRequestCondition( Collections.singleton( one ) );
		CompositeCustomRequestCondition right = new CompositeCustomRequestCondition( Arrays.asList( two, one ) );

		assertEquals( 1, left.compareTo( right, request ) );
		assertEquals( -1, right.compareTo( left, request ) );
		verify( one, never() ).compareTo( any(), any() );

		List<CompositeCustomRequestCondition> conditions = Arrays.asList( left, right );
		conditions.sort( ( l, r ) -> l.compareTo( r, null ) );

		assertEquals( Arrays.asList( right, left ), conditions );
	}

	interface CustomOne extends CustomRequestCondition<CustomOne>
	{
	}

	interface CustomTwo extends CustomRequestCondition<CustomTwo>
	{
	}
}
