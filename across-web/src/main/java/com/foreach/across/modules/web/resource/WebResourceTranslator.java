package com.foreach.across.modules.web.resource;

/**
 * Interface used by WebResourceRegistryInterceptor to translate WebResources before rendering.
 */
public interface WebResourceTranslator
{
	/**
	 * Verifies if the WebResource matches for this translator.
	 * Calls to shouldTranslate() should never modify the WebResource.
	 *
	 * @param resource WebResource to inspect if it needs to be translated.
	 * @return True if the resource should be modified.
	 */
	boolean shouldTranslate( WebResource resource );

	/**
	 * Modifies the WebResource.
	 *
	 * @param resource WebResource to inspect and modify.
	 */
	void translate( WebResource resource );
}
