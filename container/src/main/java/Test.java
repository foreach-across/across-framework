import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossCoreModule;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.modules.debugweb.DebugWebModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class Test
{
	@Bean
	public DataSource installDataSource() throws Exception {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName( "oracle.jdbc.driver.OracleDriver" );
		ds.setUrl( "jdbc:oracle:thin:@192.168.2.215:1522:fe" );
		ds.setUsername( "mmevk" );
		ds.setPassword( "mmevk" );
		ds.setDefaultAutoCommit( true );

		return ds;
	}

	@Bean
	public AcrossContext acrossContext() {
		AcrossContext context = new AcrossContext();
		context.setAllowInstallers( false );

		context.addModule( debugWebModule() );

		return context;
	}

	@Bean
	public DebugWebModule debugWebModule() {
		return new DebugWebModule();
	}
	public static void main( String[] args ) throws Exception {
		ApplicationContext ctx = new AnnotationConfigApplicationContext( Test.class );
	}
}
