package com.foreach.across.core.annotations;

import org.springframework.context.event.EventListener;

import java.lang.annotation.*;

/**
 * Default handler annotation for events fired on the AcrossEventPublisher.
 * As of 3.0.0 this annotation is no longer required. It will be removed in a future release.
 * Use the Spring {@link EventListener} annotation instead.
 *
 * @deprecated use the Spring standard {@link EventListener} instead
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EventListener
@Deprecated
public @interface Event
{
}
