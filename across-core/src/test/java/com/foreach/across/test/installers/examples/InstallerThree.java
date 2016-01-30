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
package com.foreach.across.test.installers.examples;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.test.modules.installer.installers.TestInstaller;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author Arne Vandamme
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Installer(description = "different package installer", name = "installerThree")
public class InstallerThree extends TestInstaller
{
}
