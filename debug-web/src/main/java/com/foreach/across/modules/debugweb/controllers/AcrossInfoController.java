package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.ApplicationContextScanner;
import com.foreach.across.core.context.bootstrap.BootstrapAcrossModuleOrder;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import net.engio.mbassy.listener.Handler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@DebugWebController
public class AcrossInfoController
{
	@Autowired
	private AcrossContext acrossContext;

	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.addItem( "/across/modules", "Across modules" );
	}

	@ModelAttribute
	public void registerJQuery( WebResourceRegistry resourceRegistry ) {
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "jquery",
		                             "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js",
		                             WebResource.EXTERNAL );
	}

	@RequestMapping("/across/modules")
	public DebugPageView showBeans( DebugPageView view ) {
		view.setPage( DebugWeb.VIEW_MODULES );

		BootstrapAcrossModuleOrder modules = new BootstrapAcrossModuleOrder( acrossContext.getModules(), false );

		view.addObject( "moduleRegistry", modules );
		view.addObject( "modules", modules.getOrderedModules() );

		Collection<ModuleInfo> moduleInfoList = new LinkedList<ModuleInfo>();

		for ( AcrossModule module : modules.getOrderedModules() ) {
			ModuleInfo moduleInfo = new ModuleInfo();
			moduleInfo.setModule( module );

			if ( module.isEnabled() ) {
				Set<String> exposed = getExposedBeanNames( module );

				ConfigurableListableBeanFactory beanFactory = AcrossContextUtils.getBeanFactory( module );

				String[] definitions = beanFactory.getBeanDefinitionNames();
				Set<String> names = new HashSet<String>();
				names.addAll( Arrays.asList( definitions ) );
				names.addAll( Arrays.asList( beanFactory.getSingletonNames() ) );

				for ( String name : names ) {
					BeanInfo info = new BeanInfo();
					info.setName( name );
					info.setExposed( exposed.contains( name ) );

					Class beanType = Object.class;
					Class actual = Object.class;

					if ( ArrayUtils.contains( definitions, name ) ) {
						BeanDefinition definition = beanFactory.getBeanDefinition( name );
						info.setSingleton( definition.isSingleton() );
						info.setScope( definition.getScope() );

						try {
							if ( beanFactory.isSingleton( name ) ) {
								Object value = beanFactory.getSingleton( name );
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

					moduleInfo.addBean( info );
				}

				Collections.sort( moduleInfo.getBeans() );

			}
			moduleInfoList.add( moduleInfo );
		}

		view.addObject( "moduleInfoList", moduleInfoList );

		return view;
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

	public static class BeanInfo implements Comparable<BeanInfo>
	{
		private boolean exposed, inherited, singleton, proxiedOrEnhanced;
		private String name, scope, beanType;

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
