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

/**
 * <p>An abstract class that can be used to single {@link #add} a {@link CssWebResourceBuilder}, {@link JavascriptWebResourceBuilder}
 * or {@link AbstractWebResourceBuilder}. </p>
 * <p>A {@link WebResourcePackage} can be added with {@link #addPackage(String)}</p>
 *
 * @author Marc Vanbrabant
 * @since 3.1.3
 */
public abstract class WebResourceRule
{
	abstract void applyTo( WebResourceRegistry webResourceRegistry );

	public static AddWebResourceRule add( ViewElementBuilder viewElementBuilder ) {
		return new AddWebResourceRule().of( viewElementBuilder );
	}

	public static AddPackageResourceRule addPackage( String packageName ) {
		return new AddPackageResourceRule( packageName );
	}

	public static RemoveWebResourceRule remove() {
		return new RemoveWebResourceRule();
	}

}
