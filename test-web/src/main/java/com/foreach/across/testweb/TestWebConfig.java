package com.foreach.across.testweb;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.ehcache.EhcacheModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.testweb.other.TestWebOtherModule;
import com.foreach.across.testweb.sub.TestWebModule;
import org.apache.commons.dbcp.BasicDataSource;
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
		context.setAllowInstallers( true );

		context.addModule( otherModule() );
		context.addModule( testWebModule() );
		context.addModule( ehCacheModule() );
		context.addModule( debugWebModule() );
		context.addModule( acrossWebModule() );
		context.addModule( adminWebModule() );

		return context;
	}

	@Bean
	public EhcacheModule ehCacheModule() {
		return new EhcacheModule();
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
	public AdminWebModule adminWebModule() {
		return new AdminWebModule();
	}

	@Bean
	public AcrossWebModule acrossWebModule() {
		AcrossWebModule webModule = new AcrossWebModule();
		webModule.setViewsResourcePath( "/static" );

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
		dataSource.setUrl( "jdbc:hsqldb:/hsql/acrossTestWeb" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		return dataSource;
	}
}
