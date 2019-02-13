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
import lombok.Getter;

@Getter
public class AddWebResourceRule extends WebResourceRule implements WebResourceRuleKey
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

	public AddWebResourceRule toBucket( String bucket ) {
		this.bucket = bucket;
		return this;
	}

	public AddWebResourceRule before( String key ) {
		this.before = key;
		return this;
	}

	public AddWebResourceRule after( String key ) {
		this.after = key;
		return this;
	}

	public AddWebResourceRule order( Integer order ) {
		this.order = order;
		return this;
	}

	public AddWebResourceRule of( ViewElementBuilder viewElementBuilder ) {
		this.viewElementBuilder = viewElementBuilder;
		return this;
	}

	@Override
	void applyTo( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.add( getBucket(), new WebResourceReference( viewElementBuilder, getKey(), after, before, order, null ) );
	}
}
