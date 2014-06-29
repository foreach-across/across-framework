package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.ApplicationContextScanner;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.events.AcrossEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.debugweb.util.ContextDebugInfo;
import gigadot.rebound.Rebound;
import net.engio.mbassy.listener.Handler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.*;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.*;

@DebugWebController
public class AcrossInfoController
{
	@Autowired
	private AcrossContextInfo acrossContext;

	@Autowired
	private AcrossEventPublisher publisher;

	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.builder()
		     .group( "/across", "Across" ).and()
		     .item( "/across/browser", "Context browser", "/across/browser/info/0" ).order(
				Ordered.HIGHEST_PRECEDENCE );
	}

	@ModelAttribute("contexts")
	public List<ContextDebugInfo> contextDebugInfoList() {
		return ContextDebugInfo.create( acrossContext );
	}

	@RequestMapping("/across/browser/info/{index}")
	public String showContextInfo( @ModelAttribute("contexts") List<ContextDebugInfo> contexts,
	                               @PathVariable("index") int index,
	                               Model model ) {
		ContextDebugInfo selected = contexts.get( index );

		model.addAttribute( "selectedContextIndex", contexts.indexOf( selected ) );
		model.addAttribute( "selectedContext", selected );
		model.addAttribute( "section", "info" );
		model.addAttribute( "sectionTemplate", DebugWeb.VIEW_BROWSER_INFO );

		return DebugWeb.LAYOUT_BROWSER;
	}

	@RequestMapping("/across/browser/beans/{index}")
	public String showContextBeans( @ModelAttribute("contexts") List<ContextDebugInfo> contexts,
	                                @PathVariable("index") int index,
	                                Model model ) {
		ContextDebugInfo selected = contexts.get( index );

		Collection<BeanInfo> beans = buildBeanSet( selected );

		int exposedCount = 0;
		for ( BeanInfo beanInfo : beans ) {
			if ( beanInfo.isExposed() ) {
				exposedCount++;
			}
		}

		model.addAttribute( "contextBeans", beans );
		model.addAttribute( "totalBeanCount", beans.size() );
		model.addAttribute( "exposedBeanCount", exposedCount );

		model.addAttribute( "selectedContextIndex", contexts.indexOf( selected ) );
		model.addAttribute( "selectedContext", selected );
		model.addAttribute( "section", "beans" );
		model.addAttribute( "sectionTemplate", DebugWeb.VIEW_BROWSER_BEANS );

		return DebugWeb.LAYOUT_BROWSER;
	}

	@RequestMapping("/across/browser/properties/{index}")
	public String showContextProperties( @ModelAttribute("contexts") List<ContextDebugInfo> contexts,
	                                     @PathVariable("index") int index,
	                                     Model model ) {
		ContextDebugInfo selected = contexts.get( index );

		model.addAttribute( "propertySources", getPropertySources( selected.getEnvironment() ) );

		model.addAttribute( "selectedContextIndex", contexts.indexOf( selected ) );
		model.addAttribute( "selectedContext", selected );
		model.addAttribute( "section", "properties" );
		model.addAttribute( "sectionTemplate", DebugWeb.VIEW_BROWSER_PROPERTIES );

		return DebugWeb.LAYOUT_BROWSER;
	}

	static class PropertySourceInfo
	{
		private PropertySource propertySource;
		private Collection<PropertyInfo> properties;

		PropertySourceInfo( PropertySource propertySource ) {
			this.propertySource = propertySource;
		}

		public String getName() {
			return propertySource.getName();
		}

		public String getPropertySourceType() {
			return propertySource.getClass().getCanonicalName();
		}

		public Collection<PropertyInfo> getProperties() {
			return properties;
		}

		public void setProperties( Collection<PropertyInfo> properties ) {
			this.properties = properties;
		}

		public boolean isSystemSource() {
			return org.apache.commons.lang3.StringUtils.equalsIgnoreCase( "systemproperties",
			                                                              propertySource.getName() );
		}

		public boolean isEnvironmentSource() {
			return org.apache.commons.lang3.StringUtils.equalsIgnoreCase( "systemenvironment",
			                                                              propertySource.getName() );
		}

		public boolean isEnumerable() {
			return propertySource instanceof EnumerablePropertySource;
		}

		public boolean isEmpty() {
			return properties == null || properties.isEmpty();
		}
	}

	static class PropertyInfo implements Comparable<PropertyInfo>
	{
		private String name;
		private Environment environment;
		private PropertySource propertySource;

		PropertyInfo( String name, Environment environment, PropertySource propertySource ) {
			this.name = name;
			this.environment = environment;
			this.propertySource = propertySource;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return propertySource.getProperty( name );
		}

		public Object getEnvironmentValue() {
			return environment.getProperty( name, Object.class );
		}

		public boolean isActualValue() {
			return ObjectUtils.equals( getValue(), getEnvironmentValue() );
		}

		public boolean isSystemProperty() {
			return org.apache.commons.lang3.StringUtils.equalsIgnoreCase( "systemproperties",
			                                                              propertySource.getName() );
		}

		public boolean isEnvironmentProperty() {
			return org.apache.commons.lang3.StringUtils.equalsIgnoreCase( "systemenvironment",
			                                                              propertySource.getName() );
		}

		@Override
		public int compareTo( PropertyInfo o ) {
			return getName().compareTo( o.getName() );
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			PropertyInfo that = (PropertyInfo) o;

			if ( name != null ? !name.equals( that.name ) : that.name != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return name != null ? name.hashCode() : 0;
		}
	}

	private Collection<PropertySourceInfo> getPropertySources( Environment environment ) {
		LinkedList<PropertySourceInfo> sources = new LinkedList<>();

		if ( environment instanceof ConfigurableEnvironment ) {
			MutablePropertySources propertySources = ( (ConfigurableEnvironment) environment ).getPropertySources();
			for ( PropertySource<?> propertySource : propertySources ) {
				PropertySourceInfo sourceInfo = new PropertySourceInfo( propertySource );

				if ( propertySource instanceof EnumerablePropertySource ) {
					EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
					List<PropertyInfo> properties = new ArrayList<>();

					for ( String propertyName : enumerablePropertySource.getPropertyNames() ) {
						properties.add( new PropertyInfo( propertyName, environment, enumerablePropertySource ) );
					}

					Collections.sort( properties );
					sourceInfo.setProperties( properties );
				}

				sources.addFirst( sourceInfo );
			}
		}

		return sources;
	}

	@RequestMapping("/across/browser/events/{index}")
	public String showContextEvents( @ModelAttribute("contexts") List<ContextDebugInfo> contexts,
	                                 @PathVariable("index") int index,
	                                 Model model ) {
		ContextDebugInfo selected = contexts.get( index );

		String modulePackage = null;

		if ( selected.isModule() ) {
			modulePackage = selected.getModuleInfo().getModule().getClass().getPackage().getName();
		}
		else if ( selected.isAcrossContext() ) {
			modulePackage = AcrossContext.class.getPackage().getName();
		}

		if ( modulePackage != null ) {
			Rebound rebound = new Rebound( modulePackage );
			Set<Class<? extends AcrossEvent>> eventTypes = rebound.getSubClassesOf( AcrossEvent.class );

			model.addAttribute( "modulePackage", modulePackage );
			model.addAttribute( "eventTypes", eventTypes );
		}

		model.addAttribute( "selectedContextIndex", contexts.indexOf( selected ) );
		model.addAttribute( "selectedContext", selected );
		model.addAttribute( "section", "events" );
		model.addAttribute( "sectionTemplate", DebugWeb.VIEW_BROWSER_EVENTS );

		return DebugWeb.LAYOUT_BROWSER;
	}

	@RequestMapping("/across/browser/handlers/{index}")
	public String showContextHandlers( @ModelAttribute("contexts") List<ContextDebugInfo> contexts,
	                                   @PathVariable("index") int index,
	                                   Model model ) {
		ContextDebugInfo selected = contexts.get( index );

		Collection<BeanInfo> beans = buildBeanSet( selected );
		Collection<BeanInfo> handlers = new LinkedList<>();

		for ( BeanInfo beanInfo : beans ) {
			if ( beanInfo.isEventHandler() ) {
				handlers.add( beanInfo );
			}
		}

		model.addAttribute( "eventHandlers", handlers );

		model.addAttribute( "selectedContextIndex", contexts.indexOf( selected ) );
		model.addAttribute( "selectedContext", selected );
		model.addAttribute( "section", "handlers" );
		model.addAttribute( "sectionTemplate", DebugWeb.VIEW_BROWSER_HANDLERS );

		return DebugWeb.LAYOUT_BROWSER;
	}

	private Collection<BeanInfo> buildBeanSet( ContextDebugInfo context ) {
		List<BeanInfo> beans = new LinkedList<>();

		if ( context.isEnabled() ) {

			Set<String> exposed = context.isModule() ? getExposedBeanNames(
					context.getModuleInfo().getModule() ) : Collections.<String>emptySet();

			BeanFactory autowireCapableBeanFactory = context.getApplicationContext().getAutowireCapableBeanFactory();

			if ( autowireCapableBeanFactory instanceof ConfigurableListableBeanFactory ) {
				ConfigurableListableBeanFactory beanFactory =
						(ConfigurableListableBeanFactory) autowireCapableBeanFactory;

				String[] definitions = beanFactory.getBeanDefinitionNames();
				Set<String> names = new HashSet<>();
				names.addAll( Arrays.asList( definitions ) );
				names.addAll( Arrays.asList( beanFactory.getSingletonNames() ) );

				for ( String name : names ) {
					BeanInfo info = new BeanInfo();
					info.setName( name );
					info.setExposed( exposed.contains( name ) );

					Class beanType = Object.class;
					Class actual;

					if ( ArrayUtils.contains( definitions, name ) ) {
						BeanDefinition definition = beanFactory.getBeanDefinition( name );
						info.setSingleton( definition.isSingleton() );
						info.setScope( definition.getScope() );

						try {
							if ( beanFactory.isSingleton( name ) ) {
								Object value = beanFactory.getSingleton( name );
								info.setInstance( value );
								actual = ClassUtils.getUserClass( AopProxyUtils.ultimateTargetClass( value ) );

								if ( value != null ) {
									beanType = value.getClass();
								}
							}
							else {
								beanType = Class.forName( definition.getBeanClassName() );
								actual = ClassUtils.getUserClass( beanType );
							}
						}
						catch ( Exception e ) {
							beanType = null;
							actual = null;
						}
					}
					else {
						info.setSingleton( true );
						info.setScope( BeanDefinition.SCOPE_SINGLETON );

						Object value = beanFactory.getSingleton( name );
						info.setInstance( value );
						try {
							actual = ClassUtils.getUserClass( AopProxyUtils.ultimateTargetClass( value ) );
						}
						catch ( Exception e ) {
							beanType = null;
							actual = null;
						}
					}

					if ( actual != null ) {
						info.setBeanType( actual.getName() );
					}
					if ( beanType != actual ) {
						info.setProxiedOrEnhanced( true );
					}

					detectEventHandlers( info );

					beans.add( info );
				}
			}

			Collections.sort( beans );
		}

		return beans;
	}

	private void detectEventHandlers( BeanInfo beanInfo ) {
		Object value = beanInfo.getInstance();

		if ( value != null && publisher.isListener( value ) ) {
			Collection<Method> handlerMethods = detectHandlerMethods( value );

			if ( !handlerMethods.isEmpty() ) {
				beanInfo.setEventHandler( true );
				beanInfo.setHandlerMethods( handlerMethods );
			}
		}
	}

	private Set<String> getExposedBeanNames( AcrossModule module ) {
		ApplicationContext ctx = AcrossContextUtils.getApplicationContext( module );

		Map<String, Object> exposedSingletons =
				ApplicationContextScanner.findSingletonsMatching( ctx, module.getExposeFilter() );
		Map<String, BeanDefinition> exposedDefinitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( ctx, module.getExposeFilter() );

		Set<String> exposedIds = new HashSet<String>();
		exposedIds.addAll( exposedSingletons.keySet() );
		exposedIds.addAll( exposedDefinitions.keySet() );

		return exposedIds;
	}

	public static class ModuleInfo
	{
		private AcrossModule module;
		private List<BeanInfo> beans = new LinkedList<BeanInfo>();

		public AcrossModule getModule() {
			return module;
		}

		public void setModule( AcrossModule module ) {
			this.module = module;
		}

		public void addBean( BeanInfo bean ) {
			beans.add( bean );
		}

		public List<BeanInfo> getBeans() {
			return beans;
		}

		public boolean isEnabled() {
			return module.isEnabled();
		}
	}

	public Collection<Method> detectHandlerMethods( Object value ) {
		List<Method> methods = new LinkedList<>();

		if ( value != null ) {
			Method[] declaredMethods = ReflectionUtils.getUniqueDeclaredMethods( value.getClass() );

			for ( Method method : declaredMethods ) {
				if ( AnnotationUtils.findAnnotation( method, Handler.class ) != null ) {
					methods.add( method );
				}
			}
		}

		return methods;
	}

	@SuppressWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
	public static class BeanInfo implements Comparable<BeanInfo>
	{
		private Object instance;
		private boolean exposed, inherited, singleton, proxiedOrEnhanced, eventHandler;
		private String name, scope, beanType;
		private Collection<Method> handlerMethods;

		public boolean isEventHandler() {
			return eventHandler;
		}

		public void setEventHandler( boolean eventHandler ) {
			this.eventHandler = eventHandler;
		}

		public Collection<Method> getHandlerMethods() {
			return handlerMethods;
		}

		public void setHandlerMethods( Collection<Method> handlerMethods ) {
			this.handlerMethods = handlerMethods;
		}

		public Object getInstance() {
			return instance;
		}

		public void setInstance( Object instance ) {
			this.instance = instance;
		}

		public void setExposed( boolean exposed ) {
			this.exposed = exposed;
		}

		public void setInherited( boolean inherited ) {
			this.inherited = inherited;
		}

		public void setSingleton( boolean singleton ) {
			this.singleton = singleton;
		}

		public void setName( String name ) {
			this.name = name;
		}

		public void setScope( String scope ) {
			this.scope = scope;
		}

		public boolean isExposed() {
			return exposed;
		}

		public boolean isSingleton() {
			return singleton;
		}

		public boolean isInherited() {
			return inherited;
		}

		public String getName() {
			return name;
		}

		public String getScope() {
			return scope;
		}

		public String getBeanType() {
			return beanType;
		}

		public void setBeanType( String beanType ) {
			this.beanType = beanType;
		}

		public boolean isProxiedOrEnhanced() {
			return proxiedOrEnhanced;
		}

		public void setProxiedOrEnhanced( boolean proxiedOrEnhanced ) {
			this.proxiedOrEnhanced = proxiedOrEnhanced;
		}

		public int compareTo( BeanInfo o ) {
			return getName().compareTo( o.getName() );
		}

		public boolean isStandardSpring() {
			return StringUtils.startsWithIgnoreCase( getName(), "org.springframework." );
		}
	}
}
