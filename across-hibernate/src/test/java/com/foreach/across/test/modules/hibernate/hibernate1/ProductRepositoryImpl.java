package com.foreach.across.test.modules.hibernate.hibernate1;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductRepositoryImpl implements ProductRepository
{
	@Autowired
	private SessionFactory sessionFactory;

	@Transactional(readOnly = true)
	public Product getProductWithId( int id ) {
		return (Product) sessionFactory.getCurrentSession().byId( Product.class ).load( id );
	}

	@Transactional
	public void save( Product product ) {
		sessionFactory.getCurrentSession().saveOrUpdate( product );
	}
}
