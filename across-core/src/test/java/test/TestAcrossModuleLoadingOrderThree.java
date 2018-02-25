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
package test;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapOrderBuilder;
import org.junit.Test;
import org.springframework.core.Ordered;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Tests optional inter-dependency ordering.
 * Based on a dependency bug between hibernate module, acl module and ehcache module.
 *
 * @author Arne Vandamme
 */
public class TestAcrossModuleLoadingOrderThree
{
	private EhcacheModule cache = new EhcacheModule();
	private HibernateModule hibernate = new HibernateModule();
	private AclModule acl = new AclModule();
	private SecurityInfrastructureModule security = new SecurityInfrastructureModule();
	private LoggingModule logging = new LoggingModule();
	private DebugWebModule debug = new DebugWebModule();

	@Test
	public void optionalOrderingOfInfrastructureModules() {
		Collection<AcrossModule> added = list( debug, acl, cache, logging, security, hibernate );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( security, logging, cache, hibernate, acl, debug ), ordered );
	}

	private Collection<AcrossModule> order( Collection<AcrossModule> list ) {
		ModuleBootstrapOrderBuilder bootstrapOrderBuilder = new ModuleBootstrapOrderBuilder();
		bootstrapOrderBuilder.setSourceModules( list );
		return bootstrapOrderBuilder.getOrderedModules();
	}

	private Collection<AcrossModule> list( AcrossModule... modules ) {
		return Arrays.asList( modules );
	}

	@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
	@AcrossDepends(optional = "DebugWebModule")
	public class EhcacheModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "EhcacheModule";
		}
	}

	@AcrossRole(value = AcrossModuleRole.INFRASTRUCTURE, order = Ordered.HIGHEST_PRECEDENCE)
	@AcrossDepends(optional = "EhcacheModule")
	public class SecurityInfrastructureModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "SecurityInfrastructureModule";
		}
	}

	public class DebugWebModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "DebugWebModule";
		}
	}

	@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
	@AcrossDepends(optional = "EhcacheModule")
	public class HibernateModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "HibernateModule";
		}
	}

	@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
	@AcrossDepends(optional = { "AcrossWebModule", "DebugWebModule" })
	public class LoggingModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "LoggingModule";
		}
	}

	@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
	@AcrossDepends(required = "SecurityInfrastructureModule", optional = { "HibernateModule", "EhcacheModule", "LoggingModule" })
	public class AclModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "AclModule";
		}
	}
}
