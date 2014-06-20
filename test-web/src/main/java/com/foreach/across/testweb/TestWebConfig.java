package com.foreach.across.testweb;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.ehcache.EhcacheModule;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebViewSupport;
import com.foreach.across.testweb.other.TestWebOtherModule;
import com.foreach.across.testweb.sub.TestWebModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TestWebConfig
{
	@Bean
	@Autowired
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
		AcrossContext context = new AcrossContext( applicationContext );
		context.setDataSource( acrossDataSource() );
		context.setAllowInstallers( false );

		context.addModule( otherModule() );
		context.addModule( testWebModule() );
		context.addModule( ehCacheModule() );
		context.addModule( debugWebModule() );
		context.addModule( acrossWebModule() );
		//context.addModule( adminWebModule() );
		//context.addModule( acrossHibernateModule() );

		return context;
	}

	@Bean
	public EhcacheModule ehCacheModule() {
		EhcacheModule ehcacheModule = new EhcacheModule();
		ehcacheModule.setEnabled( true );
		ehcacheModule.setProperty( "property-in-ehcache", "ehcache-value" );

		return ehcacheModule;
	}

	@Bean
	public TestWebOtherModule otherModule() {
		return new TestWebOtherModule();
	}

	@Bean
	public TestWebModule testWebModule() {
		return new TestWebModule();
	}

	@Bean
	public AcrossHibernateModule acrossHibernateModule() {
		AcrossHibernateModule hibernateModule = new AcrossHibernateModule();
		hibernateModule.setHibernateProperty( AvailableSettings.AUTOCOMMIT, "false" );

		return hibernateModule;
	}

	@Bean
	public AdminWebModule adminWebModule() {
		return new AdminWebModule();
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

		return webModule;
	}

	@Bean
	public DebugWebModule debugWebModule() {
		DebugWebModule debugWebModule = new DebugWebModule();
		debugWebModule.setRootPath( "/debug" );
		debugWebModule.setProperty( "property-in-debugweb", "debugweb-value" );

		return debugWebModule;
	}

	@Bean
	public DataSource acrossDataSource() throws Exception {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:/hsql/acrossTestWeb" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		//menuRegistry.put( new SortedMenu( "debugMenu", ));

		return dataSource;
	}
}
