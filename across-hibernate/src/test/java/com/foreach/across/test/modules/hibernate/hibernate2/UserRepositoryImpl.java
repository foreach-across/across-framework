package com.foreach.across.test.modules.hibernate.hibernate2;

import com.foreach.across.test.modules.hibernate.hibernate1.Product;
import com.foreach.across.test.modules.hibernate.hibernate1.ProductRepository;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRepositoryImpl implements UserRepository
{
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private ProductRepository productRepository;

	@Transactional(readOnly = true)
	public User getUserWithId( int id ) {
		return (User) sessionFactory.getCurrentSession().byId( User.class ).load( id );
	}

	@Transactional
	public void save( User user ) {
		sessionFactory.getCurrentSession().saveOrUpdate( user );
	}

	@Transactional
	public void save( User user, Product product ) {
		productRepository.save( product );

		if ( user != null ) {
			save( user );
		}
		else {
			throw new RuntimeException( "rollback transaction" );
		}
	}
}
