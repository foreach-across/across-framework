package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossModule;

/**
 * This bean only exists during the bootstrap of an AcrossContext and provides information about
 * the bootstrap in progress.
 */
public class AcrossBootstrapContext
{
	private AcrossModule currentModule;

	public AcrossModule getCurrentModule() {
		return currentModule;
	}

	void setCurrentModule( AcrossModule currentModule ) {
		this.currentModule = currentModule;
	}
}
