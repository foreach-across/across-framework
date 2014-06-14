package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@PropertySource(value = "file:${user.home}/dev-configs/across-test.properties", ignoreResourceNotFound = true)
public class AcrossTestContextConfiguration
{
	@Autowired
	private Environment environment;

	@Bean
	public DataSource dataSource() throws Exception {
		BasicDataSource dataSource = new BasicDataSource();

		String dsName = System.getProperty( "acrossTest.datasource", null );

		if ( dsName == null ) {
			dsName = environment.getProperty( "acrossTest.datasource.default", "auto" );
		}

		System.out.println( "Creating Across test datasource with profile: " + dsName );

		if ( StringUtils.equals( "auto", dsName ) ) {
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:/hsql-mem/across-test" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );
		}
		else {
			dataSource.setDriverClassName(
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".driver" ) );
			dataSource.setUrl( environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".url" ) );
			dataSource.setUsername(
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".username" ) );
			dataSource.setPassword(
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".password" ) );
		}

		return dataSource;
	}

	@Bean
	public SpringLiquibase databaseReset() throws Exception {
		SpringLiquibase springLiquibase = new SpringLiquibase();
		springLiquibase.setDataSource( dataSource() );
		springLiquibase.setChangeLog( "classpath:com/foreach/across/test/resetDatabase.xml" );
		springLiquibase.setDropFirst( true );

		return springLiquibase;
	}

	@Bean
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
		databaseReset();

		Map<String, AcrossTestContextConfigurer> configurerMap =
				applicationContext.getBeansOfType( AcrossTestContextConfigurer.class );

		AcrossContext context = new AcrossContext( applicationContext );
		context.setAllowInstallers( true );
		context.setDataSource( dataSource() );

		for ( AcrossTestContextConfigurer configurer : configurerMap.values() ) {
			configurer.configure( context );
		}

		return context;
	}
}
