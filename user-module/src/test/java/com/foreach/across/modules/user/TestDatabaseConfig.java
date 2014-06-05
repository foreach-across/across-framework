package com.foreach.across.modules.user;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.provider.PackagesToScanProvider;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class TestDatabaseConfig
{
	@Bean
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
		AcrossContext acrossContext = new AcrossContext( applicationContext );
		acrossContext.setDataSource( dataSource() );
		acrossContext.addModule( acrossHibernateModule() );

		return acrossContext;
	}

	@Bean
	public AcrossHibernateModule acrossHibernateModule() {
		AcrossHibernateModule acrossHibernateModule = new AcrossHibernateModule();
		acrossHibernateModule.addHibernatePackageProvider(
				new PackagesToScanProvider( "com.foreach.across.modules.user.business" ) );

		acrossHibernateModule.setHibernateProperty( AvailableSettings.AUTOCOMMIT, "false" );
		acrossHibernateModule.setHibernateProperty( AvailableSettings.HBM2DDL_AUTO, "create-drop" );

		return acrossHibernateModule;
	}

	@Bean
	public DataSource dataSource() throws Exception {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:/hsql/user-module" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		return dataSource;
	}
}
