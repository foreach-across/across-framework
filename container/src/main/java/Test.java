import com.foreach.across.core.AcrossContext;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@ComponentScan({ "com.foreach.across" })
public class Test
{
	@Bean
	public DataSource installDataSource() throws Exception {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName( "oracle.jdbc.driver.OracleDriver" );
		ds.setUrl( "jdbc:oracle:thin:@192.168.2.215:1522:fe" );
		ds.setUsername( "vkstub" );
		ds.setPassword( "vkstub" );
		ds.setDefaultAutoCommit( true );

		return ds;
	}

	@Bean
	public AcrossContext acrossContext() {
		AcrossContext context = new AcrossContext();
		context.setAllowInstallers( false );

		return context;
	}

	public static void main( String[] args ) throws Exception {
		ApplicationContext ctx = new AnnotationConfigApplicationContext( Test.class );

		//ApplicationContext spring = new ClassPathXmlApplicationContext( "/config/spring-context.xml" );

		//	PathMatchingResourcePatternResolver resolver =
		new PathMatchingResourcePatternResolver( ClassLoader.getSystemClassLoader() );

		//Resource[] resources = resolver.getResources( "classpath*:**/modules.across" );

		/*for ( Resource resource : resources ) {
			Properties props = new Properties();
			props.load( resource.getInputStream() );

			for ( String moduleClass : props.stringPropertyNames() ) {

				Class c = Class.forName( moduleClass );

				AcrossModule module = (AcrossModule) c.newInstance();

				System.out.println( module );
			}
		}*/
	}
}
