package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Exposed
@Component
public @interface DebugWebController
{
	String path() default "";
}
