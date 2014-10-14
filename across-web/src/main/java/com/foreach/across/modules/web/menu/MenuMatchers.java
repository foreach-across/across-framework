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

package com.foreach.across.modules.web.menu;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.regex.Pattern;

public final class MenuMatchers
{
	private MenuMatchers() {
	}

	public static class PathEqualityMenuMatcher implements MenuMatcher
	{
		private String path;

		public PathEqualityMenuMatcher( String path ) {
			Assert.notNull( path );

			this.path = path;
		}

		@Override
		public boolean matches( Menu menu ) {
			return StringUtils.equals( menu.getPath(), path );
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( !( o instanceof PathEqualityMenuMatcher ) ) {
				return false;
			}

			PathEqualityMenuMatcher that = (PathEqualityMenuMatcher) o;

			if ( path != null ? !path.equals( that.path ) : that.path != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return path != null ? path.hashCode() : 0;
		}
	}

	public static class PathPatternMenuMatcher implements MenuMatcher
	{

		private final Pattern pattern;

		public PathPatternMenuMatcher( Pattern pattern ) {
			this.pattern = pattern;
		}

		@Override
		public boolean matches( Menu menu ) {
			return pattern.matcher( menu.getPath() ).find();
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( !( o instanceof PathPatternMenuMatcher ) ) {
				return false;
			}

			PathPatternMenuMatcher that = (PathPatternMenuMatcher) o;

			if ( pattern != null ? !pattern.equals( that.pattern ) : that.pattern != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return pattern != null ? pattern.hashCode() : 0;
		}
	}

	public static MenuMatcher pathEquals( String path ) {
		return new PathEqualityMenuMatcher( path );
	}

	public static MenuMatcher pathMatches( String pattern ) {
		return pathMatches( pattern, false );
	}

	public static MenuMatcher pathMatches( String pattern, boolean ignoreCase ) {
		int flags = ( ignoreCase ? Pattern.CASE_INSENSITIVE : 0 );
		return pathMatches( Pattern.compile( pattern, flags ) );
	}

	public static MenuMatcher pathMatches( Pattern pattern ) {
		return new PathPatternMenuMatcher( pattern );
	}
}
