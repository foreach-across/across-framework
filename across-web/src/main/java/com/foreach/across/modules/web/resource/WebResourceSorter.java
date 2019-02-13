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
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.*;

class WebResourceSorter
{
	public static List<WebResourceReference> sort( @NonNull List<WebResourceReference> items ) {
		List<WebResourceReference> sorted = new LinkedList<>( items );
		sorted.sort( ( o1, o2 ) -> {
			if ( o1.getOrder() != null && o2.getOrder() != null ) {
				return Comparator.comparingInt( WebResourceReference::getOrder ).compare( o1, o2 );
			}
			else {
				return 0;
			}
		} );

		return sortByBeforeOrAfter( sorted );
	}

	private static List<WebResourceReference> sortByBeforeOrAfter( List<WebResourceReference> classes ) {
		List<WebResourceReference> toSort = new ArrayList<>( classes );
		Set<WebResourceReference> sorted = new LinkedHashSet<>();
		Set<WebResourceReference> processing = new LinkedHashSet<>();
		while ( !toSort.isEmpty() ) {
			doSortByAfter( classes, toSort, sorted, processing, null );
		}
		return new ArrayList<>( sorted );
	}

	private static void doSortByAfter( List<WebResourceReference> classes,
	                                   List<WebResourceReference> toSort, Set<WebResourceReference> sorted, Set<WebResourceReference> processing,
	                                   WebResourceReference current ) {
		if ( current == null ) {
			current = toSort.remove( 0 );
		}
		processing.add( current );
		Set<WebResourceReference> afterClasses = getClassesRequestedAfter( classes, current );
		for ( WebResourceReference after : afterClasses ) {
			Assert.state( !processing.contains( after ),
			              "AutoConfigure cycle detected between " + current + " and " + after );
			if ( !sorted.contains( after ) && toSort.contains( after ) ) {
				doSortByAfter( classes, toSort, sorted, processing, after );
			}
		}
		processing.remove( current );
		sorted.add( current );
	}

	private static Set<WebResourceReference> getClassesRequestedAfter( List<WebResourceReference> classes, WebResourceReference after ) {
		Set<WebResourceReference> rtn = new LinkedHashSet<>();
		if ( after.getAfter() != null ) {
			classes.stream().filter( r -> after.getAfter() != null && StringUtils.equals( after.getAfter(), r.getKey() ) ).findFirst().ifPresent( rtn::add );
		}
		for ( WebResourceReference clazz : classes ) {
			if ( clazz.getKey() != null && StringUtils.equals( clazz.getBefore(), after.getKey() ) ) {
				rtn.add( clazz );
			}
		}
		return rtn;
	}
}
