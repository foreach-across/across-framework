package com.foreach.across.modules.ehcache.controllers;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.debugweb.mvc.DebugMenu;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.menu.BuildMenuEvent;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.table.Table;
import com.foreach.across.modules.web.table.TableHeader;
import net.engio.mbassy.listener.Handler;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.LinkedList;

@AcrossEventHandler
@DebugWebController
public class DebugEhcacheController
{
	@Autowired
	private CacheManager cacheManager;

	@Handler
	public void buildMenu( BuildMenuEvent<DebugMenu> event ) {
		event.addMenuItem( "/ehcache", "Cache overview" );
	}

	@InitBinder
	public void init( WebResourceRegistry registry ) {
		registry.addWithKey( WebResource.CSS, "EhcacheModule", "/css/ehcache/ehcache.css", WebResource.VIEWS );
	}

	@RequestMapping(value = "/ehcache", method = RequestMethod.GET)
	public DebugPageView listCaches( DebugPageView view ) {
		view.setPage( "th/ehcache/cacheList" );

		Collection<Ehcache> caches = new LinkedList<Ehcache>();

		for ( String cacheName : cacheManager.getCacheNames() ) {
			caches.add( cacheManager.getCache( cacheName ) );
		}

		view.addObject( "cacheList", caches );

		return view;
	}

	@RequestMapping(value = "/ehcache/flush", method = RequestMethod.GET)
	public DebugPageView flushCache( DebugPageView view,
	                          @RequestParam(value = "cache", required = false) String cacheName,
	                          @RequestParam(value = "from", required = false) String from ) {
		String[] cachesToFlush = cacheName == null ? cacheManager.getCacheNames() : new String[] { cacheName };

		for ( String cache : cachesToFlush ) {
			cacheManager.getCache( cache ).flush();
		}

		view.redirect( "/ehcache?flushed=" + cachesToFlush.length );

		return view;
	}

	@RequestMapping(value = "/ehcache/view", method = RequestMethod.GET)
	public DebugPageView showCache( DebugPageView view, @RequestParam("cache") String cacheName ) {
		view.setPage( "th/ehcache/cacheDetail" );

		Cache cache = cacheManager.getCache( cacheName );

		Table table = new Table();
		table.setHeader( new TableHeader( "Key", "Data", "Age", "Last accessed", "Hits" ) );

		for ( Object key : cache.getKeys() ) {
			Element cacheElement = cache.getQuiet( key );

			long age = System.currentTimeMillis() - cacheElement.getLatestOfCreationAndUpdateTime();
			long accessed = System.currentTimeMillis() - cacheElement.getLastAccessTime();

			table.addRow( key, cacheElement.getObjectValue(), DurationFormatUtils.formatDurationHMS( age ),
			              DurationFormatUtils.formatDurationHMS( accessed ), cacheElement.getHitCount() );
		}

		view.addObject( "cache", cache );
		view.addObject( "cacheEntries", table );

		return view;
	}
}
