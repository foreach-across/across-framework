package com.foreach.across.modules.web;

import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.annotations.AcrossEventHandler;
import net.engio.mbassy.listener.Handler;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AcrossEventHandler
public class CallbackHandler
{
	//@Autowired
	//private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Handler
	public void contextBootstrapped( AcrossContextBootstrappedEvent acrossContextBootstrappedEvent ) {
		/*System.out.println("called back!");
		mapping.scan( acrossBootstrapFinishedEvent.getContext().getApplicationContext() );*/

		System.out.println( "re-registering" );

		ApplicationContext applicationContext =
				acrossContextBootstrappedEvent.getContext().getApplicationContext().getParent();
		//applicationContext.getAutowireCapableBeanFactory().createBean( TestMvcConfiguration.class );

		if ( applicationContext.containsBean( "requestMappingHandlerMapping" ) ) {
			applicationContext.getBean( RequestMappingHandlerMapping.class ).afterPropertiesSet();
			System.out.println( "Web MVC beans created..." );
		}




		/*
		RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
		ApplicationContext ctx = acrossBootstrapFinishedEvent.getModule().getApplicationContext();

		ctx.getAutowireCapableBeanFactory().autowireBean( mapping );
		ctx.getAutowireCapableBeanFactory().initializeBean( mapping, "" );
*/
		//System.out.println( acrossBootstrapFinishedEvent.getModule() );
	}
}
