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
package com.foreach.across.modules.web.resource;

import com.foreach.across.modules.web.ui.ViewElementBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class TestWebResourceReferenceSorter
{
	@Mock
	private ViewElementBuilder viewElementBuilder;

	@Test
	public void registrationOrderIsKeptIfNoOrderSpecified() {
		WebResourceReference one = WebResourceReference.builder().viewElementBuilder( viewElementBuilder ).build();
		WebResourceReference two = WebResourceReference.builder().viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		WebResourceReference three = WebResourceReference.builder().viewElementBuilder( mock( ViewElementBuilder.class ) ).build();

		assertThat( WebResourceReferenceSorter.sort( Arrays.asList( one, two, three ) ) )
				.containsExactly( one, two, three );
		assertThat( WebResourceReferenceSorter.sort( Arrays.asList( two, three, one ) ) )
				.containsExactly( two, three, one );
	}

	@Test
	public void beforeOrAfterHaveNoImpactIfTargetNotPresent() {
		WebResourceReference one = WebResourceReference.builder().viewElementBuilder( viewElementBuilder ).before( "idontexist" ).build();
		WebResourceReference two = WebResourceReference.builder().viewElementBuilder( mock( ViewElementBuilder.class ) ).after( "ialsodontexist" ).build();
		WebResourceReference three = WebResourceReference.builder().viewElementBuilder( mock( ViewElementBuilder.class ) ).build();

		assertThat( WebResourceReferenceSorter.sort( Arrays.asList( one, two, three ) ) )
				.containsExactly( one, two, three );
		assertThat( WebResourceReferenceSorter.sort( Arrays.asList( two, three, one ) ) )
				.containsExactly( two, three, one );
	}

	@Test
	public void sortByOrder() {
		List<WebResourceReference> items = new LinkedList<>();
		items.add( new WebResourceReference( viewElementBuilder, "0", null, null, null ) );
		items.add( new WebResourceReference( viewElementBuilder, "3", null, null, 3 ) );
		items.add( new WebResourceReference( viewElementBuilder, "2", null, null, 2 ) );
		items.add( new WebResourceReference( viewElementBuilder, "1", null, null, 1 ) );
		items.add( new WebResourceReference( viewElementBuilder, "4", null, null, null ) );
		List<WebResourceReference> sorted = WebResourceReferenceSorter.sort( items );
		assertEquals( "1", sorted.get( 0 ).getKey() );
		assertEquals( "2", sorted.get( 1 ).getKey() );
		assertEquals( "3", sorted.get( 2 ).getKey() );
		assertEquals( "0", sorted.get( 3 ).getKey() );
		assertEquals( "4", sorted.get( 4 ).getKey() );
	}

	@Test
	public void sortByBefore() {
		List<WebResourceReference> items = new LinkedList<>();
		items.add( new WebResourceReference( viewElementBuilder, "0", "1", null, null ) );
		items.add( new WebResourceReference( viewElementBuilder, "2", "3", null, null ) );
		items.add( new WebResourceReference( viewElementBuilder, "3", "4", null, null ) );
		items.add( new WebResourceReference( viewElementBuilder, "1", "2", null, 1 ) );
		items.add( new WebResourceReference( viewElementBuilder, "4", null, null, null ) );
		List<WebResourceReference> sorted = WebResourceReferenceSorter.sort( items );
		assertEquals( "0", sorted.get( 0 ).getKey() );
		assertEquals( "1", sorted.get( 1 ).getKey() );
		assertEquals( "2", sorted.get( 2 ).getKey() );
		assertEquals( "3", sorted.get( 3 ).getKey() );
		assertEquals( "4", sorted.get( 4 ).getKey() );
	}

	@Test
	public void sortByAfter() {
		List<WebResourceReference> items = new LinkedList<>();
		items.add( new WebResourceReference( viewElementBuilder, "0", null, null, null ) );
		items.add( new WebResourceReference( viewElementBuilder, "2", null, "1", null ) );
		items.add( new WebResourceReference( viewElementBuilder, "3", null, "2", null ) );
		items.add( new WebResourceReference( viewElementBuilder, "1", null, null, 1 ) );
		items.add( new WebResourceReference( viewElementBuilder, "4", null, "3", null ) );
		List<WebResourceReference> sorted = WebResourceReferenceSorter.sort( items );
		assertEquals( "1", sorted.get( 0 ).getKey() );
		assertEquals( "0", sorted.get( 1 ).getKey() );
		assertEquals( "2", sorted.get( 2 ).getKey() );
		assertEquals( "3", sorted.get( 3 ).getKey() );
		assertEquals( "4", sorted.get( 4 ).getKey() );
	}
}
