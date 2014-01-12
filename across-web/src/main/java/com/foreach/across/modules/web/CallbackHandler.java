package com.foreach.across.modules.web;

import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


public class CallbackHandler implements ApplicationListener<AcrossContextBootstrappedEvent>
{
	//@Autowired
	//private RequestMappingHandlerMapping requestMappingHandlerMapping;


	public void onApplicationEvent( AcrossContextBootstrappedEvent acrossContextBootstrappedEvent ) {
		/*System.out.println("called back!");
		mapping.scan( acrossBootstrapFinishedEvent.getContext().getApplicationContext() );*/

		System.out.println("re-registering");

		ApplicationContext applicationContext = acrossContextBootstrappedEvent.getContext().getApplicationContext().getParent();
		//applicationContext.getAutowireCapableBeanFactory().createBean( TestMvcConfiguration.class );

		if ( applicationContext.containsBean( "requestMappingHandlerMapping" ) ) {
			applicationContext.getBean( RequestMappingHandlerMapping.class ).afterPropertiesSet();
			System.out.println("Web MVC beans created...");
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
