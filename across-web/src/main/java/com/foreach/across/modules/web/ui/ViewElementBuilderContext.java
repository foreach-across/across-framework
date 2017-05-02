package com.foreach.across.modules.web.ui;

import com.foreach.across.core.support.WritableAttributes;
import com.foreach.across.modules.web.resource.WebResourceUtils;

import java.util.Optional;

public interface ViewElementBuilderContext extends WritableAttributes
{
	/**
	 * Will build a link using the {@link com.foreach.across.modules.web.context.WebAppLinkBuilder} attribute
	 * that is present on this context.  If none is present, the baseLink will remain unmodified.
	 *
	 * @param baseLink to process
	 * @return processed link
	 */
	String buildLink( String baseLink );

	/**
	 * Fetches the global {@link ViewElementBuilderContext}.  Can be bound to the local thread using
	 * {@link ViewElementBuilderContextHolder} or - lacking that - to the request attributes.
	 *
	 * @return optional buildercontext
	 */
	static Optional<ViewElementBuilderContext> retrieveGlobalBuilderContext() {
		return Optional.ofNullable(
				ViewElementBuilderContextHolder
						.getViewElementBuilderContext()
						.orElseGet( () -> WebResourceUtils.currentViewElementBuilderContext().orElse( null ) )
		);
	}
}
