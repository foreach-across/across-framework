package com.foreach.across.test.modules.web.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.menu.RequestMenuBuilder;
import com.foreach.across.modules.web.menu.RequestMenuStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITRequestScopedBeans.Config.class)
public class ITRequestScopedBeans
{
	@Autowired(required = false)
	private RequestMenuBuilder requestMenuBuilder;

	@Autowired(required = false)
	private RequestMenuStore requestMenuStore;

	@Test
	public void scopedTargetsCreated() {
		assertNotNull( requestMenuBuilder );
		assertNotNull( requestMenuStore );
	}

	@EnableAcrossContext
	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossWebModule() );
		}
	}
}
