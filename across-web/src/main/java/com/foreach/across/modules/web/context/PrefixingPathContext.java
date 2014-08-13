package com.foreach.across.modules.web.context;

import org.springframework.util.Assert;

/**
 * Helper class for relative urls that need a prefix.
 *
 * @see com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping
 */
public class PrefixingPathContext
{
	private static final String REDIRECT = "redirect:";
	private static final String FORWARD = "forward:";

	private final String prefix;
	private final String[] ignoredPrefixes = new String[] { REDIRECT, FORWARD };

	public PrefixingPathContext( String prefix ) {
		Assert.notNull( prefix );

		if ( prefix.length() > 0 ) {
			if ( prefix.endsWith( "/" ) ) {
				this.prefix = prefix.substring( 0, prefix.length() - 1 );
			}
			else {
				this.prefix = prefix;
			}
		}
		else {
			this.prefix = "";
		}
	}

	/**
	 * @return Root of the prefixed context (no sub path).
	 */
	public String getRoot() {
		return path( "" );
	}

	public String getPathPrefix() {
		return prefix;
	}

	public String path( String path ) {
		for ( String ignoredPrefix : ignoredPrefixes ) {
			if ( path.startsWith( ignoredPrefix ) ) {
				return ignoredPrefix + prefix( path.substring( ignoredPrefix.length() ) );
			}
		}

		return prefix( path );
	}

	public String redirect( String path ) {
		return REDIRECT + path( path.startsWith( REDIRECT ) ? path.substring( 9 ) : path );
	}

	private String prefix( String path ) {
		if ( path.startsWith( "~" ) || path.contains( "://" ) ) {
			return path;
		}

		if ( path.startsWith( "/" ) ) {
			return prefix + path;
		}
		else {
			return prefix + "/" + path;
		}
	}
}
