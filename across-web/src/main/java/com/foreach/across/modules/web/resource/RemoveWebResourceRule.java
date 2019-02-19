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

import lombok.NonNull;

/**
 * A rule which specifies that a {@link WebResourceReference} with a specific key must be removed from the {@link WebResourceRegistry}
 * <p>
 * If a bucket is specified, only remove the item with this key from the specified bucket.
 * If no bucket is specified, remove the item with this key from all buckets.
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
 */
public class RemoveWebResourceRule implements WebResourceRule
{
	private String key;
	private String bucket;

	public RemoveWebResourceRule withKey( @NonNull String key ) {
		this.key = key;
		return this;
	}

	public RemoveWebResourceRule fromBucket( @NonNull String bucket ) {
		this.bucket = bucket;
		return this;
	}

	@Override
	public void applyTo( @NonNull WebResourceRegistry webResourceRegistry ) {
		if ( bucket != null ) {
			webResourceRegistry.removeResourceWithKey( bucket, key );
		}
		else {
			webResourceRegistry.removeResourceWithKey( key );
		}
	}
}
