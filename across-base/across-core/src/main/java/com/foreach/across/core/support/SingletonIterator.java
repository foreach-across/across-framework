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
package com.foreach.across.core.support;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Arne Vandamme
 */
public class SingletonIterator<T> implements Iterator<T>
{
	private boolean done;
	private T value;

	public SingletonIterator( T value ) {
		this.value = value;
	}

	@Override
	public boolean hasNext() {
		return !done;
	}

	@Override
	public T next() {
		if ( done ) {
			throw new NoSuchElementException();
		}
		done = true;
		return value;
	}

	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}
}
