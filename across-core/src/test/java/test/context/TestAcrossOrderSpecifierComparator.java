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

package test.context;

import com.foreach.across.core.context.AcrossOrderSpecifierComparator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.foreach.across.core.context.support.AcrossOrderSpecifier.builder;
import static org.junit.Assert.assertEquals;

public class TestAcrossOrderSpecifierComparator
{
	private final Object one = new Object();
	private final Object two = new Object();
	private final Object three = new Object();
	private final Object four = new Object();
	private final Object five = new Object();
	private final Object six = new Object();

	private List<Object> beans = new ArrayList<>();
	private AcrossOrderSpecifierComparator comparator = new AcrossOrderSpecifierComparator();

	@Test
	public void beansAccordingToModuleIndex() {
		list( one, three, two );

		comparator.register( one, builder().moduleIndex( 50 ).build() );
		comparator.register( two, builder().moduleIndex( 10 ).build() );
		comparator.register( three, builder().moduleIndex( -1 ).build() );

		assertOrder( three, two, one );
	}

	@Test
	public void orderPrecedesModuleIndex() {
		list( two, three, one, four, six, five );

		comparator.register( one, builder().moduleIndex( 1 ).order( 1 ).build() );
		comparator.register( two, builder().moduleIndex( 2 ).order( 2 ).build() );
		comparator.register( three, builder().moduleIndex( 3 ).order( 3 ).build() );
		comparator.register( four, builder().moduleIndex( 4 ).order( 5 ).build() );
		comparator.register( five, builder().moduleIndex( 5 ).order( 6 ).build() );
		comparator.register( six, builder().moduleIndex( 6 ).order( -1 ).build() );

		assertOrder( six, one, two, three, four, five );
	}

	@Test
	public void moduleIndexPrecedesOrderInModule() {
		list( one, three, two, five, six, four );

		comparator.register( one, builder().moduleIndex( 1 ).orderInModule( 1 ).build() );
		comparator.register( two, builder().moduleIndex( 2 ).orderInModule( 2 ).build() );
		comparator.register( three, builder().moduleIndex( 3 ).orderInModule( 3 ).build() );
		comparator.register( four, builder().moduleIndex( 4 ).orderInModule( 11 ).build() );
		comparator.register( five, builder().moduleIndex( 5 ).orderInModule( 10 ).build() );
		comparator.register( six, builder().moduleIndex( 6 ).orderInModule( 0 ).build() );

		assertOrder( one, two, three, four, five, six );
	}

	private void list( Object... beans ) {
		this.beans = Arrays.asList( beans );
	}

	private void assertOrder( Object... expected ) {
		beans.sort( comparator );
		assertEquals( Arrays.asList( expected ), beans );
	}
}
