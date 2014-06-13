package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.business.User;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public class UserRepositoryImpl implements UserRepository
{
	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Collection<User> getUsers() {
		return (Collection<User>) sessionFactory.getCurrentSession().createCriteria( User.class ).setResultTransformer(
				Criteria.DISTINCT_ROOT_ENTITY ).list();
	}

	@Transactional(readOnly = true)
	@Override
	public User getUserById( long id ) {
		return (User) sessionFactory.getCurrentSession().get( User.class, id );
	}

	@Transactional(readOnly = true)
	@Override
	public User getUserByUsername( String userName ) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria( User.class );
		criteria.add( Restrictions.eq( "username", userName ) );

		return (User) criteria.uniqueResult();
	}

	@Transactional
	@Override
	public void save( User user ) {
		sessionFactory.getCurrentSession().saveOrUpdate( user );
	}

	@Transactional
	@Override
	public void delete( User user ) {
		sessionFactory.getCurrentSession().delete( user );
	}
}
