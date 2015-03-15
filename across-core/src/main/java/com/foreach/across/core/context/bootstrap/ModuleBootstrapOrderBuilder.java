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
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private final List<AcrossModule> source;

	private LinkedList<AcrossModule> orderedModules;

	private Map<String, AcrossModule> modulesById = new HashMap<>();
	private Map<AcrossModule, Collection<AcrossModule>> appliedRequiredDependencies = new HashMap<>();
	private Map<AcrossModule, Collection<AcrossModule>> appliedOptionalDependencies = new HashMap<>();
	private Map<AcrossModule, Collection<AcrossModule>> configuredRequiredDependencies = new HashMap<>();
	private Map<AcrossModule, Collection<AcrossModule>> configuredOptionalDependencies = new HashMap<>();
	private Map<AcrossModule, AcrossModuleRole> moduleRoles = new HashMap<>();

	public ModuleBootstrapOrderBuilder( Collection<AcrossModule> source ) {
		this.source = new ArrayList<>( source );

		orderModules();
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
		return moduleRoles.get( module );
	}

	private void orderModules() {
		buildModuleMetaData();

		orderedModules = new LinkedList<>();

		List<AcrossModule> orderedByRole = applyRoleOrder( this.source );

		for ( AcrossModule module : orderedByRole ) {
			buildDependencies( module );
		}

		applyEnabledInfrastructureModules();
		applyEnabledPostProcessorModules();

		Map<AcrossModule, Boolean> requiredStack = new HashMap<>();

		// Place in required order
		for ( AcrossModule module : orderedByRole ) {
			place( requiredStack, orderedModules, module );
		}

		optimizeOptionals( orderedByRole, orderedModules );

		verifyModuleList( orderedModules );
	}

	private void optimizeOptionals( List<AcrossModule> orderedByRole, LinkedList<AcrossModule> orderedModules ) {
		List<AcrossModule> optionalsToMove = new ArrayList<>();

		for ( AcrossModule module : orderedByRole ) {
			optionalsToMove.addAll( getAppliedOptionalDependencies( module ) );
		}

		Set<AcrossModule> optionalsMoved = new HashSet<>();

		for ( AcrossModule optional : optionalsToMove ) {
			moveModuleAsHighAsPossible( optional, optionalsMoved, orderedModules );
		}
	}

	private void moveModuleAsHighAsPossible( AcrossModule moduleToMove,
	                                         Set<AcrossModule> optionalsMoved,
	                                         LinkedList<AcrossModule> orderedModules ) {
		if ( !optionalsMoved.contains( moduleToMove ) ) {
			optionalsMoved.add( moduleToMove );

			// First move the other dependencies it has
			for ( AcrossModule dependency : getAppliedRequiredDependencies( moduleToMove ) ) {
				moveModuleAsHighAsPossible( dependency, optionalsMoved, orderedModules );
			}

			for ( AcrossModule dependency : getAppliedOptionalDependencies( moduleToMove ) ) {
				moveModuleAsHighAsPossible( dependency, optionalsMoved, orderedModules );
			}

			// Find lowest required dependency
			int earliestPossiblePosition = findEarliestPossiblePosition( moduleToMove, orderedModules );

			// Attempt to move the module
			if ( earliestPossiblePosition < orderedModules.indexOf( moduleToMove ) ) {
				orderedModules.add( earliestPossiblePosition, moduleToMove );
				orderedModules.removeLastOccurrence( moduleToMove );

				for ( AcrossModule other : new ArrayList<>( orderedModules ) ) {
					if ( getAppliedOptionalDependencies( other ).contains( moduleToMove ) ) {
						moveModuleAsHighAsPossible( moduleToMove, optionalsMoved, orderedModules );
					}
				}
			}
		}
	}

	private int findEarliestPossiblePosition( AcrossModule moduleToMove, LinkedList<AcrossModule> orderedModules ) {
		int index = -1;

		// Find the required dependencies
		for ( AcrossModule dependency : getAppliedRequiredDependencies( moduleToMove ) ) {
			int i = orderedModules.indexOf( dependency );

			index = Math.max( i, index );
		}

		// Find earliest position for modules in the same role without changing the fixed order
		int currentIndex = orderedModules.indexOf( moduleToMove );

		for ( int i = 0; i < orderedModules.size() && i < currentIndex; i++ ) {
			AcrossModule module = orderedModules.get( i );

			if ( getModuleRole( moduleToMove ) == getModuleRole( module ) ) {
				if ( getRoleOrder( module ) < getRoleOrder( moduleToMove )
						|| getOrderInRole( module ) < getOrderInRole( moduleToMove )) {
					index = Math.max( i, index );
				}
			}
			else {
				index = Math.max( i, index );
			}
		}

		return index + 1;
	}

	private void buildModuleMetaData() {
		for ( AcrossModule module : source ) {
			modulesById.put( module.getName(), module );
			modulesById.put( module.getClass().getName(), module );
			determineRole( module );
		}
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
		for ( Map.Entry<AcrossModule, AcrossModuleRole> moduleRole : moduleRoles.entrySet() ) {
			if ( moduleRole.getValue() == AcrossModuleRole.INFRASTRUCTURE ) {
				AcrossModule infrastructure = moduleRole.getKey();

				if ( infrastructure.isEnabled() ) {
					// Infrastructure modules are added as required dependencies to all non-infrastructure modules
					// Indirect dependencies
					for ( Map.Entry<AcrossModule, AcrossModuleRole> targetModuleRole : moduleRoles.entrySet() ) {
						AcrossModule target = targetModuleRole.getKey();

						if ( targetModuleRole.getValue() != AcrossModuleRole.INFRASTRUCTURE
								&& !appliedRequiredDependencies.get( infrastructure ).contains( target ) ) {
							appliedRequiredDependencies.get( targetModuleRole.getKey() ).add( moduleRole.getKey() );
						}
					}
				}
			}
		}
	}

	private void applyEnabledPostProcessorModules() {
		// Post processor modules have all non post-processor enabled modules as required dependencies
		for ( Map.Entry<AcrossModule, AcrossModuleRole> moduleRole : moduleRoles.entrySet() ) {
			if ( moduleRole.getValue() == AcrossModuleRole.POSTPROCESSOR ) {
				AcrossModule postProcessor = moduleRole.getKey();

				if ( postProcessor.isEnabled() ) {
					for ( Map.Entry<AcrossModule, AcrossModuleRole> targetModuleRole : moduleRoles.entrySet() ) {
						AcrossModule target = targetModuleRole.getKey();

						if ( targetModuleRole.getValue() != AcrossModuleRole.POSTPROCESSOR ) {
							if ( appliedRequiredDependencies.get( target ).contains( postProcessor ) ) {
								LOG.debug(
										"Ignoring {} as required dependency for {} since the former is a postprocessor module",
										postProcessor.getName(), target.getName() );
								appliedRequiredDependencies.get( target ).remove( postProcessor );
							}
							if ( appliedOptionalDependencies.get( target ).contains( postProcessor ) ) {
								LOG.debug(
										"Ignoring {} as optional dependency for {} since the former is a postprocessor module",
										postProcessor.getName(), target.getName() );
								appliedOptionalDependencies.get( target ).remove( postProcessor );
							}

							// Add the target as a required dependency for the post-processor
							appliedRequiredDependencies.get( postProcessor ).add( target );
						}
					}
				}
			}
		}
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

	private void buildDependencies( AcrossModule module ) {
		Annotation depends = AnnotationUtils.getAnnotation( module.getClass(), AcrossDepends.class );

		Set<AcrossModule> requiredByModule = new TreeSet<>( new Comparator<AcrossModule>()
		{
			public int compare( AcrossModule left, AcrossModule right ) {
				return Integer.valueOf( source.indexOf( left ) ).compareTo( source.indexOf( right ) );
			}
		} );
		Set<AcrossModule> optionalForModule = new TreeSet<>( new Comparator<AcrossModule>()
		{
			public int compare( AcrossModule left, AcrossModule right ) {
				return Integer.valueOf( source.indexOf( left ) ).compareTo( source.indexOf( right ) );
			}
		} );

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

	private void determineRole( AcrossModule module ) {
		Annotation role = AnnotationUtils.getAnnotation( module.getClass(), AcrossRole.class );

		if ( role != null ) {
			moduleRoles.put( module,
			                 (AcrossModuleRole) AnnotationUtils.getAnnotationAttributes( role ).get( "value" ) );
		}
		else {
			moduleRoles.put( module, AcrossModuleRole.APPLICATION );
		}
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
		Annotation role = AnnotationUtils.getAnnotation( module.getClass(), AcrossRole.class );

		if ( role != null ) {
			return (Integer) AnnotationUtils.getAnnotationAttributes( role ).get( "order" );
		}

		return 0;
	}
}
