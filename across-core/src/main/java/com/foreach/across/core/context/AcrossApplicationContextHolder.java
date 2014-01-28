package com.foreach.across.core.context;

public abstract class AcrossApplicationContextHolder
{
	private AcrossApplicationContext acrossApplicationContext;

	AcrossApplicationContext getAcrossApplicationContext() {
		return acrossApplicationContext;
	}

	void setAcrossApplicationContext( AcrossApplicationContext acrossApplicationContext ) {
		this.acrossApplicationContext = acrossApplicationContext;
	}
}
