package com.foreach.across.core.context.configurer;

import com.foreach.across.core.context.beans.ProvidedBeansMap;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

import java.util.Arrays;

/**
 * Adapter class that implements the ApplicationContextConfigurer interface.
 * Provides default empty implementations for all beans.
 */
public abstract class ApplicationContextConfigurerAdapter implements ApplicationContextConfigurer
{
	/**
	 * Returns a map of beans to register directly in the ApplicationContext.
	 * Provided beans will be registered first, before any of the annotated classes
	 * or defined packages are loaded.
	 *
	 * @return Map of bean name and value.
	 * @see com.foreach.across.core.context.beans.ProvidedBeansMap
	 */
	public ProvidedBeansMap providedBeans() {
		return null;
	}

	/**
	 * Returns a set of annotated classes to register as components in the ApplicationContext.
	 * These can be annotated with @Configuration.
	 *
	 * @return Array of annotated classes.
	 */
	public Class[] annotatedClasses() {
		return new Class[0];
	}

	/**
	 * Return a set of packages that should be scanned for additional components.
	 *
	 * @return Array of package names.
	 */
	public String[] componentScanPackages() {
		return new String[0];
	}

	/**
	 * Returns a set of BeanFactoryPostProcessor instances to apply to the ApplicationContext.
	 *
	 * @return Array of post processor instances.
	 */
	public BeanFactoryPostProcessor[] postProcessors() {
		return new BeanFactoryPostProcessor[0];
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof ApplicationContextConfigurerAdapter ) ) {
			return false;
		}

		ApplicationContextConfigurerAdapter that = (ApplicationContextConfigurerAdapter) o;

		if ( !Arrays.equals( annotatedClasses(), that.annotatedClasses() ) ) {
			return false;
		}
		if ( !Arrays.equals( componentScanPackages(), that.componentScanPackages() ) ) {
			return false;
		}
		if ( !Arrays.equals( postProcessors(), that.postProcessors() ) ) {
			return false;
		}
		if ( !ObjectUtils.equals( providedBeans(), that.providedBeans() ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		Object providedBeans = providedBeans();
		String[] componentScanPackages = componentScanPackages();
		Class<?>[] annotatedClasses = annotatedClasses();
		BeanFactoryPostProcessor[] postProcessors = postProcessors();

		int result = annotatedClasses != null ? Arrays.hashCode( annotatedClasses ) : 0;
		result = 31 * result + ( componentScanPackages != null ? Arrays.hashCode( componentScanPackages ) : 0 );
		result = 31 * result + ( postProcessors != null ? Arrays.hashCode( postProcessors ) : 0 );
		result = 31 * result + ( providedBeans != null ? providedBeans.hashCode() : 0 );
		return result;
	}
}
