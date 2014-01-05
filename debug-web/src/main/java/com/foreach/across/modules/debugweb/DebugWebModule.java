package com.foreach.across.modules.debugweb;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.events.AcrossBootstrapFinishedEvent;
import org.springframework.context.ApplicationListener;

public class DebugWebModule extends AcrossModule implements ApplicationListener<AcrossBootstrapFinishedEvent>
{
	public static final String NAME = "DebugWebModule";

	private String rootPath = "/debug";

	public void setRootPath( String rootPath ) {
		this.rootPath = rootPath;
	}

	public String getRootPath() {
		return rootPath;
	}

	@Override
	protected Class[] installerClasses() {
		return new Class[] { TestInstaller.class };
	}

	public String getName() {
		return NAME;
	}

	public void onApplicationEvent( AcrossBootstrapFinishedEvent event ) {
		System.out.println( "finished bootstrapping " + event.getModules().size() + " modules" );
	}
}

