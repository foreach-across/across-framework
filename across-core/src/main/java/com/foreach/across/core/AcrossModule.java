package com.foreach.across.core;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.filters.BeanFilterComposite;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

public abstract class AcrossModule extends AcrossApplicationContextHolder
{
	private AcrossContext context;

	private BeanFilter exposeFilter = defaultExposeFilter();
	private final Set<ApplicationContextConfigurer> applicationContextConfigurers =
			new HashSet<ApplicationContextConfigurer>();

	public AcrossModule() {
		registerDefaultApplicationContextConfigurers( applicationContextConfigurers );
	}

	public AcrossContext getContext() {
		return context;
	}

	protected void setContext( AcrossContext context ) {
		this.context = context;
	}

	/**
	 * @return The filter that beans should match to exposed to other modules in the AcrossContext.
	 */
	public BeanFilter getExposeFilter() {
		return exposeFilter;
	}

	/**
	 * Sets the filter that will be used after module bootstrap to copy beans to the parent context and make
	 * them available to other modules in the AcrossContext.
	 *
	 * @param exposeFilter The filter that beans should match to exposed to other modules in the AcrossContext.
	 */
	public void setExposeFilter( BeanFilter exposeFilter ) {
		this.exposeFilter = exposeFilter;
	}

	/**
	 * @return Array containing the installer classes in the order of which they should be run.
	 */
	public Object[] getInstallers() {
		return new Object[0];
	}

	/**
	 * Register the default ApplicationContextConfigurers for this module.
	 *
	 * @param contextConfigurers Set of existing configurers to add to.
	 */
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new ComponentScanConfigurer( getClass().getPackage().getName() ) );
	}

	public Set<ApplicationContextConfigurer> getApplicationContextConfigurers() {
		return applicationContextConfigurers;
	}

	public void addApplicationContextConfigurer( ApplicationContextConfigurer configurer ) {
		applicationContextConfigurers.add( configurer );
	}

	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	public abstract String getName();

	/**
	 * @return Description of the content of this module.
	 */
	public abstract String getDescription();

	/**
	 * Called after all modules have been installed and - depending on the registration order in the context -
	 * previous modules have been bootstrapped already.
	 */
	public void bootstrap() {
	}

	/**
	 * Called in case of a context shutdown.  Modules registered after this one in the context will have
	 * been shutdown already.
	 */
	public void shutdown() {
	}

	/**
	 * By default all @Service and @Controller beans are exposed, along with any other beans
	 * annotated explicitly with @Exposed or created through an @Exposed BeanFactory.
	 */
	@SuppressWarnings("unchecked")
	public static BeanFilter defaultExposeFilter() {
		return new BeanFilterComposite( new AnnotationBeanFilter( Service.class, Controller.class ),
		                                new AnnotationBeanFilter( true, Exposed.class ) );
	}
}
