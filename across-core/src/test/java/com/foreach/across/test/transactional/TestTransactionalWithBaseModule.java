package com.foreach.across.test.transactional;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.test.modules.hibernate1.Hibernate1Module;
import com.foreach.across.test.modules.hibernate2.Hibernate2Module;
import com.foreach.across.test.modules.hibernatebase.HibernateBaseModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.runner.RunWith;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestTransactionalWithBaseModule.Config.class)
@DirtiesContext
public class TestTransactionalWithBaseModule extends AbstractTransactionalSupportTest
{
	@Configuration
	static class Config
	{
		@Bean
		public DataSource dataSource() throws Exception {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:acrosscore" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );

			return dataSource;
		}

		@Bean
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			AcrossContext acrossContext = new AcrossContext( applicationContext );
			acrossContext.setDataSource( dataSource() );
			acrossContext.addModule( hibernateBaseModule() );
			acrossContext.addModule( hibernate1Module() );
			acrossContext.addModule( hibernate2Module() );

			return acrossContext;
		}

		@Bean
		public HibernateBaseModule hibernateBaseModule() {
			return new HibernateBaseModule();
		}

		@Bean
		public Hibernate1Module hibernate1Module() {
			return new Hibernate1Module();
		}

		@Bean
		public Hibernate2Module hibernate2Module() {
			return new Hibernate2Module();
		}
	}
}


