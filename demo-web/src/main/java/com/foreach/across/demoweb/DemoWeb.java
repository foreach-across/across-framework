package com.foreach.across.demoweb;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.ehcache.EhcacheModule;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebViewSupport;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DemoWeb
{
	@Bean
	@Autowired
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
		AcrossContext context = new AcrossContext( applicationContext );
		context.setDataSource( acrossDataSource() );
		context.setAllowInstallers( true );

		context.addModule( ehCacheModule() );
		context.addModule( debugWebModule() );
		context.addModule( acrossWebModule() );
		context.addModule( adminWebModule() );
		context.addModule( acrossHibernateModule() );

		return context;
	}

	@Bean
	public EhcacheModule ehCacheModule() {
		EhcacheModule ehcacheModule = new EhcacheModule();
		ehcacheModule.setEnabled( false );

		return ehcacheModule;
	}

	@Bean
	public AcrossHibernateModule acrossHibernateModule() {
		AcrossHibernateModule hibernateModule = new AcrossHibernateModule();
		hibernateModule.setHibernateProperty( AvailableSettings.AUTOCOMMIT, "false" );
		hibernateModule.setHibernateProperty( AvailableSettings.HBM2DDL_AUTO, "validate" );

		return hibernateModule;
	}

	@Bean
	public AdminWebModule adminWebModule() {
		/**
		 * Configure the AdminWebModule to prefix all AdminWebControllers with /secure instead of the default /admin.
		 */
		AdminWebModule adminWebModule = new AdminWebModule();
		adminWebModule.setRootPath( "/secure" );

		return adminWebModule;
	}

	@Bean
	public AcrossWebModule acrossWebModule() {
		AcrossWebModule webModule = new AcrossWebModule();
		webModule.setViewsResourcePath( "/static" );
		webModule.setSupportViews( AcrossWebViewSupport.JSP, AcrossWebViewSupport.THYMELEAF );

		webModule.setDevelopmentMode( true );
		webModule.addDevelopmentViews( "", "c:/code/across/across-web/src/main/resources/views/" );
		webModule.addDevelopmentViews( "debugweb", "c:/code/across/debug-web/src/main/resources/views/" );
		webModule.addDevelopmentViews( "ehcache", "c:/code/across/across-ehcache/src/main/resources/views/" );
		webModule.addDevelopmentViews( "adminweb", "c:/code/across/admin-web/src/main/resources/views/" );

		return webModule;
	}

	@Bean
	public DebugWebModule debugWebModule() {
		DebugWebModule debugWebModule = new DebugWebModule();
		debugWebModule.setRootPath( "/debug" );

		return debugWebModule;
	}

	@Bean
	public DataSource acrossDataSource() throws Exception {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:/hsql/acrossDemoWeb" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		return dataSource;
	}
}
