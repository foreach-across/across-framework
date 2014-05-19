package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import org.apache.commons.lang3.StringUtils;
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
public class ModuleBootstrapOrderBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger( ModuleBootstrapOrderBuilder.class );

	private final List<AcrossModule> source;

	private LinkedList<AcrossModule> orderedModules;

	private Map<String, AcrossModule> modulesById = new HashMap<String, AcrossModule>();
	private Map<AcrossModule, Collection<AcrossModule>> requiredDependencies =
			new HashMap<AcrossModule, Collection<AcrossModule>>();
	private Map<AcrossModule, Collection<AcrossModule>> optionalDependencies =
			new HashMap<AcrossModule, Collection<AcrossModule>>();
	private Map<AcrossModule, AcrossModuleRole> moduleRoles = new HashMap<AcrossModule, AcrossModuleRole>();

	public ModuleBootstrapOrderBuilder( Collection<AcrossModule> source ) {
		this.source = new ArrayList<AcrossModule>( source );

		orderModules();
	}

	public Collection<AcrossModule> getOrderedModules() {
		return orderedModules;
	}

	public Collection<AcrossModule> getRequiredDependencies( AcrossModule module ) {
		return requiredDependencies.get( module );
	}

	public Collection<AcrossModule> getOptionalDependencies( AcrossModule module ) {
		return optionalDependencies.get( module );
	}

	public AcrossModuleRole getModuleRole( AcrossModule module ) {
		return moduleRoles.get( module );
	}

	private void orderModules() {
		orderedModules = new LinkedList<AcrossModule>();

		for ( AcrossModule module : source ) {
			modulesById.put( module.getName(), module );
			modulesById.put( module.getClass().getName(), module );
		}

		for ( AcrossModule module : source ) {
			buildDependencies( module );
			determineRole( module );
		}

		applyEnabledInfrastructureModules();

		Map<AcrossModule, Boolean> requiredStack = new HashMap<AcrossModule, Boolean>();

		// Place in required order
		for ( AcrossModule module : source ) {
			place( requiredStack, orderedModules, module );
		}

		// Shuffle optionals if possible
		boolean shuffled;

		do {
			shuffled = false;

			for ( AcrossModule module : source ) {
				if ( getModuleRole( module ) != AcrossModuleRole.INFRASTRUCTURE ) {
					Collection<AcrossModule> optionalModules = getOptionalDependencies( module );

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
		if ( orderedModules.indexOf(moduleToMove) > index ) {
			// Only move if all required dependencies are already before that index
			boolean requirementsMet = true;

			for ( AcrossModule requirement : getRequiredDependencies( moduleToMove ) ) {
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

		for ( AcrossModule requirement : getRequiredDependencies( module ) ) {
			if ( !orderedModules.contains( requirement ) ) {
				if ( requiredStack.containsKey( requirement ) ) {
					throw new RuntimeException(
							"Unable to determine legal module bootstrap order, possible cyclic dependency on module " + requirement.getName() );
				}
				else {
					place( requiredStack, orderedModules, requirement );
				}
			}
		}
/*
		for ( AcrossModule optional : getOptionalDependencies( module ) ) {
			if ( !orderedModules.contains( optional ) && !requiredStack.containsKey( optional ) ) {
				place( requiredStack, orderedModules, optional );
			}
		}
*/
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

						if ( targetModuleRole.getValue() != AcrossModuleRole.INFRASTRUCTURE && !requiredDependencies.get(
								infrastructure ).contains( target ) ) {
							requiredDependencies.get( targetModuleRole.getKey() ).add( moduleRole.getKey() );
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
				for ( AcrossModule dependency : requiredDependencies.get( module ) ) {
					if ( !handled.contains( dependency ) ) {
						throw new RuntimeException(
								"Unable to determine legal module bootstrap order, possible cyclic dependency on module " + dependency.getName() );
					}
				}
			}
		}
	}

	private void buildDependencies( AcrossModule module ) {
		Annotation depends = AnnotationUtils.getAnnotation( module.getClass(), AcrossDepends.class );

		List<AcrossModule> requiredByModule = new LinkedList<AcrossModule>();
		List<AcrossModule> optionalForModule = new LinkedList<AcrossModule>();

		Set<String> definedRequired = new LinkedHashSet<String>();
		Set<String> definedOptional = new LinkedHashSet<String>();

		if ( depends != null ) {
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes( depends );
			String[] required = (String[]) attributes.get( "required" );
			String[] optional = (String[]) attributes.get( "optional" );

			String expression = (String) attributes.get( "expression" );

			if ( !StringUtils.isBlank( expression ) ) {
				LOG.warn(
						"@AcrossDepends expression attribute configured on AcrossModule, but will be ignored as this is not supported." );
			}

			definedRequired.addAll( Arrays.asList( required ) );
			definedOptional.addAll( Arrays.asList( optional ) );
		}

		definedRequired.addAll( module.getRuntimeDependencies() );

		for ( String requiredModule : definedRequired ) {
			if ( !modulesById.containsKey( requiredModule ) ) {
				throw new RuntimeException(
						"Unable to bootstrap AcrossContext as module " + module.getName() + " requires module " + requiredModule + ".  Module " + requiredModule + " is not present in the context." );
			}
			else if ( !modulesById.get( requiredModule ).isEnabled() ) {
				throw new RuntimeException(
						"Unable to bootstrap AcrossContext as module " + module.getName() + " requires module " + requiredModule + ".  Module " + requiredModule + " is present but is not enabled." );
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

		Collections.sort( requiredByModule, new Comparator<AcrossModule>()
		{
			public int compare( AcrossModule left, AcrossModule right ) {
				return Integer.valueOf( source.indexOf( left ) ).compareTo( source.indexOf( right ) );
			}
		} );

		Collections.sort( optionalForModule, new Comparator<AcrossModule>()
		{
			public int compare( AcrossModule left, AcrossModule right ) {
				return Integer.valueOf( source.indexOf( left ) ).compareTo( source.indexOf( right ) );
			}
		} );

		requiredDependencies.put( module, requiredByModule );
		optionalDependencies.put( module, optionalForModule );
	}

	private void determineRole( AcrossModule module ) {
		Annotation role = AnnotationUtils.getAnnotation( module.getClass(), AcrossRole.class );

		if ( role != null ) {
			moduleRoles.put( module,
			                 (AcrossModuleRole) AnnotationUtils.getAnnotationAttributes( role ).get( "value" ) );
		}
		else {
			moduleRoles.put( module, AcrossModuleRole.CUSTOM );
		}
	}
}
