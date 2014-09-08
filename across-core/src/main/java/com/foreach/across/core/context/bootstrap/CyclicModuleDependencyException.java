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

package com.foreach.across.core.context.bootstrap;

public class CyclicModuleDependencyException extends RuntimeException
{
	private final String module;

	public CyclicModuleDependencyException( String module ) {
		super( "Unable to determine legal module bootstrap order, possible cyclic dependency on module " + module );
		this.module = module;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		CyclicModuleDependencyException that = (CyclicModuleDependencyException) o;

		if ( module != null ? !module.equals( that.module ) : that.module != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return module != null ? module.hashCode() : 0;
	}
}
