package com.foreach.across.core.annotations;

import com.foreach.across.core.events.EventNameFilter;
import com.foreach.across.core.events.ParameterizedAcrossEventFilter;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;

import java.lang.annotation.*;

/**
 * Default handler annotation for events fired on the AcrossEventPublisher.
 *
 * @see com.foreach.across.core.events.AcrossEvent
 * @see com.foreach.across.core.annotations.AcrossEventHandler
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Handler(filters = { @Filter(ParameterizedAcrossEventFilter.class), @Filter(EventNameFilter.class) })
public @interface Event
{
}
