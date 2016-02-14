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

package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.support.ModuleSet;
import com.foreach.across.core.context.support.ModuleSetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Creates an ordered list of all modules, depending on the AcrossDepends annotations configured.
 * This class creates the optimal bootstrap order for all modules and can throw an exception if two
 * modules have a cyclic dependency and are impossible to bootstrap (very bad - impossible to fix).
 */
@SuppressWarnings("all")
public class ModuleBootstrapOrderBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger( ModuleBootstrapOrderBuilder.class );

	private List<AcrossModule> sourceModules = Collections.emptyList();
	private ModuleDependencyResolver dependencyResolver;

	private LinkedList<AcrossModule> orderedModules;

	private Map<String, AcrossModule> modulesById;
	private Map<AcrossModule, Collection<AcrossModule>> appliedRequiredDependencies = new HashMap<>();
	private Map<AcrossModule, Collection<AcrossModule>> appliedOptionalDependencies = new HashMap<>();
	private Map<AcrossModule, Collection<AcrossModule>> configuredRequiredDependencies = new HashMap<>();
	private Map<AcrossModule, Collection<AcrossModule>> configuredOptionalDependencies = new HashMap<>();

	private ModuleSet moduleSet;

	/**
	 * @param sourceModules list of modules preloaded
	 */
	public void setSourceModules( Collection<AcrossModule> sourceModules ) {
		this.sourceModules = new ArrayList<>( sourceModules );
		orderModules();
	}

	/**
	 * @param dependencyResolver to use for resolving unsatisfied module dependencies
	 */
	public void setDependencyResolver( ModuleDependencyResolver dependencyResolver ) {
		this.dependencyResolver = dependencyResolver;
		if ( !sourceModules.isEmpty() ) {
			orderModules();
		}
	}

	public Collection<AcrossModule> getOrderedModules() {
		return orderedModules;
	}

	public Collection<AcrossModule> getConfiguredRequiredDependencies( AcrossModule module ) {
		return configuredRequiredDependencies.get( module );
	}

	public Collection<AcrossModule> getConfiguredOptionalDependencies( AcrossModule module ) {
		return configuredOptionalDependencies.get( module );
	}

	public Collection<AcrossModule> getAppliedRequiredDependencies( AcrossModule module ) {
		return appliedRequiredDependencies.get( module );
	}

	public Collection<AcrossModule> getAppliedOptionalDependencies( AcrossModule module ) {
		return appliedOptionalDependencies.get( module );
	}

	public AcrossModuleRole getModuleRole( AcrossModule module ) {
		return moduleSet.getModuleRole( module );
	}

	private void orderModules() {
		ModuleSetBuilder moduleSetBuilder = new ModuleSetBuilder();
		moduleSetBuilder.setDependencyResolver( dependencyResolver );
		sourceModules.forEach( moduleSetBuilder::addModule );

		moduleSet = moduleSetBuilder.build();
		modulesById = moduleSet.getModuleMap();

		orderedModules = new LinkedList<>();

		List<AcrossModule> orderedByRole = applyRoleOrder( moduleSet.getModules() );

		for ( AcrossModule module : orderedByRole ) {
			verifyDependencies( module );
		}

		applyEnabledInfrastructureModules();
		applyEnabledPostProcessorModules();

		Map<AcrossModule, Boolean> requiredStack = new HashMap<>();

		// Place in required order
		for ( AcrossModule module : orderedByRole ) {
			place( requiredStack, orderedModules, module );
		}

		// Shuffle optionals if possible
		boolean shuffled;

		do {
			shuffled = false;

			for ( AcrossModule module : orderedByRole ) {
				AcrossModuleRole role = getModuleRole( module );
				Collection<AcrossModule> optionalModules = getAppliedOptionalDependencies( module );

				for ( AcrossModule optional : optionalModules ) {
					if ( hasModuleRole( optional, role )
							&& !getAppliedOptionalDependencies( optional ).contains( module )
							&& moveToIndexIfPossible( orderedModules, optional, orderedModules.indexOf( module ) ) ) {
						shuffled = true;
					}
				}
			}
		}
		while ( shuffled );

		verifyModuleList( orderedModules );
	}

	private List<AcrossModule> applyRoleOrder( final List<AcrossModule> source ) {
		List<AcrossModule> ordered = new ArrayList<>( source );

		Collections.sort( ordered, new Comparator<AcrossModule>()
		{
			@Override
			public int compare( AcrossModule left, AcrossModule right ) {
				Integer comparison = Integer.compare( getRoleOrder( left ), getRoleOrder( right ) );

				if ( comparison == 0 ) {
					comparison = Integer.compare( getOrderInRole( left ), getOrderInRole( right ) );
				}

				if ( comparison == 0 ) {
					comparison = Integer.compare( source.indexOf( left ), source.indexOf( right ) );
				}

				return comparison;
			}
		} );

		return ordered;
	}

	private boolean hasModuleRole( AcrossModule module, AcrossModuleRole role ) {
		return getModuleRole( module ).equals( role );
	}

	private boolean moveToIndexIfPossible( LinkedList<AcrossModule> orderedModules,
	                                       AcrossModule moduleToMove,
	                                       int index ) {

		// Only move if the module is after the index
		if ( orderedModules.indexOf( moduleToMove ) > index ) {
			// Only move if all required dependencies are already before that index
			boolean requirementsMet = true;

			for ( AcrossModule requirement : getAppliedRequiredDependencies( moduleToMove ) ) {
				if ( orderedModules.indexOf( requirement ) >= index ) {
					requirementsMet = false;
				}
			}

			if ( requirementsMet ) {
				orderedModules.add( index, moduleToMove );
				orderedModules.removeLastOccurrence( moduleToMove );

				return true;
			}
		}

		return false;
	}

	private void place( Map<AcrossModule, Boolean> requiredStack,
	                    LinkedList<AcrossModule> orderedModules,
	                    AcrossModule module ) {
		requiredStack.put( module, true );

		for ( AcrossModule requirement : getAppliedRequiredDependencies( module ) ) {
			if ( !orderedModules.contains( requirement ) ) {
				if ( requiredStack.containsKey( requirement ) ) {
					throw new CyclicModuleDependencyException( requirement.getName() );
				}
				else {
					place( requiredStack, orderedModules, requirement );
				}
			}
		}

		if ( !orderedModules.contains( module ) ) {
			orderedModules.addLast( module );
		}

		requiredStack.remove( module );
	}

	private void applyEnabledInfrastructureModules() {
		modulesById.values().forEach(
				module -> {
					AcrossModuleRole role = moduleSet.getModuleRole( module );
					if ( role == AcrossModuleRole.INFRASTRUCTURE && module.isEnabled() ) {
						// Infrastructure modules are added as required dependencies to all non-infrastructure modules
						modulesById.values().forEach(
								targetModule -> {
									if ( moduleSet.getModuleRole( targetModule ) != AcrossModuleRole.INFRASTRUCTURE
											&& !appliedRequiredDependencies.get( module ).contains( targetModule ) ) {
										appliedRequiredDependencies.get( targetModule ).add( module );
									}
								}
						);
					}
				}
		);
	}

	private void applyEnabledPostProcessorModules() {
		modulesById.values().forEach(
				module -> {
					AcrossModuleRole role = moduleSet.getModuleRole( module );
					if ( role == AcrossModuleRole.POSTPROCESSOR && module.isEnabled() ) {
						// Post processor modules have all non post-processor enabled modules as required dependencies
						modulesById.values().forEach(
								target -> {
									if ( moduleSet.getModuleRole( target ) != AcrossModuleRole.POSTPROCESSOR ) {
										if ( appliedRequiredDependencies.get( target ).contains( module ) ) {
											LOG.debug(
													"Ignoring {} as required dependency for {} since the former is a postprocessor module",
													module.getName(), target.getName() );
											appliedRequiredDependencies.get( target ).remove( module );
										}
										if ( appliedOptionalDependencies.get( target ).contains( module ) ) {
											LOG.debug(
													"Ignoring {} as optional dependency for {} since the former is a postprocessor module",
													module.getName(), target.getName() );
											appliedOptionalDependencies.get( target ).remove( module );
										}

										// Add the target as a required dependency for the post-processor
										appliedRequiredDependencies.get( module ).add( target );
									}
								}
						);
					}
				}
		);
	}

	private void verifyModuleList( LinkedList<AcrossModule> ordered ) {
		Set<AcrossModule> handled = new HashSet<AcrossModule>();

		Iterator<AcrossModule> iterator = ordered.iterator();

		while ( iterator.hasNext() ) {
			AcrossModule module = iterator.next();

			// Remove module if already handled
			if ( handled.contains( module ) ) {
				iterator.remove();
			}
			else {
				handled.add( module );

				LOG.trace( "Verifying module: {}", module.getName() );

				// Required dependencies should have already been handled
				for ( AcrossModule dependency : appliedRequiredDependencies.get( module ) ) {
					if ( !handled.contains( dependency ) ) {
						throw new CyclicModuleDependencyException( dependency.getName() );
					}
				}
			}
		}
	}

	private void verifyDependencies( AcrossModule module ) {
		Set<AcrossModule> requiredByModule = new TreeSet<>( new Comparator<AcrossModule>()
		{
			public int compare( AcrossModule left, AcrossModule right ) {
				return Integer.valueOf( sourceModules.indexOf( left ) ).compareTo( sourceModules.indexOf( right ) );
			}
		} );
		Set<AcrossModule> optionalForModule = new TreeSet<>( new Comparator<AcrossModule>()
		{
			public int compare( AcrossModule left, AcrossModule right ) {
				return Integer.valueOf( sourceModules.indexOf( left ) ).compareTo( sourceModules.indexOf( right ) );
			}
		} );

		Collection<String> definedRequired = moduleSet.getRequiredDependencies( module );
		Collection<String> definedOptional = moduleSet.getOptionalDependencies( module );

		for ( String requiredModule : definedRequired ) {
			if ( !modulesById.containsKey( requiredModule ) ) {
				throw new ModuleDependencyMissingException( module.getName(), requiredModule );
			}
			else if ( !modulesById.get( requiredModule ).isEnabled() ) {
				throw new ModuleDependencyDisabledException( module.getName(), requiredModule );
			}

			requiredByModule.add( modulesById.get( requiredModule ) );

			LOG.trace( "Module {} requires module {}", module.getName(), requiredModule );
		}

		for ( String optionalModule : definedOptional ) {
			if ( modulesById.containsKey( optionalModule ) ) {
				optionalForModule.add( modulesById.get( optionalModule ) );

				LOG.trace( "Module {} optionally depends on module {}", module.getName(), optionalModule );
			}
		}

		appliedRequiredDependencies.put( module, requiredByModule );
		appliedOptionalDependencies.put( module, optionalForModule );

		configuredRequiredDependencies.put( module, new ArrayList<>( requiredByModule ) );
		configuredOptionalDependencies.put( module, new ArrayList<>( optionalForModule ) );
	}

	private int getRoleOrder( AcrossModule module ) {
		switch ( getModuleRole( module ) ) {
			case INFRASTRUCTURE:
				return -1;
			case POSTPROCESSOR:
				return 1;
			default:
				return 0;
		}
	}

	private int getOrderInRole( AcrossModule module ) {
		if ( module instanceof Ordered ) {
			return ( (Ordered) module ).getOrder();
		}

		Annotation role = AnnotationUtils.getAnnotation( module.getClass(), AcrossRole.class );

		if ( role != null ) {
			return (Integer) AnnotationUtils.getAnnotationAttributes( role ).get( "order" );
		}

		return 0;
	}
}
