package com.foreach.across.core.context;

import com.foreach.across.core.context.beans.ProvidedBeansMap;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Interface the ApplicationContext must implement for Across to be able to use it.
 */
public interface AcrossConfigurableApplicationContext
{
	ConfigurableEnvironment getEnvironment();

	void provide( ProvidedBeansMap... beans );

	void addBeanFactoryPostProcessor( BeanFactoryPostProcessor postProcessor );

	void register( Class<?>... annotatedClasses );

	void scan( String... basePackages );

	void refresh();

	void start();
}
