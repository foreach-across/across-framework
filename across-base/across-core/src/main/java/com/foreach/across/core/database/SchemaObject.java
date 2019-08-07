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

package com.foreach.across.core.database;

public class SchemaObject
{
	private final String key;
	private final String originalName;
	private String currentName;

	public SchemaObject( String key, String originalName ) {
		this.key = key;
		this.originalName = originalName;
		this.currentName = originalName;
	}

	public String getKey() {
		return key;
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getCurrentName() {
		return currentName;
	}

	public void setCurrentName( String currentName ) {
		this.currentName = currentName;
	}
}
