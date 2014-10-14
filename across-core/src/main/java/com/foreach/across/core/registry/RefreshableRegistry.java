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

package com.foreach.across.core.registry;

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * <p>The RefreshableRegistry is a simple bean holding a collection of instances and provides more flexibility than
 * autowiring a list of instances directly.  Depending on the includeModuleInternals only members visible in the
 * containing module will be selected (false, default) or all members from all modules (true).
 * The RefreshableRegistry updates its member list after the AcrossContext has bootstrapped.</p>
 * <p>Members will be returned in the following order:
 * <ul>
 * <li>implementing {@link org.springframework.core.Ordered} or {@link org.springframework.core.annotation.Order}</li>
 * <li>based on bootstrap index of the module that provides the member</li>
 * <li>implementing {@link com.foreach.across.core.OrderedInModule} or {@link com.foreach.across.core.annotations.OrderInModule}</li>
 * <li>any manually added members that were not picked up by the context scan</li>
 * </ul>
 * </p>
 * <p>Note that a RefreshableRegistry behaves as a set: duplicate members will be ignored.</p>
 *
 * @see com.foreach.across.core.registry.IncrementalRefreshableRegistry
 * @see org.springframework.core.Ordered
 * @see org.springframework.core.PriorityOrdered
 * @see org.springframework.core.OrderComparator
 */
@Refreshable
public class RefreshableRegistry<T> implements Collection<T>
{
	private final ResolvableType resolvableType;
	private final boolean includeModuleInternals;

	@Autowired(required = false)
	private AcrossContextBeanRegistry beanRegistry;

	private List<T> members = new ArrayList<T>();
	private Set<T> fixedMembers = new HashSet<T>();

	public RefreshableRegistry( Class<T> memberType ) {
		this( memberType, false );
	}

	public RefreshableRegistry( Class<T> type, boolean includeModuleInternals ) {
		this( ResolvableType.forClass( type ), includeModuleInternals );
	}

	public RefreshableRegistry( ResolvableType resolvableType, boolean includeModuleInternals ) {
		this.resolvableType = resolvableType;
		this.includeModuleInternals = includeModuleInternals;
	}

	/**
	 * Will cause the registry to refresh its members from the AcrossContext.
	 * Also called automatically after the containing module has bootstrapped and after the
	 * entire AcrossContext has bootstrapped.
	 */
	@PostConstruct
	@PostRefresh
	@SuppressWarnings( "unchecked" )
	public void refresh() {
		if ( beanRegistry != null ) {
			List<T> refreshed =
					new ArrayList<>( (List<T>) beanRegistry.getBeansOfType( resolvableType, includeModuleInternals ) );

			// Add fixed members at the end
			for ( T fixed : fixedMembers ) {
				if ( !refreshed.contains( fixed ) ) {
					refreshed.add( fixed );
				}
			}

			members = refreshed;
		}
	}

	/**
	 * Manually add a member to the registry.  If the same member is already present, it will be ignored.
	 * Manually added members will never be removed by a refresh.
	 *
	 * @param member Member instance to add.
	 */
	public boolean add( T member ) {
		fixedMembers.add( member );

		boolean added = false;

		if ( !members.contains( member ) ) {
			added = members.add( member );
		}

		return added;
	}

	public boolean addAll( Collection<? extends T> c ) {
		fixedMembers.addAll( c );

		boolean added = true;

		for ( T member : c ) {
			if ( !members.contains( member ) ) {
				added &= members.add( member );
			}
		}

		return added;
	}

	/**
	 * Removes a member from the registry.  If the member was not added manually it is possible it will be added
	 * again if a call to {@link #refresh()} is done.
	 *
	 * @param member Member instance to remove.
	 */
	public boolean remove( Object member ) {
		fixedMembers.remove( member );
		return members.remove( member );
	}

	/**
	 * @return All manually added members of the registry.
	 */
	public Collection<T> getFixedMembers() {
		return fixedMembers;
	}

	/**
	 * @return All current members of the registry: manually added and scanned.
	 */
	public Collection<T> getMembers() {
		return members;
	}

	public boolean isEmpty() {
		return members.isEmpty();
	}

	public int size() {
		return members.size();
	}

	public boolean contains( Object o ) {
		return members.contains( o );
	}

	public Iterator<T> iterator() {
		return members.iterator();
	}

	public Object[] toArray() {
		return members.toArray();
	}

	public <T1> T1[] toArray( T1[] a ) {
		return members.toArray( a );
	}

	public boolean containsAll( Collection<?> c ) {
		return members.contains( c );
	}

	public boolean removeAll( Collection<?> c ) {
		boolean removed = true;

		for ( Object member : c ) {
			removed &= remove( member );
		}

		return removed;
	}

	public boolean retainAll( Collection<?> c ) {
		fixedMembers.retainAll( c );
		return members.retainAll( c );
	}

	public void clear() {
		fixedMembers.clear();
		members.clear();
	}
}


