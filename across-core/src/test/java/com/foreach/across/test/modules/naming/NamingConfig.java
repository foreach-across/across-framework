package com.foreach.across.test.modules.naming;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NamingConfig
{
	@Autowired
	private AcrossContext autoAcrossContext;

	@Autowired
	@Qualifier( AcrossContext.BEAN)
	private AcrossContext specificAcrossContext;

	@Autowired
	private NamingModule autoNamingModule;

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private NamingModule currentModule;

	@Autowired
	@Qualifier("FirstModule")
	private NamingModule moduleNamedFirst;

	@Autowired
	@Qualifier("LastModule")
	private NamingModule moduleNamedLast;

	public AcrossContext getAutoAcrossContext() {
		return autoAcrossContext;
	}

	public AcrossContext getSpecificAcrossContext() {
		return specificAcrossContext;
	}

	public NamingModule getAutoNamingModule() {
		return autoNamingModule;
	}

	public NamingModule getCurrentModule() {
		return currentModule;
	}

	public NamingModule getModuleNamedFirst() {
		return moduleNamedFirst;
	}

	public NamingModule getModuleNamedLast() {
		return moduleNamedLast;
	}
}
