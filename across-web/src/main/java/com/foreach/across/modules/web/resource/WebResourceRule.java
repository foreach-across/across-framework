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
import lombok.NonNull;

/**
 * <p>An abstract class that can be used to single {@link #add} a {@link CssWebResourceBuilder}, {@link JavascriptWebResourceBuilder}
 * or {@link ViewElementBuilderSupport}. </p>
 * <p>A {@link WebResourcePackage} can be added with {@link #addPackage(String)}</p>
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
 */
@FunctionalInterface
public interface WebResourceRule
{
	void applyTo( @NonNull WebResourceRegistry webResourceRegistry );

	/**
	 * A factory method which can be used to declare a {@link AddWebResourceRule} and
	 * add a single {@link ViewElementBuilder} to the registry
	 *
	 * @see JavascriptWebResourceBuilder
	 * @see CssWebResourceBuilder
	 * @see WebResourceRegistry
	 */
	static AddWebResourceRule add( @NonNull ViewElementBuilder viewElementBuilder ) {
		return new AddWebResourceRule().of( viewElementBuilder );
	}

	/**
	 * A factory method which can be used to declare a {@link AddPackageResourceRule} and
	 * add a package of web resources to the registry
	 *
	 * @see SimpleWebResourcePackage
	 * @see WebResourceRegistry
	 */
	static AddPackageResourceRule addPackage( @NonNull String packageName ) {
		return new AddPackageResourceRule( packageName );
	}

	/**
	 * A factory method why can be used to remove a web resource from the registry
	 *
	 * @see WebResourceRegistry
	 */
	static RemoveWebResourceRule remove() {
		return new RemoveWebResourceRule();
	}

}
