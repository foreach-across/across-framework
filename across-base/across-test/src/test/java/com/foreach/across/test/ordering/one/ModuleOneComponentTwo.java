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
package com.foreach.across.test.ordering.one;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.test.ordering.MyComponent;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Component
@Exposed
@OrderInModule(2)
public class ModuleOneComponentTwo implements MyComponent
{
}
