package com.foreach.across.test.modules.hibernate;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.unitofwork.UnitOfWorkFactory;
import com.foreach.across.test.modules.hibernate.hibernate1.Hibernate1Module;
import com.foreach.across.test.modules.hibernate.hibernate1.Product;
import com.foreach.across.test.modules.hibernate.hibernate1.ProductRepository;
import com.foreach.across.test.modules.hibernate.hibernate2.Hibernate2Module;
import com.foreach.across.test.modules.hibernate.hibernate2.User;
import com.foreach.across.test.modules.hibernate.hibernate2.UserRepository;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUnitOfWorkInSameThread.Config.class)
@DirtiesContext
public class TestUnitOfWorkInSameThread
{
	@Autowired
	private UnitOfWorkFactory unitOfWork;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void openSession() {
		unitOfWork.start();
	}

	@After
	public void closeSession() {
		unitOfWork.stop();
	}

	@Test
	public void singleModuleTransactional() {
		assertNull( productRepository.getProductWithId( 1 ) );

		Product product = new Product( 1, "product 1" );
		productRepository.save( product );

		unitOfWork.restart();

		Product other = productRepository.getProductWithId( 1 );
		assertNotNull( other );
		assertEquals( product, other );
	}

	@Test
	public void otherModuleTransactional() {
		assertNull( userRepository.getUserWithId( 1 ) );

		User user = new User( 1, "user 1" );
		userRepository.save( user );

		unitOfWork.restart();

		User other = userRepository.getUserWithId( 1 );
		assertNotNull( other );
		assertEquals( user, other );
	}

	@Test
	public void combinedSave() {
		Product product = new Product( 2, "product 2" );
		User user = new User( 2, "user 2" );

		userRepository.save( user, product );

		unitOfWork.restart();

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

		unitOfWork.restart();

		Product otherProduct = productRepository.getProductWithId( 3 );
		assertNull( otherProduct );
	}

	@Configuration
	static class Config
	{
		@Bean
		public DataSource dataSource() throws Exception {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:acrosscore" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );

			return dataSource;
		}

		@Bean
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			AcrossContext acrossContext = new AcrossContext( applicationContext );
			acrossContext.setDataSource( dataSource() );
			acrossContext.addModule( acrossHibernateModule() );
			acrossContext.addModule( hibernate1Module() );
			acrossContext.addModule( hibernate2Module() );

			return acrossContext;
		}

		@Bean
		public AcrossHibernateModule acrossHibernateModule() {
			AcrossHibernateModule module = new AcrossHibernateModule();
			module.setHibernateProperty( "hibernate.hbm2ddl.auto", "create-drop" );
			module.setConfigureUnitOfWorkFactory( true );

			return module;
		}

		@Bean
		public Hibernate1Module hibernate1Module() {
			return new Hibernate1Module();
		}

		@Bean
		public Hibernate2Module hibernate2Module() {
			return new Hibernate2Module();
		}
	}
}


