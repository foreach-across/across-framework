import com.foreach.across.core.AcrossModule;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Properties;

public class Test
{
	public static void main( String[] args ) throws Exception {
		//ApplicationContext spring = new ClassPathXmlApplicationContext( "/config/spring-context.xml" );

		PathMatchingResourcePatternResolver resolver =
				new PathMatchingResourcePatternResolver( ClassLoader.getSystemClassLoader() );

		Resource[] resources = resolver.getResources( "classpath*:**/modules.across" );

		for ( Resource resource : resources ) {
			Properties props = new Properties();
			props.load( resource.getInputStream() );

			for ( String moduleClass : props.stringPropertyNames() ) {

				Class c = Class.forName( moduleClass );

				AcrossModule module = (AcrossModule) c.newInstance();

				System.out.println( module );
			}
		}
	}
}
