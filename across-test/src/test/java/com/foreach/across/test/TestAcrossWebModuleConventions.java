package com.foreach.across.test;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.modules.web.AcrossWebModule;

/**
 * @author Arne Vandamme
 */
public class TestAcrossWebModuleConventions extends AbstractAcrossModuleConventionsTest
{
	@Override
	protected boolean hasSettings() {
		return true;
	}

	@Override
	protected AcrossModule createModule() {
		return new AcrossWebModule();
	}
}
