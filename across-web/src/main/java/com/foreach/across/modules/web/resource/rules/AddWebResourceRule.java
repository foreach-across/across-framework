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
package com.foreach.across.modules.web.resource.rules;

import com.foreach.across.modules.web.resource.WebResourceReference;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceRule;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import lombok.Getter;
import lombok.NonNull;

/**
 * A rule which specifies that a {@link ViewElementBuilder} must be added to a specific bucket of a {@link WebResourceRegistry}.
 * Allows specifying key and order related properties of the resulting {@link WebResourceRegistry}.
 * <p/>
 * Unlike directly calling {@link WebResourceRegistry#addResourceToBucket(WebResourceReference, String)} using this rule will
 * not replace a previously registered resource with the same key, unless {@link #replaceIfPresent(boolean)} has explicitly
 * been set to {@code true}.
 *
 * @author Marc Vanbrabant
 * @see ViewElementBuilder
 * @see WebResourceReference
 * @see WebResourceRegistry
 * @since 3.2.0
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
	private boolean replaceIfPresent;

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

	/**
	 * Should the web resource be replaced if it is already present (based on the key).
	 * Defaults to {@code false}.
	 *
	 * @param replaceIfPresent true if any previously registered resource with that key should be replaced
	 * @return rule
	 */
	public AddWebResourceRule replaceIfPresent( boolean replaceIfPresent ) {
		this.replaceIfPresent = replaceIfPresent;
		return this;
	}

	public AddWebResourceRule of( @NonNull ViewElementBuilder viewElementBuilder ) {
		this.viewElementBuilder = viewElementBuilder;
		return this;
	}

	@Override
	public void applyTo( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addResourceToBucket(
				new WebResourceReference( viewElementBuilder, getKey(), before, after, order ), getBucket(), replaceIfPresent
		);
	}
}
