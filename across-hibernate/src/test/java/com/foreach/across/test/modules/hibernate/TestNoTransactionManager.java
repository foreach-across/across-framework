package com.foreach.across.test.modules.hibernate;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.unitofwork.UnitOfWorkFactory;
import com.foreach.across.test.modules.hibernate.hibernate1.Hibernate1Module;
import com.foreach.across.test.modules.hibernate.hibernate1.Product;
import com.foreach.across.test.modules.hibernate.hibernate1.ProductRepository;
import com.foreach.across.test.modules.hibernate.hibernate2.Hibernate2Module;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.HibernateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestNoTransactionManager.Config.class)
@DirtiesContext
public class TestNoTransactionManager
{
	private int productId = 10000;

	@Autowired
	private AcrossHibernateModule hibernateModule;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UnitOfWorkFactory unitOfWork;

	@Test
	public void noTransactionManagerShouldExist() {
		assertTrue( AcrossContextUtils.getApplicationContext( hibernateModule ).getBeansOfType(
				PlatformTransactionManager.class ).isEmpty() );
	}

	@Test(expected = HibernateException.class)
	public void withoutExplicitSessionShouldFail() {
		createAndGetProduct();
	}

	@Test
	public void unitOfWorkShouldWork() {
		unitOfWork.start();

		try {
			createAndGetProduct();
		}
		finally {
			unitOfWork.stop();
		}
	}

	private void createAndGetProduct() {
		productId++;

		assertNull( productRepository.getProductWithId( productId ) );

		Product product = new Product( productId, "product " + productId );
		productRepository.save( product );

		Product other = productRepository.getProductWithId( productId );
		assertNotNull( other );
		assertEquals( product, other );
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
			module.setConfigureUnitOfWorkFactory( true );
			module.setConfigureTransactionManagement( false );
			module.setHibernateProperty( "hibernate.hbm2ddl.auto", "create-drop" );

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
