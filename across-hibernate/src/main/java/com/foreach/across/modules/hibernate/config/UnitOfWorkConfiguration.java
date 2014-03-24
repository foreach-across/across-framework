package com.foreach.across.modules.hibernate.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.hibernate.unitofwork.UnitOfWorkFactory;
import com.foreach.across.modules.hibernate.unitofwork.UnitOfWorkFactoryImpl;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Configures a UnitOfWorkFactory for the current SessionFactory.
 */
@Configuration
public class UnitOfWorkConfiguration
{
	@Bean
	@Exposed
	public UnitOfWorkFactory unitOfWork( SessionFactory sessionFactory ) {
		return new UnitOfWorkFactoryImpl( Collections.singleton( sessionFactory ) );
	}
}
