package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.test.modules.TestContextEventListener;
import com.foreach.across.test.modules.TestEvent;
import com.foreach.across.test.modules.module1.ConstructedBeanModule1;
import com.foreach.across.test.modules.module1.ScannedBeanModule1;
import com.foreach.across.test.modules.module1.ScannedPrototypeBeanModule1;
import com.foreach.across.test.modules.module1.TestModule1;
import com.foreach.across.test.modules.module2.ConstructedBeanModule2;
import com.foreach.across.test.modules.module2.ScannedBeanModule2;
import com.foreach.across.test.modules.module2.TestModule2;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestAcrossContextBoot.Config.class)
@DirtiesContext
public class TestAcrossContextBoot
{
	@Autowired
	private AcrossContext context;

	@Autowired
	private TestModule1 module1;

	@Autowired
	private TestModule2 module2;

	@Autowired
	private ScannedBeanModule1 scannedBeanModule1;

	@Autowired
	private ScannedBeanModule2 scannedBeanModule2;

	@Autowired
	private ConstructedBeanModule1 constructedBeanModule1;

	@Autowired
	private ConstructedBeanModule2 constructedBeanModule2;

	@Autowired
	@Qualifier("refreshable")
	private ConstructedBeanModule1 refreshedBeanModule1;

	@Autowired
	private ScannedPrototypeBeanModule1 prototype1;

	@Autowired
	private ScannedPrototypeBeanModule1 prototype2;

	@Autowired
	@Qualifier("testListener")
	private TestContextEventListener testListener;

	@Value("${general.version}")
	private String version;

	@Test
	public void moduleContextLoadingOrder() {
		assertNotNull( module1 );
		assertNotNull( module2 );

		assertEquals( "versionForAcross", version );
		assertEquals( "versionForModule1", module1.getVersion() );
		assertEquals( "versionForModule2", module2.getVersion() );

		// Beans should only be constructed & (post-)constructed - the counter is incremented for construct & postconstruct
		assertEquals( 2, ScannedBeanModule1.CONSTRUCTION_COUNTER.get() );
		assertEquals( 2, ScannedBeanModule2.CONSTRUCTION_COUNTER.get() );

		// Prototypes should be different but should have module set
		assertNotNull( prototype1 );
		assertNotNull( prototype2 );
		assertNotSame( prototype1, prototype2 );
		assertSame( scannedBeanModule1, prototype1.getScannedBeanModule1() );
		assertSame( scannedBeanModule2, prototype1.getScannedBeanModule2() );
		assertSame( constructedBeanModule2, prototype1.getConstructedBeanModule2() );
		assertSame( scannedBeanModule1, prototype2.getScannedBeanModule1() );

		// As module 1 is initialized before module 2, the bean references from inside module 2 should not be set
		assertNotNull( scannedBeanModule1 );
		assertEquals( "valueForModule1", scannedBeanModule1.getBeanValue() );
		assertTrue( scannedBeanModule1.isPostConstructed() );
		assertSame( module2, scannedBeanModule1.getReferenceToModule2() );
		assertNull( scannedBeanModule1.getReferenceToBeanFromModule2() );

		assertNotNull( constructedBeanModule1 );
		assertEquals( "helloFromModule1", constructedBeanModule1.getText() );
		assertSame( scannedBeanModule1, constructedBeanModule1.getScannedBeanModule1() );
		assertNull( constructedBeanModule1.getScannedBeanModule2() );

		// The refreshable constructed bean in module 1 does hold all references
		assertNotNull( refreshedBeanModule1 );
		assertEquals( "i have been refreshed", refreshedBeanModule1.getText() );
		assertSame( scannedBeanModule1, refreshedBeanModule1.getScannedBeanModule1() );
		assertSame( scannedBeanModule2, refreshedBeanModule1.getScannedBeanModule2() );

		// Module 2 should have the references to beans from module 1
		assertNotNull( scannedBeanModule2 );
		assertSame( module1, scannedBeanModule2.getReferenceToModule1() );
		assertSame( scannedBeanModule1, scannedBeanModule2.getReferenceToBeanFromModule1() );

		assertNotNull( constructedBeanModule2 );
		assertEquals( "helloFromModule2", constructedBeanModule2.getText() );
		assertSame( scannedBeanModule1, constructedBeanModule1.getScannedBeanModule1() );
		assertSame( constructedBeanModule1, constructedBeanModule2.getConstructedBeanModule1() );
	}

	@Test
	public void allAcrossEventHandlersShouldReceiveTheEvents() {
		assertNotNull( scannedBeanModule1 );
		assertNotNull( scannedBeanModule2 );
		assertNotNull( testListener );
		assertNotSame( scannedBeanModule1, testListener );
		assertNotSame( scannedBeanModule2, testListener );

		assertTrue( scannedBeanModule1.getEventsReceived().isEmpty() );
		assertTrue( scannedBeanModule2.getEventsReceived().isEmpty() );
		assertTrue( constructedBeanModule1.getEventsReceived().isEmpty() );
		assertTrue( constructedBeanModule2.getEventsReceived().isEmpty() );
		assertTrue( testListener.getEventsReceived().isEmpty() );

		TestEvent testEvent = new TestEvent();
		context.publishEvent( testEvent );

		assertEquals( 1, scannedBeanModule1.getEventsReceived().size() );
		assertEquals( 1, scannedBeanModule2.getEventsReceived().size() );
		assertEquals( 1, constructedBeanModule1.getEventsReceived().size() );
		assertEquals( 1, constructedBeanModule2.getEventsReceived().size() );
		assertEquals( 1, testListener.getEventsReceived().size() );

		assertSame( testEvent, scannedBeanModule1.getEventsReceived().get( 0 ) );
		assertSame( testEvent, scannedBeanModule2.getEventsReceived().get( 0 ) );
		assertSame( testEvent, constructedBeanModule1.getEventsReceived().get( 0 ) );
		assertSame( testEvent, constructedBeanModule2.getEventsReceived().get( 0 ) );
		assertSame( testEvent, testListener.getEventsReceived().get( 0 ) );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public TestContextEventListener testListener() {
			return new TestContextEventListener();
		}

		@Bean
		public PropertySourcesPlaceholderConfigurer properties() {
			PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
			configurer.setLocation(
					new ClassPathResource( "com/foreach/across/test/TestAcrossContextBoot.properties" ) );

			return configurer;
		}

		@Bean
		public DataSource acrossDataSource() throws Exception {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:acrossTest" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );

			return dataSource;
		}

		@Bean
		@Autowired
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			ScannedBeanModule1.CONSTRUCTION_COUNTER.set( 0 );
			ScannedBeanModule2.CONSTRUCTION_COUNTER.set( 0 );

			AcrossContext context = new AcrossContext( applicationContext );
			context.setDataSource( acrossDataSource() );
			context.setAllowInstallers( true );
			context.addPropertySources( properties() );

			context.addModule( testModule1() );
			context.addModule( testModule2() );

			return context;
		}

		@Bean
		public TestModule1 testModule1() {
			return new TestModule1();
		}

		@Bean
		public TestModule2 testModule2() {
			return new TestModule2();
		}
	}
}
