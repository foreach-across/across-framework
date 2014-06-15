package com.foreach.across.demoweb;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.demoweb.module.DemoWebModule;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.adminweb.AdminWebModuleSettings;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.ehcache.EhcacheModule;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.user.UserModuleSettings;
import com.foreach.across.modules.user.config.UserSchemaConfiguration;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebViewSupport;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class DemoWeb
{
	@Bean
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
		AcrossContext context = new AcrossContext( applicationContext );
		context.setDataSource( acrossDataSource() );
		context.setAllowInstallers( true );

		context.addModule( ehCacheModule() );
		context.addModule( debugWebModule() );
		context.addModule( acrossWebModule() );
		context.addModule( userModule() );
		context.addModule( adminWebModule() );
		context.addModule( acrossHibernateModule() );
		context.addModule( new DemoWebModule() );
		context.addModule( new SpringSecurityModule() );

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

		return hibernateModule;
	}

	@Bean
	public UserModule userModule() {
		UserModule userModule = new UserModule();

		//userModule.setProperty( UserModuleSettings.PASSWORD_ENCODER, NoOpPasswordEncoder.getInstance() );

		SchemaConfiguration schema = userModule.getSchemaConfiguration();
		schema.renameTable( UserSchemaConfiguration.TABLE_PERMISSION, "permissies" );
		schema.renameTable( UserSchemaConfiguration.TABLE_USER, "gebruikers" );

		return userModule;
	}

	/**
	 * Configure the AdminWebModule to prefix all AdminWebControllers with /secure instead of the default /admin.
	 * Rename some of the default user table names.
	 */
	@Bean
	public AdminWebModule adminWebModule() {
		AdminWebModule adminWebModule = new AdminWebModule();
		adminWebModule.setRootPath( "/secure" );
		adminWebModule.setProperty( AdminWebModuleSettings.REMEMBER_ME_KEY, "sdqfjsdklmjsdfmojiondsijqiosdjodj" );

		return adminWebModule;
	}

	@Bean
	public AcrossWebModule acrossWebModule() {
		AcrossWebModule webModule = new AcrossWebModule();
		webModule.setViewsResourcePath( "/static" );
		webModule.setSupportViews( AcrossWebViewSupport.JSP, AcrossWebViewSupport.THYMELEAF );

		// Todo: configure legacyhtml5 just for the heck of it
		webModule.setDevelopmentMode( true );
		webModule.addDevelopmentViews( "", "c:/code/across/across-web/src/main/resources/views/" );
		webModule.addDevelopmentViews( "debugweb", "c:/code/across/debug-web/src/main/resources/views/" );
		webModule.addDevelopmentViews( "ehcache", "c:/code/across/across-ehcache/src/main/resources/views/" );
		webModule.addDevelopmentViews( "adminweb", "c:/code/across/admin-web/src/main/resources/views/" );
		webModule.addDevelopmentViews( "user", "c:/code/across/user-module/src/main/resources/views/" );

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
		/*
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:/hsql/acrossDemoWeb" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );
		*/

		dataSource.setDriverClassName( "com.mysql.jdbc.Driver" );
		dataSource.setUrl( "jdbc:mysql://localhost:3306/across_demoweb" );
		dataSource.setUsername( "demoweb" );
		dataSource.setPassword( "demoweb" );

		return dataSource;
	}
}
