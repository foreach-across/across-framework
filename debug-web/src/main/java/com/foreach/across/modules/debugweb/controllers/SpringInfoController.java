package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.debugweb.mvc.DebugMenu;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.menu.BuildMenuEvent;
import com.foreach.across.modules.web.table.Table;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Field;
import java.util.*;

@AcrossEventHandler
@DebugWebController
public class SpringInfoController
{
	@Autowired
	private ApplicationContext applicationContext;

	@Handler
	public void buildMenu( BuildMenuEvent<DebugMenu> event ) {
		event.addMenuItem( "/spring/beans", "Spring beans" );
		event.addMenuItem( "/spring/interceptors", "Spring interceptors" );
		event.addMenuItem( "/spring/handlers", "Spring handlers" );
	}

	@RequestMapping("/spring/beans")
	public DebugPageView showBeans( DebugPageView view ) {
		view.setPage( "listBeans" );

		String[] beanDefinitionNames = getStrings();
		Table table = new Table();

		List<String> sortedNames = Arrays.asList( beanDefinitionNames );
		Collections.sort( sortedNames );

		for ( String beanDefinitionName : sortedNames ) {
			Object bean = applicationContext.getBean( beanDefinitionName );
			table.addRow( beanDefinitionName, bean != null ? bean.getClass().getName() : "" );
		}

		table.setTitle( "Registered beans: " + table.size() );

		view.addObject( "beans", table );

		return view;
	}

	private String[] getStrings() {
		return BeanFactoryUtils.beanNamesIncludingAncestors(
				(ListableBeanFactory) applicationContext.getAutowireCapableBeanFactory() );
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/spring/interceptors")
	public DebugPageView showInterceptors( DebugPageView view ) {
		view.setPage( "listInterceptors" );

		try {
			Map<String, AbstractHandlerMapping> handlers = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					(ListableBeanFactory) applicationContext.getAutowireCapableBeanFactory(),
					AbstractHandlerMapping.class );

			Field field = AbstractHandlerMapping.class.getDeclaredField( "interceptors" );
			field.setAccessible( true );

			List<Table> tables = new LinkedList<Table>();
			for ( Map.Entry<String, AbstractHandlerMapping> handlerEntry : handlers.entrySet() ) {

				Table table = new Table( handlerEntry.getKey() + " - " + handlerEntry.getValue().getClass().getName() );

				List<Object> interceptors = (List<Object>) field.get( handlerEntry.getValue() );
				if ( interceptors != null ) {
					int index = 0;
					for ( Object interceptor : interceptors ) {
						table.addRow( ++index, interceptor.getClass().getName() );
					}
				}

				tables.add( table );
			}

			view.addObject( "handlerTables", tables );
		}
		catch ( Exception e ) {
		}

		return view;
	}

	@RequestMapping("/spring/handlers")
	public DebugPageView showHandlers( DebugPageView view ) {
		view.setPage( "listInterceptors" );

		Map<String, AbstractHandlerMethodMapping> handlers = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				(ListableBeanFactory) applicationContext.getAutowireCapableBeanFactory(),
				AbstractHandlerMethodMapping.class );

		List<Table> tables = new LinkedList<Table>();
		for ( Map.Entry<String, AbstractHandlerMethodMapping> handlerEntry : handlers.entrySet() ) {
			Table table = new Table( handlerEntry.getKey() + " - " + handlerEntry.getValue().getClass().getName() );

			Map<RequestMappingInfo, HandlerMethod> mappings = handlerEntry.getValue().getHandlerMethods();

			for ( Map.Entry<RequestMappingInfo, HandlerMethod> mapping : mappings.entrySet() ) {
				PatternsRequestCondition patterns = mapping.getKey().getPatternsCondition();
				RequestMethodsRequestCondition methodsRequestCondition = mapping.getKey().getMethodsCondition();

				Object patternLabel = patterns;
				Object methodLabel = methodsRequestCondition;

				if ( patterns.getPatterns().size() == 1 ) {
					patternLabel = patterns.getPatterns().iterator().next();
				}

				if ( methodsRequestCondition.getMethods().isEmpty() ) {
					methodLabel = "";
				}
				else if ( methodsRequestCondition.getMethods().size() == 1 ) {
					methodLabel = methodsRequestCondition.getMethods().iterator().next();
				}

				table.addRow( patternLabel, methodLabel,
				              mapping.getValue() );
			}

			tables.add( table );
		}

		view.addObject( "handlerTables", tables );

		return view;
	}
}
