package com.foreach.across.modules.debugweb.controllers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.table.Table;
import net.engio.mbassy.listener.Handler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@DebugWebController
public class LogController
{
	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.addItem( "/loggers", "Logger overview" );
	}

	@RequestMapping(value = "/loggers", method = RequestMethod.GET)
	public DebugPageView showLoggers( DebugPageView view ) {
		view.setPage( DebugWeb.VIEW_LOGGERS );

		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<Logger> loggers = loggerContext.getLoggerList();
		List<Level> levels = Arrays.asList( Level.OFF, Level.TRACE, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG );

		Map<String, Level> loggerMap = new TreeMap<String, Level>();
		Map<String, String> appenderMap = new TreeMap<String, String>();

		for ( Logger logger : loggers ) {
			loggerMap.put( logger.getName(), logger.getEffectiveLevel() );

			Iterator<Appender<ILoggingEvent>> appenders = logger.iteratorForAppenders();
			while ( appenders.hasNext() ) {
				Appender appender = appenders.next();
				if ( appender instanceof FileAppender ) {
					FileAppender fileAppender = (FileAppender) appender;
					String filename = fileAppender.getFile();
					appenderMap.put( appender.getName(), filename );
				}
				else {
					appenderMap.put( appender.getName(), appender.toString() );
				}

			}
		}

		view.addObject( "levels", levels );
		view.addObject( "loggers", loggerMap );
		view.addObject( "appenders", Table.fromMap( "Appenders", appenderMap ) );

		return view;
	}

	@RequestMapping(value = "/loggers", method = RequestMethod.POST)
	public DebugPageView updateLoggers( DebugPageView view, HttpServletRequest request ) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<Logger> loggers = loggerContext.getLoggerList();

		int updated = 0;

		for ( Logger logger : loggers ) {
			String requestedLevel = request.getParameter( logger.getName() );

			if ( !StringUtils.isBlank( requestedLevel ) ) {
				Level newLevel = Level.valueOf( requestedLevel );

				if ( newLevel != logger.getEffectiveLevel() ) {
					logger.setLevel( newLevel );

					updated++;
				}
			}
		}

		if ( updated > 0 ) {
			view.addObject( "feedback", updated + " loggers have been updated." );
		}
		else {
			view.addObject( "feedback", "No loggers have been updated." );
		}

		return showLoggers( view );
	}
}