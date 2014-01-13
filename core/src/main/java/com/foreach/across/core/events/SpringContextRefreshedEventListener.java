package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.util.AcrossContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Takes care of actions that need to happen whenever the parent Spring ApplicationContext
 * of the AcrossContext is refreshed.
 * <p/>
 * Actions include:
 * <ul>
 * <li>updating all @Refreshable components</li>
 * <li>scanning for and registering new @AcrossEventHandler beans</li>
 * </ul>
 */
public class SpringContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent>
{
	@Autowired
	private AcrossContext context;

	@Autowired
	private AcrossEventPublisher publisher;

	@PostConstruct
	private void registerInParentContext() {
		( (ConfigurableApplicationContext) context.getApplicationContext().getParent() ).addApplicationListener( this );
	}

	public void onApplicationEvent( ContextRefreshedEvent event ) {
		if ( event.getApplicationContext() == context.getApplicationContext().getParent() ) {
			// Scan for event handlers
			AcrossContextUtil.autoRegisterEventHandlers( event.getApplicationContext(), publisher );

			// Refresh the different beans
			AcrossContextUtil.refreshBeans( context );
		}
	}
}
