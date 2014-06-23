package com.foreach.across.modules.ehcache.controllers;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.LinkedList;

@DebugWebController
@Refreshable
@AcrossDepends(required = "DebugWebModule")
public class DebugEhcacheController
{
	@Autowired
	private CacheManager cacheManager;

	@Autowired(required = false)
	private DebugWeb debugWeb;

	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.builder().item( "/ehcache", "Cache overview" );
	}

	@ModelAttribute
	public void init( WebResourceRegistry registry ) {
		registry.addWithKey( WebResource.CSS, "EhcacheModule", "/css/ehcache/ehcache.css", WebResource.VIEWS );
	}

	@RequestMapping(value = "/ehcache", method = RequestMethod.GET)
	public String listCaches( Model model ) {
		Collection<Ehcache> caches = new LinkedList<Ehcache>();

		for ( String cacheName : cacheManager.getCacheNames() ) {
			caches.add( cacheManager.getCache( cacheName ) );
		}

		model.addAttribute( "cacheList", caches );

		return "th/ehcache/cacheList";
	}

	@RequestMapping(value = "/ehcache/flush", method = RequestMethod.GET)
	public String flushCache( @RequestParam(value = "cache", required = false) String cacheName,
	                          @RequestParam(value = "from", required = false) String from ) {
		String[] cachesToFlush = cacheName == null ? cacheManager.getCacheNames() : new String[] { cacheName };

		for ( String cache : cachesToFlush ) {
			cacheManager.getCache( cache ).flush();
		}

		return debugWeb.redirect( "/ehcache?flushed=" + cachesToFlush.length );
	}

	@RequestMapping(value = "/ehcache/view", method = RequestMethod.GET)
	public String showCache( @RequestParam("cache") String cacheName, Model model ) {
		Cache cache = cacheManager.getCache( cacheName );

		Table table = new Table();
		table.setHeader( new TableHeader( "Key", "Data", "Age", "Last accessed", "Hits" ) );

		for ( Object key : cache.getKeys() ) {
			Element cacheElement = cache.getQuiet( key );

			if ( cacheElement != null && !cacheElement.isExpired() ) {
				long age = System.currentTimeMillis() - cacheElement.getLatestOfCreationAndUpdateTime();
				long accessed = System.currentTimeMillis() - cacheElement.getLastAccessTime();

				table.addRow( key, cacheElement.getObjectValue(), DurationFormatUtils.formatDurationHMS( age ),
				              DurationFormatUtils.formatDurationHMS( accessed ), cacheElement.getHitCount() );
			}
		}

		model.addAttribute( "cache", cache );
		model.addAttribute( "cacheEntries", table );

		return "th/ehcache/cacheDetail";
	}
}
