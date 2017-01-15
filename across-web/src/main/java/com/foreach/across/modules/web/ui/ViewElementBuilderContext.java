package com.foreach.across.modules.web.ui;

import com.foreach.across.core.support.WritableAttributes;

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
}
