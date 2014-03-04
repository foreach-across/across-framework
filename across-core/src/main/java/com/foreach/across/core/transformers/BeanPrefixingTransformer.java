package com.foreach.across.core.transformers;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>Will add a prefix to all bean names, and will camelCase if required (default).</p>
 * <p><strong>Eg.</strong> with prefix test: sessionFactory would become testSessionFactory</p>
 */
public class BeanPrefixingTransformer extends AbstractBeanRenameTransformer
{
	private final String prefix;
	private final boolean camelCase;

	public BeanPrefixingTransformer( String prefix ) {
		this( prefix, true );
	}

	public BeanPrefixingTransformer( String prefix, boolean camelCase ) {
		this.prefix = prefix;
		this.camelCase = camelCase;
	}

	@Override
	protected String rename( String beanName, Object valueOrDefinition ) {
		return prefix + ( camelCase ? StringUtils.capitalize( beanName ) : beanName );
	}
}
