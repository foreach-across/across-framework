package com.foreach.across.core.events;

import com.foreach.across.core.annotations.EventName;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandlerMetadata;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/**
 * Filters events based on the @EventName annotation and the eventName property of the message.
 * If the message is not a NamedAcrossEvent instance, it will not be handled.
 */
public class EventNameFilter implements IMessageFilter {

    private static final Logger LOG = LoggerFactory.getLogger(EventNameFilter.class);

    public boolean accepts(Object message, MessageHandlerMetadata metadata) {
        if (message instanceof NamedAcrossEvent) {
            String eventName = ((NamedAcrossEvent) message).getEventName();

            Annotation[][] annotationMap = metadata.getHandler().getParameterAnnotations();

            if (annotationMap.length > 0) {
                for (Annotation annotation : annotationMap[0]) {
                    if (annotation instanceof EventName) {
                        String[] acceptable = ((EventName) annotation).value();

                        return ArrayUtils.contains(acceptable, eventName);
                    }
                }
            }

        } else {
            LOG.trace("Message type {} does not implement NamedAcrossEvent", message != null ? message.getClass() : null);
        }

        return false;
    }
}
