package com.foreach.across.modules.hibernate;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.hibernate.provider.HibernatePackage;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Exposed
@EnableTransactionManagement
public class HibernateConfiguration
{
	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AcrossHibernateModule module;

	@Bean
	public LocalSessionFactoryBean sessionFactory() throws Exception {
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource( module.getDataSource() );

		HibernatePackage hibernatePackage = module.getHibernatePackage();
		sessionFactory.setAnnotatedClasses( hibernatePackage.getAnnotatedClasses() );
		sessionFactory.setPackagesToScan( hibernatePackage.getPackagesToScan() );
		sessionFactory.setMappingResources( hibernatePackage.getMappingResources() );

		sessionFactory.setHibernateProperties( module.getHibernateProperties() );

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
