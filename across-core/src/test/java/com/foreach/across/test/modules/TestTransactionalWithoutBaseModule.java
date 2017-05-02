/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.test.modules;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.TransactionManagementConfigurer;
import com.foreach.across.database.support.HikariDataSourceHelper;
import com.foreach.across.test.modules.hibernate1.Hibernate1Module;
import com.foreach.across.test.modules.hibernate1.Product;
import com.foreach.across.test.modules.hibernate1.ProductRepository;
import com.foreach.across.test.modules.hibernate2.Hibernate2Module;
import com.foreach.across.test.modules.hibernate2.User;
import com.foreach.across.test.modules.hibernate2.UserRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestTransactionalWithoutBaseModule.Config.class)
@DirtiesContext
public class TestTransactionalWithoutBaseModule
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

	@Configuration
	static class Config
	{
		@Bean
		public DataSource dataSource() throws Exception {
			return HikariDataSourceHelper.create( "org.hsqldb.jdbc.JDBCDriver", "jdbc:hsqldb:mem:acrosscore", "sa",
			                                      StringUtils.EMPTY );
		}

		@Bean
		public LocalSessionFactoryBean sessionFactory() throws Exception {
			LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
			sessionFactory.setDataSource( dataSource() );
			sessionFactory.setPackagesToScan( "com.foreach.across.test.modules.hibernate1",
			                                  "com.foreach.across.test.modules.hibernate2" );

			Properties p = new Properties();
			p.setProperty( "hibernate.hbm2ddl.auto", "create-drop" );

			sessionFactory.setHibernateProperties( p );

			return sessionFactory;
		}

		@Bean
		public HibernateTransactionManager transactionManager( SessionFactory sessionFactory ) {
			return new HibernateTransactionManager( sessionFactory );
		}

		@Bean
		public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
			return new PersistenceExceptionTranslationPostProcessor();
		}

		@Bean
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			AcrossContext acrossContext = new AcrossContext( applicationContext );
			acrossContext.setDataSource( dataSource() );
			acrossContext.addModule( hibernate1Module() );
			acrossContext.addModule( hibernate2Module() );
			acrossContext.addApplicationContextConfigurer( new TransactionManagementConfigurer(),
			                                               ConfigurerScope.CONTEXT_AND_MODULES );

			acrossContext.bootstrap();

			return acrossContext;
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

