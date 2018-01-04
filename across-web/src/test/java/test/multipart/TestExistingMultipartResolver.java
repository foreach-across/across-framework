package test.multipart;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestExistingMultipartResolver.Config.class)
public class TestExistingMultipartResolver
{
	private static MultipartResolver FAKE_RESOLVER = new MultipartResolver()
	{
		@Override
		public boolean isMultipart( HttpServletRequest request ) {
			return false;
		}

		@Override
		public MultipartHttpServletRequest resolveMultipart( HttpServletRequest request ) throws MultipartException {
			return null;
		}

		@Override
		public void cleanupMultipart( MultipartHttpServletRequest request ) {

		}
	};

	@Autowired(required = false)
	private MultipartResolver multipartResolver;

	@Test
	public void existingResolverShouldBeReused() {
		assertNotNull( multipartResolver );
		assertSame( FAKE_RESOLVER, multipartResolver );
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			AcrossWebModule webModule = new AcrossWebModule();
			context.addModule( webModule );
		}

		@Bean
		public MultipartResolver multipartResolver() {
			return FAKE_RESOLVER;
		}
	}
}
