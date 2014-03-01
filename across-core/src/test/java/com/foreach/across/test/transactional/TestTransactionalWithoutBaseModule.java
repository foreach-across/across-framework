package com.foreach.across.test.transactional;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.TransactionManagementConfigurer;
import com.foreach.across.test.modules.hibernate1.Hibernate1Module;
import com.foreach.across.test.modules.hibernate2.Hibernate2Module;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.junit.runner.RunWith;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestTransactionalWithoutBaseModule.Config.class)
@DirtiesContext
public class TestTransactionalWithoutBaseModule extends AbstractTransactionalSupportTest
{
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
			acrossContext.addApplicationContextConfigurer( new TransactionManagementConfigurer(), true );

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

