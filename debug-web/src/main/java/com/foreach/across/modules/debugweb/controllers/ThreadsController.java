package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import net.engio.mbassy.listener.Handler;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

@AcrossEventHandler
@DebugWebController
public class ThreadsController
{
	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.addMenuItem( "/threadStack", "Threads and stack" );
	}

	@RequestMapping("/threadStack")
	public DebugPageView showThreads( DebugPageView view ) {
		view.setPage( DebugWeb.VIEW_THREADS );

		ArrayList<ThreadGroup> threadGroups = loadThreadData();

		StringBuilder output = new StringBuilder();
		output.append( "<div id='threads'>" );
		output.append( printThreads( threadGroups ) );
		output.append( "<hr />" );

		Thread[] allThreads = getAllThreads( threadGroups );

		if ( ( allThreads == null ) || ( allThreads.length < 1 ) ) {
			output.append( "SystemThreadList: main(): allThreads is null or length < 1." );

		}
		else {

			// Print thread info
			for ( int i = 0; i < allThreads.length; i++ ) {
				Thread t = allThreads[i];

				output.append( "Thread " ).append( i ).append( " = " ).append( t.toString() ).append( "<br />\n" );
				output.append( "&nbsp;&nbsp;&nbsp;&nbsp;state: " ).append( t.getState().toString() ).append(
						" - prio: " ).append( t.getPriority() ).append(
						" - alive: " + t.isAlive() + " - daemon: " + t.isDaemon() + " - interrupted: " ).append(
						t.isInterrupted() ).append( "<br />" );
				StackTraceElement[] stack = t.getStackTrace();
				for ( int j = 0; j < stack.length; j++ ) {
					output.append( "frame: " ).append( j ).append( ": " ).append( stack[j].toString() ).append(
							"<br />" );
				}
				output.append( "<hr/>" );
			}
		}

		view.addObject( "threadOutput", output );

		return view;
	}

	private ArrayList<ThreadGroup> loadThreadData() {
		ArrayList<ThreadGroup> _threads = new ArrayList<ThreadGroup>();

		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		if ( tg != null ) {
			_threads.add( tg );
			while ( tg != null && tg.getParent() != null ) {
				tg = tg.getParent();
				if ( tg != null ) {
					_threads.add( tg );
				}
			}
		}
		return _threads;
	}

	private String printThreads( ArrayList<ThreadGroup> threads ) {
		StringBuilder sb = new StringBuilder( "[SystemThreadList:\n<br />" );

		int threadCount = getThreadCount( threads );
		if ( threadCount < 1 ) {
			sb.append( " No Threads " );
		}
		else {
			for ( int i = 0; i < threadCount; i++ ) {
				sb.append( " ThreadGroup " ).append( i ).append( "= " );
				sb.append( getThreadGroup( i, threads ).toString() );
				sb.append( ", activeCount = " ).append( getThreadGroup( i, threads ).activeCount() );
				sb.append( "\n<br />" );
			}
		}

		// Total active count
		sb.append( " totalActiveCount = " ).append( getTotalActiveCount( threads ) ).append( "\n<br />" );

		sb.append( " (End of SystemThreadList)]\n<br />" );
		return sb.toString();
	}

	private Thread[] getAllThreads( ArrayList<ThreadGroup> _threads ) {
		int estimatedCount = getTotalActiveCount( _threads );

		// Start with array twice size of estimated,
		// to be safe. Trim later.
		Thread[] estimatedThreads = new Thread[estimatedCount * 2];

		// Locate root group
		ThreadGroup rootGroup = getRootThreadGroup( _threads );
		if ( rootGroup == null ) {
			return null;
		}

		int actualCount = rootGroup.enumerate( estimatedThreads, true );

		// Check that something was returned
		if ( actualCount < 1 ) {
			return null;
		}

		// Copy into actualThreads of correct size
		Thread[] actualThreads = new Thread[actualCount];
		System.arraycopy( estimatedThreads, 0, actualThreads, 0, actualCount );

		return actualThreads;
	}

	private ThreadGroup getRootThreadGroup( ArrayList<ThreadGroup> _threads ) {
		if ( getThreadCount( _threads ) < 1 ) {
			return null;
		}
		else {
			ThreadGroup tg;
			for ( int i = 0; i < getThreadCount( _threads ); i++ ) {
				tg = getThreadGroup( i, _threads );
				if ( tg.getParent() == null ) {
					return tg;
				}
			}

			// If we got here, we didn't find one, so return null.
			return null;
		}
	}

	private ThreadGroup getThreadGroup( int index, ArrayList<ThreadGroup> _threads ) {
		if ( getThreadCount( _threads ) < 1 ) {
			return null;
		}
		else if ( ( index < 0 ) || ( index > ( getThreadCount( _threads ) - 1 ) ) ) {
			return null;
		}
		else {
			return _threads.get( index );
		}
	}

	private int getThreadCount( ArrayList<ThreadGroup> _threads ) {
		if ( _threads == null ) {
			return -1;
		}
		else {
			return _threads.size();
		}
	}

	private int getTotalActiveCount( ArrayList<ThreadGroup> _threads ) {
		if ( getThreadCount( _threads ) < 1 ) {
			return 0;
		}
		else {
			int totalActiveCount = 0;
			ThreadGroup tg;
			for ( int i = 0; i < getThreadCount( _threads ); i++ ) {
				tg = getThreadGroup( i, _threads );
				totalActiveCount += tg.activeCount();
			}

			return totalActiveCount;
		}
	}
}
