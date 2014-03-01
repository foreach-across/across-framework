package com.foreach.across.test.modules.hibernatebase;

import com.foreach.across.core.annotations.Exposed;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@Exposed
public class HibernateSessionFactoryConfig
{
	@Autowired
	private HibernateBaseModule baseModule;

	@Bean
	public LocalSessionFactoryBean sessionFactory( DataSource dataSource ) throws Exception {
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource( dataSource );
		sessionFactory.setPackagesToScan(
				baseModule.getPackagesToScan().toArray( new String[baseModule.getPackagesToScan().size()] ) );

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
}
