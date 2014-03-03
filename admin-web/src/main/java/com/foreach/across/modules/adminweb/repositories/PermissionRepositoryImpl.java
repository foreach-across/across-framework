package com.foreach.across.modules.adminweb.repositories;

import com.foreach.across.modules.adminweb.business.Permission;
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
	public Collection<Permission> getPermissions() {
		return (Collection<Permission>) sessionFactory.getCurrentSession().createCriteria( Permission.class ).list();
	}

	@Transactional(readOnly = true)
	public Permission getPermission( String name ) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria( Permission.class );
		criteria.add( Restrictions.eq( "name", name ) );

		return (Permission) criteria.uniqueResult();
	}

	@Transactional
	public void delete( Permission permission ) {
		sessionFactory.getCurrentSession().delete( permission );
	}

	@Transactional
	public void save( Permission permission ) {
		sessionFactory.getCurrentSession().saveOrUpdate( permission );
	}
}