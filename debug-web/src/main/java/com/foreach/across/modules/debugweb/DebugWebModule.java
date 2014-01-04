package com.foreach.across.modules.debugweb;

import com.foreach.across.core.AcrossCoreModule;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.events.AcrossBootstrapFinishedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component(DebugWebModule.NAME)
@DependsOn({ AcrossCoreModule.NAME })
public class DebugWebModule extends AcrossModule implements ApplicationListener<AcrossBootstrapFinishedEvent>
{
	public static final String NAME = "DebugWebModule";

	@Override
	protected Class[] installerClasses() {
		return new Class[] { TestInstaller.class };
	}

	public String getName() {
		return NAME;
	}

	@Bean
	public ApplicationListener<AcrossBootstrapFinishedEvent> a() {
		return new ApplicationListener<AcrossBootstrapFinishedEvent>()
		{
			public void onApplicationEvent( AcrossBootstrapFinishedEvent event ) {
				System.out.println( "finished as well..." );
			}
		};
	}

	public void onApplicationEvent( AcrossBootstrapFinishedEvent event ) {
		System.out.println( "finished boostrapping " + event.getModules().size() + " modules" );
	}
}

