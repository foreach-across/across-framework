package com.foreach.across.modules.web;

import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class CallbackHandler implements ApplicationListener<AcrossContextBootstrappedEvent>
{
	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;


	public void onApplicationEvent( AcrossContextBootstrappedEvent acrossContextBootstrappedEvent ) {
		/*System.out.println("called back!");
		mapping.scan( acrossBootstrapFinishedEvent.getContext().getApplicationContext() );*/

		requestMappingHandlerMapping.afterPropertiesSet();
		/*
		RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
		ApplicationContext ctx = acrossBootstrapFinishedEvent.getModule().getApplicationContext();

		ctx.getAutowireCapableBeanFactory().autowireBean( mapping );
		ctx.getAutowireCapableBeanFactory().initializeBean( mapping, "" );
*/
		//System.out.println( acrossBootstrapFinishedEvent.getModule() );
	}
}
