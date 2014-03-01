package com.foreach.across.test.transactional;

import com.foreach.across.test.modules.hibernate1.Product;
import com.foreach.across.test.modules.hibernate1.ProductRepository;
import com.foreach.across.test.modules.hibernate2.User;
import com.foreach.across.test.modules.hibernate2.UserRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.Assert.*;

public abstract class AbstractTransactionalSupportTest
{
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void openSession() {
		Session session = sessionFactory.openSession();
		TransactionSynchronizationManager.bindResource( sessionFactory, new SessionHolder( session ) );
	}

	@After
	public void closeSession() {
		SessionFactoryUtils.closeSession( sessionFactory.getCurrentSession() );
		TransactionSynchronizationManager.unbindResource( sessionFactory );
	}

	@Test
	public void singleModuleTransactional() {
		assertNull( productRepository.getProductWithId( 1 ) );

		Product product = new Product( 1, "product 1" );
		productRepository.save( product );

		closeSession();
		openSession();

		Product other = productRepository.getProductWithId( 1 );
		assertNotNull( other );
		assertEquals( product, other );
	}

	@Test
	public void otherModuleTransactional() {
		assertNull( userRepository.getUserWithId( 1 ) );

		User user = new User( 1, "user 1" );
		userRepository.save( user );

		closeSession();
		openSession();

		User other = userRepository.getUserWithId( 1 );
		assertNotNull( other );
		assertEquals( user, other );
	}

	@Test
	public void combinedSave() {
		Product product = new Product( 2, "product 2" );
		User user = new User( 2, "user 2" );

		userRepository.save( user, product );

		closeSession();
		openSession();

		User otherUser = userRepository.getUserWithId( 2 );
		assertNotNull( otherUser );
		assertEquals( user, otherUser );

		Product otherProduct = productRepository.getProductWithId( 2 );
		assertNotNull( otherProduct );
		assertEquals( product, otherProduct );
	}

	@Test
	public void combinedRollback() {
		Product product = new Product( 3, "product 3" );

		boolean failed = false;

		try {
			userRepository.save( null, product );
		}
		catch ( Exception e ) {
			failed = true;
		}

		assertTrue( failed );

		closeSession();
		openSession();

		Product otherProduct = productRepository.getProductWithId( 3 );
		assertNull( otherProduct );
	}
}
