package com.foreach.across.modules.debugweb;

import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.annotations.AcrossEventHandler;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@AcrossEventHandler
public class TestHandler
{
	@Autowired
	private DebugWebModule debugWebModule;

	public TestHandler() {
		System.out.println( "constructed" );
	}

	@PostConstruct
	public void test() {
		System.out.println( "zuuucht" );
	}

	@Handler
	public void onApplicationEvent( AcrossContextBootstrappedEvent event ) {
		System.out.println( "finished as well..." );
	}
}
