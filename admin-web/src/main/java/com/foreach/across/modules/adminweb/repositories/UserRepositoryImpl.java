package com.foreach.across.modules.adminweb.repositories;

import com.foreach.across.modules.adminweb.business.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserRepositoryImpl implements UserRepository
{
	@Autowired
	private SessionFactory sessionFactory;

	@Transactional(readOnly = true)
	@Override
	public User getUserById( long id ) {
		return (User) sessionFactory.getCurrentSession().get( User.class, id );
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
