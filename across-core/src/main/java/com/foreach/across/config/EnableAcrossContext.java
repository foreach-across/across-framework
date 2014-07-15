package com.foreach.across.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Using this annotation on a Configuration class will automatically create an AcrossContext
 * that delegates its configuration to separate AcrossContextConfigurers.
 *
 * @see AcrossContextConfiguration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(AcrossContextConfiguration.class)
public @interface EnableAcrossContext
{
}
