package com.foreach.across.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;

@Configuration(AcrossCoreModule.NAME)
@DependsOn("AcrossCoreInstaller")
public class AcrossCoreModule implements AcrossModule
{
	public static final String NAME = "Across";

	@PostConstruct
	public void construct() {
		System.out.println( "hi from core" );
	}
}
