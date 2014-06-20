package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@AcrossEventHandler
public @interface DebugWebController
{
}
