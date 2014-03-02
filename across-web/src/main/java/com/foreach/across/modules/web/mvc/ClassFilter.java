package com.foreach.across.modules.web.mvc;

public interface ClassFilter
{
	/**
	 * Checks if a particular class matches against the rules defined in the ClassMatcher implementation.
	 *
	 * @param beanType Class to check.
	 * @return True if the class matches.
	 */
	boolean matches( Class<?> beanType );
}
