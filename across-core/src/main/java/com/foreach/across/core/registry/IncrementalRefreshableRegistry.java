package com.foreach.across.core.registry;

import com.foreach.across.core.annotations.EventName;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.core.events.EventNameFilter;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * <p>Extends the {@link com.foreach.across.core.registry.RefreshableRegistry} by scanning for members
 * every time a module has bootstrapped.  Modules later in the bootstrapping order will immediately see
 * all members added by previous modules.</p>
 * <p>Note that this implementation slows down AcrossContext bootstrap duration, so use only if necessary.</p>
 */
@Refreshable
public class IncrementalRefreshableRegistry<T> extends RefreshableRegistry<T> {
    @Autowired
    private AcrossEventPublisher eventBus;

    public IncrementalRefreshableRegistry(Class<T> memberType) {
        super(memberType);
    }

    public IncrementalRefreshableRegistry(Class<T> type, boolean scanModules) {
        super(type, scanModules);
    }

    @PostConstruct
    private void hookupEventHandler() {
        eventBus.subscribe(this);
    }

    @Handler(filters = @Filter(EventNameFilter.class))
    private void moduleBootstrapped(@EventName("test") AcrossModuleBootstrappedEvent moduleBootstrapped) {
        refresh();
    }

    @PostRefresh
    @Override
    public void refresh() {
        super.refresh();
    }
}
