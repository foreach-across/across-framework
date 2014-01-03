package com.foreach.across.modules.debugweb;

import com.foreach.across.core.AcrossModule;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("DebugWebModule")
@DependsOn({ "Across", "DebugWebSchemaInstaller" })
public class DebugWebModule implements AcrossModule
{
	@PostConstruct
	public void construct() {
		System.out.println( "hi from debugweb" );
	}
}

