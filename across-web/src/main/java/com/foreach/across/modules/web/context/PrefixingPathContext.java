package com.foreach.across.modules.web.context;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for relative urls that need a prefix.
 *
 * @see com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping
 */
public class PrefixingPathContext
{
	private final String prefix;

	public PrefixingPathContext( String prefix ) {
		this.prefix = prefix;
	}

	public String path( String path ) {
		return StringUtils.replaceEach( prefix + path, new String[] { "//", "///" }, new String[] { "/", "/" } );
	}

	public String redirect( String path ) {
		return "redirect:" + path( path );
	}
}
