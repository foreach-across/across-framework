package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.business.PermissionGroup;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository
{
	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Collection<PermissionGroup> getPermissionGroups() {
		return (Collection<PermissionGroup>) sessionFactory.getCurrentSession().createCriteria(
				PermissionGroup.class ).setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY ).list();
	}

	@Transactional(readOnly = true)
	@Override
	public PermissionGroup getPermissionGroup( String groupName ) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria( PermissionGroup.class );
		criteria.add( Restrictions.eq( "name", groupName ) );

		return (PermissionGroup) criteria.uniqueResult();
	}

	@Transactional
	@Override
	public void delete( PermissionGroup permissionGroup ) {
		sessionFactory.getCurrentSession().delete( permissionGroup );
	}

	@Transactional
	@Override
	public void save( PermissionGroup permissionGroup ) {
		sessionFactory.getCurrentSession().saveOrUpdate( permissionGroup );
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Collection<Permission> getPermissions() {
		return (Collection<Permission>) sessionFactory.getCurrentSession().createCriteria( Permission.class ).list();
	}

	@Transactional(readOnly = true)
	@Override
	public Permission getPermission( String name ) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria( Permission.class );
		criteria.add( Restrictions.eq( "name", name ) );

		return (Permission) criteria.uniqueResult();
	}

	@Transactional
	@Override
	public void delete( Permission permission ) {
		sessionFactory.getCurrentSession().delete( permission );
	}

	@Transactional
	@Override
	public void save( Permission permission ) {
		sessionFactory.getCurrentSession().saveOrUpdate( permission );
	}
}
