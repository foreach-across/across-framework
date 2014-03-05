package com.foreach.across.core.context.configurer;

import com.foreach.across.core.context.beans.ProvidedBeansMap;

import java.util.Map;

/**
 * Allows specifying a ProvidedBeansMap to provide to an ApplicationContext.
 */
public class ProvidedBeansConfigurer extends ApplicationContextConfigurerAdapter
{
	private ProvidedBeansMap providedBeans;

	public ProvidedBeansConfigurer() {
		this( new ProvidedBeansMap() );
	}

	public ProvidedBeansConfigurer( Map<String, Object> beansMap ) {
		this( new ProvidedBeansMap( beansMap ) );
	}

	public ProvidedBeansConfigurer( ProvidedBeansMap providedBeans ) {
		this.providedBeans = providedBeans;
	}

	/**
	 * Returns a map of beans to register directly in the ApplicationContext.
	 * Provided beans will be registered first, before any of the annotated classes
	 * or defined packages are loaded.
	 *
	 * @return Map of bean name and value.
	 * @see com.foreach.across.core.context.beans.ProvidedBeansMap
	 */
	@Override
	public ProvidedBeansMap providedBeans() {
		return providedBeans;
	}

	public void addBean( String name, Object value ) {
		providedBeans.put( name, value );
	}
}
