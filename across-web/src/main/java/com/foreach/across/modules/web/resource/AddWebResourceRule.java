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
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import lombok.Getter;
import lombok.NonNull;

/**
 * A rule which specifies that a {@link ViewElementBuilder} must be added to the {@link WebResourceRegistry}
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
 * @see WebResourceReference
 * @see ViewElementBuilderSupport
 * @see WebResourceRegistry
 *
 */
@Getter
public class AddWebResourceRule implements WebResourceRule
{
	private ViewElementBuilder viewElementBuilder;
	private String after;
	private String before;
	private Integer order;
	private String key;
	private String bucket;

	public AddWebResourceRule withKey( String key ) {
		this.key = key;
		return this;
	}

	public AddWebResourceRule toBucket( @NonNull String bucket ) {
		this.bucket = bucket;
		return this;
	}

	public AddWebResourceRule before( @NonNull String before ) {
		this.before = before;
		return this;
	}

	public AddWebResourceRule after( @NonNull String after ) {
		this.after = after;
		return this;
	}

	public AddWebResourceRule order( int order ) {
		this.order = order;
		return this;
	}

	public AddWebResourceRule of( @NonNull ViewElementBuilder viewElementBuilder ) {
		this.viewElementBuilder = viewElementBuilder;
		return this;
	}

	@Override
	public void applyTo( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.add( getBucket(), new WebResourceReference( viewElementBuilder, getKey(), before, after, order ) );
	}
}
