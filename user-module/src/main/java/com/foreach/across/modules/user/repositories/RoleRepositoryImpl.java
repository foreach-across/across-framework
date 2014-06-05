package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.business.Role;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public class RoleRepositoryImpl implements RoleRepository
{
	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Collection<Role> getRoles() {
		return (Collection<Role>) sessionFactory.getCurrentSession().createCriteria( Role.class ).setResultTransformer(
				Criteria.DISTINCT_ROOT_ENTITY ).list();
	}

	@Transactional(readOnly = true)
	@Override
	public Role getRole( String name ) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria( Role.class );
		criteria.add( Restrictions.eq( "name", name ) );

		return (Role) criteria.uniqueResult();
	}

	@Transactional
	@Override
	public void delete( Role role ) {
		sessionFactory.getCurrentSession().delete( role );
	}

	@Transactional
	@Override
	public void save( Role role ) {
		sessionFactory.getCurrentSession().saveOrUpdate( role );
	}
}
