/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
