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
package com.foreach.across.test.modules.web.ui;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementGenerator;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * POC test
 */
public class TestViewElementGenerator
{
	// todo replace by final
	@Test
	public void test() {
		ViewElementGenerator generator = new ViewElementGenerator();
		generator.setItems( Arrays.asList( (Object) "one", (Object) "two" ) );
		generator.setCallback( new ViewElementGenerator.GeneratorCallback()
		{
			@Override
			public ViewElement create( Object item ) {
				return new TextViewElement( (String) item );
			}
		} );

		Set<ViewElement> generated = new HashSet<>();

		for ( ViewElement element : generator ) {
			generated.add( element );

			assertTrue( element instanceof TextViewElement );
		}

		for ( ViewElement repeat : generator ) {
			assertTrue( generated.contains( repeat ) );
		}

		assertEquals( 2, generated.size() );
	}
}
