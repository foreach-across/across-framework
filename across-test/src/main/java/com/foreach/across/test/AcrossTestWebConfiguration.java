package com.foreach.across.test;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(AcrossTestWebContextConfiguration.class)
public @interface AcrossTestWebConfiguration
{
}
