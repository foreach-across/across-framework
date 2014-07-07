package com.foreach.across.modules.web.ui;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * <p>
 * The WebUiContextRegistry takes care of request scoping the WebUiContext implementation.
 * This component should be created using a request scope and ScopedProxyMode INTERFACES or TARGET_CLASS.
 * </p>
 * <p>
 * A AbstractWebUiContextHandlerInterceptor must be present to construct the UiContext.
 * </p>
 *
 * @param <T> Specific WebUiContext implementation (use interface).
 */
public abstract class AbstractWebUiContextRegistry<T extends WebUiContext> implements FactoryBean<T>
{
	private T webUiContext;

	void setWebUiContext( T webUiContext ) {
		this.webUiContext = webUiContext;
	}

	public T getObject() {
		return webUiContext;
	}

	public boolean isSingleton() {
		return true;
	}
}
