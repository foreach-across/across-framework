package com.foreach.across.testweb.other;

import com.foreach.across.modules.web.ui.AbstractWebUiContextRegistry;
import com.foreach.across.testweb.SpecificUiContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
public class SpecificUiContextFactory extends AbstractWebUiContextRegistry<SpecificUiContext>
{
	public Class<?> getObjectType() {
		return SpecificUiContext.class;
	}
}
