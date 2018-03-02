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
package com.foreach.across.core.context.support;

import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ModuleDependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Helper that builds a {@link ModuleSet}.
 *
 * @author Arne Vandamme
 */
public class ModuleSetBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger( ModuleSetBuilder.class );

	private List<Supplier<AcrossModule>> moduleSuppliers = new ArrayList<>();
	private ModuleDependencyResolver dependencyResolver;

	public void setDependencyResolver( ModuleDependencyResolver dependencyResolver ) {
		this.dependencyResolver = dependencyResolver;
	}

	/**
	 * Add a module by name, the internal {@link ModuleDependencyResolver} will be used to resolve the module.
	 *
	 * @param moduleName name of the module to resolve
	 */
	public void addModule( String moduleName ) {
		moduleSuppliers.add( () -> {
			if ( dependencyResolver == null ) {
				throw new AcrossConfigurationException(
						"Unable to resolve modules by name as no dependencyResolver is configured.",
						"Add all modules directly to the Across context or configure module scanning on your @EnableAcrossContext or @AcrossApplication."
				);
			}
			Optional<AcrossModule> module = dependencyResolver.resolveModule( moduleName, true );
			if ( !module.isPresent() ) {
				throw new AcrossConfigurationException(
						"Unable to resolve module " + moduleName + ".",
						"Either declare the module as a @Bean in the parent context or configure module scanning and check your module has a 'public static final String NAME' constant."
				);
			}
			return module.get();
		} );
	}

	public void addModule( AcrossModule module ) {
		moduleSuppliers.add( () -> module );
	}

	/**
	 * Tries to build a complete {@link ModuleSet} by resolving all modules, including all dependencies.
	 *
	 * @return set with all dependencies resolved
	 */
	public ModuleSet build() {
		ModuleSet moduleSet = new ModuleSet();

		List<AcrossModule> sourceModules = new ArrayList<>();
		moduleSuppliers.forEach( s -> sourceModules.add( s.get() ) );

		registerAndResolveDependencies( moduleSet, sourceModules );

		return moduleSet;
	}

	private void registerAndResolveDependencies( ModuleSet moduleSet, List<AcrossModule> sourceModules ) {
		sourceModules.forEach( m -> registerModule( moduleSet, m ) );

		Predicate<AcrossModule> dependenciesLoaded = moduleSet.definedRequiredDependencies::containsKey;

		while ( moduleSet.definedRequiredDependencies.size() != moduleSet.modules.size() ) {
			new ArrayList<>( moduleSet.modules.values() )
					.stream()
					.filter( dependenciesLoaded.negate() )
					.forEach( m -> resolveDependencies( moduleSet, m ) );
		}
	}

	private void resolveDependencies( ModuleSet moduleSet, AcrossModule module ) {
		Annotation depends = AnnotationUtils.getAnnotation( module.getClass(), AcrossDepends.class );

		Set<String> definedRequired = new LinkedHashSet<>();
		Set<String> definedOptional = new LinkedHashSet<>();

		if ( depends != null ) {
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes( depends );
			String[] required = (String[]) attributes.get( "required" );
			String[] optional = (String[]) attributes.get( "optional" );

			definedRequired.addAll( Arrays.asList( required ) );
			definedOptional.addAll( Arrays.asList( optional ) );
		}

		definedRequired.addAll( module.getRuntimeDependencies() );

		moduleSet.definedRequiredDependencies.put( module, definedRequired );
		moduleSet.definedOptionalDependencies.put( module, definedOptional );

		Predicate<String> moduleRegistered = moduleSet.modules::containsKey;

		if ( dependencyResolver != null ) {
			definedRequired.stream()
			               .filter( moduleRegistered.negate() )
			               .forEach( m -> resolveDependency( moduleSet, m, true ) );

			definedOptional.stream()
			               .filter( moduleRegistered.negate() )
			               .forEach( m -> resolveDependency( moduleSet, m, false ) );
		}
	}

	private void resolveDependency( ModuleSet moduleSet, String moduleName, boolean required ) {
		LOG.debug( "Resolving {} dependency: {}", required ? "required" : "optional", moduleName );

		Optional<AcrossModule> module = dependencyResolver.resolveModule( moduleName, required );

		if ( module.isPresent() ) {
			registerModule( moduleSet, module.get() );
		}
	}

	private void registerModule( ModuleSet moduleSet, AcrossModule module ) {
		moduleSet.modules.put( module.getName(), module );
		moduleSet.moduleRoles.put( module, determineRole( module ) );
	}

	private AcrossModuleRole determineRole( AcrossModule module ) {
		Annotation role = AnnotationUtils.getAnnotation( module.getClass(), AcrossRole.class );

		if ( role != null ) {
			return (AcrossModuleRole) AnnotationUtils.getAnnotationAttributes( role ).get( "value" );
		}

		return AcrossModuleRole.APPLICATION;
	}
}
