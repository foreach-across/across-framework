package com.foreach.across.core.annotations;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EventName
{
	String[] value();
}
