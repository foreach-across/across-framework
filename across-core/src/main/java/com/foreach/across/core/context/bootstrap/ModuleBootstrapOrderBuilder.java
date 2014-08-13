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
		orderedModules = new LinkedList<>();

		for ( AcrossModule module : source ) {
			modulesById.put( module.getName(), module );
			modulesById.put( module.getClass().getName(), module );
		}

		for ( AcrossModule module : source ) {
			buildDependencies( module );
			determineRole( module );
		}

		applyEnabledInfrastructureModules();
		applyEnabledPostProcessorModules();

		Map<AcrossModule, Boolean> requiredStack = new HashMap<>();

		// Place in required order
		for ( AcrossModule module : source ) {
			place( requiredStack, orderedModules, module );
		}

		// Shuffle optionals if possible
		boolean shuffled;

		do {
			shuffled = false;

			for ( AcrossModule module : source ) {
				AcrossModuleRole role = getModuleRole( module );
				if ( role != AcrossModuleRole.INFRASTRUCTURE && role != AcrossModuleRole.POSTPROCESSOR ) {
					Collection<AcrossModule> optionalModules = getAppliedOptionalDependencies( module );

					for ( AcrossModule optional : optionalModules ) {
						if ( moveToIndexIfPossible( orderedModules, optional, orderedModules.indexOf( module ) ) ) {
							shuffled = true;
						}
					}
				}
			}
		}
		while ( shuffled );

		verifyModuleList( orderedModules );
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
		for ( Map.Entry<AcrossModule, AcrossModuleRole> moduleRole : moduleRoles.entrySet() ) {
			if ( moduleRole.getValue() == AcrossModuleRole.INFRASTRUCTURE ) {
				AcrossModule infrastructure = moduleRole.getKey();

				if ( infrastructure.isEnabled() ) {
					// Infrastructure modules are added as required dependencies to all non-infrastructure modules
					// Indirect dependencies
					for ( Map.Entry<AcrossModule, AcrossModuleRole> targetModuleRole : moduleRoles.entrySet() ) {
						AcrossModule target = targetModuleRole.getKey();

						if ( targetModuleRole
								.getValue() != AcrossModuleRole.INFRASTRUCTURE && !appliedRequiredDependencies.get(
								infrastructure ).contains( target ) ) {
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
}
