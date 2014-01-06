package com.foreach.across.modules.debugweb;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.events.AcrossBootstrapFinishedEvent;
import org.springframework.context.ApplicationListener;

public class DebugWebModule extends AcrossModule implements ApplicationListener<AcrossBootstrapFinishedEvent>
{
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
		return "DebugWebModule";
	}

	@Override
	public String getDescription() {
		return "Provides a debug web path and functionality to easily register additional debug controllers.";
	}

	public void onApplicationEvent( AcrossBootstrapFinishedEvent event ) {
		System.out.println( "finished bootstrapping " + event.getModules().size() + " modules" );
	}
}

