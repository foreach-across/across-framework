package com.foreach.across.modules.hibernate.config;

import com.foreach.across.core.annotations.Exposed;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TransactionManagerConfiguration
{
	@Bean
	@Exposed
	public HibernateTransactionManager transactionManager( SessionFactory sessionFactory ) {
		return new HibernateTransactionManager( sessionFactory );
	}
}
