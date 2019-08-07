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
package com.foreach.across.test;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mock version of a {@link javax.servlet.FilterRegistration.Dynamic} that does nothing but keep a number
 * of configured properties.  Support is limited and only intended in combination with {@link MockAcrossServletContext}.
 *
 * @author Marc Vanbrabant, Arne Vandamme
 */
public class MockFilterRegistration extends AbstractMockRegistration implements FilterRegistration.Dynamic, FilterConfig
{
	public static class MappingRule
	{
		private final boolean isUrlPatterns, matchAfter;
		private final EnumSet<DispatcherType> dispatcherTypes;
		private final String[] namesOrPatterns;

		public MappingRule( boolean isUrlPatterns,
		                    boolean matchAfter,
		                    EnumSet<DispatcherType> dispatcherTypes,
		                    String... namesOrPatterns ) {
			this.isUrlPatterns = isUrlPatterns;
			this.matchAfter = matchAfter;
			this.dispatcherTypes = dispatcherTypes;
			this.namesOrPatterns = namesOrPatterns;
		}

		public String[] getServletNames() {
			return isUrlPatterns ? new String[0] : namesOrPatterns.clone();
		}

		public String[] getUrlPatterns() {
			return isUrlPatterns ? namesOrPatterns.clone() : new String[0];
		}

		public boolean isMatchAfter() {
			return matchAfter;
		}

		public EnumSet<DispatcherType> getDispatcherTypes() {
			return dispatcherTypes;
		}

		public boolean isUrlPatterns() {
			return isUrlPatterns;
		}

		public boolean isServletNames() {
			return !isUrlPatterns;
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			MappingRule that = (MappingRule) o;
			return isUrlPatterns == that.isUrlPatterns &&
					matchAfter == that.matchAfter &&
					Objects.equals( dispatcherTypes, that.dispatcherTypes ) &&
					Arrays.equals( namesOrPatterns, that.namesOrPatterns );
		}

		@Override
		public int hashCode() {
			return Objects.hash( isUrlPatterns, matchAfter, dispatcherTypes, namesOrPatterns );
		}
	}

	private final Filter filter;
	private final Class<? extends Filter> filterClass;

	private final List<MappingRule> mappingRules = new ArrayList<>();

	MockFilterRegistration( MockAcrossServletContext servletContext, String name, Filter filter ) {
		super( servletContext, name, filter.getClass().getName() );

		this.filter = filter;
		this.filterClass = filter.getClass();
	}

	MockFilterRegistration( MockAcrossServletContext servletContext,
	                        String name,
	                        Class<? extends Filter> filterClass ) {
		super( servletContext, name, filterClass.getName() );

		this.filterClass = filterClass;
		this.filter = null;
	}

	MockFilterRegistration( MockAcrossServletContext servletContext, String name, String className ) {
		super( servletContext, name, className );

		this.filter = null;
		this.filterClass = null;
	}

	/**
	 * @return filter name
	 */
	@Override
	public String getFilterName() {
		return getName();
	}

	/**
	 * @return filter instance if configured
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * @return filter class if configured
	 */
	public Class<? extends Filter> getFilterClass() {
		return filterClass;
	}

	/**
	 * @return list of mapping rules in the order they were added
	 */
	public List<MappingRule> getMappingRules() {
		return Collections.unmodifiableList( mappingRules );
	}

	@Override
	public void addMappingForServletNames( EnumSet<DispatcherType> dispatcherTypes,
	                                       boolean isMatchAfter,
	                                       String... servletNames ) {
		mappingRules.add( new MappingRule( false, isMatchAfter, dispatcherTypes, servletNames ) );
	}

	@Override
	public Collection<String> getServletNameMappings() {
		return mappingRules.stream()
		                   .filter( MappingRule::isServletNames )
		                   .map( MappingRule::getServletNames )
		                   .flatMap( Stream::of )
		                   .collect( Collectors.toSet() );
	}

	@Override
	public void addMappingForUrlPatterns( EnumSet<DispatcherType> dispatcherTypes,
	                                      boolean isMatchAfter,
	                                      String... urlPatterns ) {
		mappingRules.add( new MappingRule( true, isMatchAfter, dispatcherTypes, urlPatterns ) );
	}

	@Override
	public Collection<String> getUrlPatternMappings() {
		return mappingRules.stream()
		                   .filter( MappingRule::isUrlPatterns )
		                   .map( MappingRule::getUrlPatterns )
		                   .flatMap( Stream::of )
		                   .collect( Collectors.toSet() );
	}
}
