package com.foreach.across.modules.debugweb;

import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TestHandler implements ApplicationListener<AcrossContextBootstrappedEvent>
{
	@Autowired
	private DebugWebModule debugWebModule;

	public TestHandler() {
		System.out.println("constructed");
	}

	@PostConstruct
	public void test(){
		System.out.println("zuuucht");
	}

	public void onApplicationEvent( AcrossContextBootstrappedEvent event ) {
		System.out.println( "finished as well..." );
	}
}
