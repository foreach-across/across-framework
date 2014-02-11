package com.foreach.across.core.registry;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.context.AcrossContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * <p>The RefreshableRegistry is a simple bean holding a collection of instances and provides more flexibility than
 * autowiring a list of instances directly.  Depending on the scanModules only members visible in the containing module
 * will be selected (false, default) or all members from all modules (true).  The RefreshableRegistry updates its
 * member list after the AcrossContext has bootstrapped.</p>
 * <p>If members implement the {@link org.springframework.core.Ordered} or {@link org.springframework.core.PriorityOrdered}
 * interface they will be sorted accordingly.</p>
 * <p>Note that a RefreshableRegistry behaves as a set: duplicate members will be ignored.</p>
 *
 * @see com.foreach.across.core.registry.IncrementalRefreshableRegistry
 * @see org.springframework.core.Ordered
 * @see org.springframework.core.PriorityOrdered
 * @see org.springframework.core.OrderComparator
 */
@Refreshable
public class RefreshableRegistry<T>
{
	private Class<T> memberType;
	private boolean scanModules;

	@Autowired
	private AcrossContext across;

	private List<T> members = new ArrayList<T>();
	private Set<T> fixedMembers = new HashSet<T>();

	public RefreshableRegistry( Class<T> memberType ) {
		this( memberType, false );
	}

	public RefreshableRegistry( Class<T> type, boolean scanModules ) {
		this.memberType = type;
		this.scanModules = scanModules;
	}

	/**
	 * Will cause the registry to refresh its members from the AcrossContext.
	 * Also called automatically after the containing module has bootstrapped and after the
	 * entire AcrossContext has bootstrapped.
	 */
	@PostConstruct
	@PostRefresh
	public void refresh() {
		List<T> refreshed = new ArrayList<T>( AcrossContextUtils.getBeansOfType( across, memberType, scanModules ) );

		for ( T fixed : fixedMembers ) {
			if ( !refreshed.contains( fixed ) ) {
				refreshed.add( fixed );
			}
		}

		OrderComparator.sort( refreshed );

		members = refreshed;
	}

	/**
	 * Manually add a member to the registry.  If the same member is already present, it will be ignored.
	 * Manually added members will never be removed by a refresh.
	 *
	 * @param member Member instance to add.
	 */
	public void add( T member ) {
		fixedMembers.add( member );

		if ( !members.contains( member ) ) {
			members.add( member );
		}

		OrderComparator.sort( members );
	}

	/**
	 * Removes a member from the registry.  If the member was not added manually it is possible it will be added
	 * again if a call to {@link #refresh()} is done.
	 *
	 * @param member Member instance to remove.
	 */
	public void remove( T member ) {
		fixedMembers.remove( member );
		members.remove( member );
	}

	/**
	 * @return All current members of the registry.
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
}


