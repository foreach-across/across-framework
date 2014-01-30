package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Creates an ordered list of all modules, depending on the AcrossDepends annotations configured.
 * This class creates the optimal bootstrap order for all modules and can throw an exception if two
 * modules have a cyclic dependency and are impossible to bootstrap (very bad - impossible to fix).
 */
public class BootstrapAcrossModuleOrder
{
	private Collection<AcrossModule> source;

	private Map<String, AcrossModule> modulesById = new HashMap<String, AcrossModule>();
	private Map<AcrossModule, Collection<AcrossModule>> requiredDependencies =
			new HashMap<AcrossModule, Collection<AcrossModule>>();
	private Map<AcrossModule, Collection<AcrossModule>> optionalDependencies =
			new HashMap<AcrossModule, Collection<AcrossModule>>();
	private Map<AcrossModule, AcrossModuleRole> moduleRoles = new HashMap<AcrossModule, AcrossModuleRole>();

	public BootstrapAcrossModuleOrder( Collection<AcrossModule> source ) {
		this.source = source;
	}

	public Collection<AcrossModule> getOrderedModules() {
		reset();

		LinkedList<AcrossModule> ordered = new LinkedList<AcrossModule>();

		for ( AcrossModule module : source ) {
			modulesById.put( module.getName(), module );
			modulesById.put( module.getClass().getName(), module );

			ordered.add( module );
		}

		for ( AcrossModule module : source ) {
			buildDependencies( module );
			determineRole( module );
		}

		applyInfrastructureModules();

		for ( AcrossModule module : source ) {
			int currentPosition = ordered.indexOf( module );

			if ( currentPosition == -1 ) {
				currentPosition = ordered.isEmpty() ? 0 : ordered.size() - 1;
				ordered.add( currentPosition, module );
			}

			// Dependencies push actual module to the back
			if ( moduleRoles.get( module ) != AcrossModuleRole.INFRASTRUCTURE ) {
				// optional dependencies do not influence the order of infrastructure modules
				ordered.addAll( currentPosition, optionalDependencies.get( module ) );
			}

			ordered.addAll( currentPosition, requiredDependencies.get( module ) );
		}

		verifyModuleList( ordered );

		return ordered;
	}

	private void reset() {
		modulesById.clear();
		requiredDependencies.clear();
		optionalDependencies.clear();
	}

	private void applyInfrastructureModules() {
		for ( Map.Entry<AcrossModule, AcrossModuleRole> moduleRole : moduleRoles.entrySet() ) {
			if ( moduleRole.getValue() == AcrossModuleRole.INFRASTRUCTURE ) {
				AcrossModule infrastructure = moduleRole.getKey();

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

	private void verifyModuleList( LinkedList<AcrossModule> ordered ) {
		Set<AcrossModule> handled = new HashSet<AcrossModule>();

		Iterator<AcrossModule> iterator = ordered.iterator();

		while ( iterator.hasNext() ) {
			AcrossModule module = iterator.next();

			// Required dependencies should have already been handled
			for ( AcrossModule dependency : requiredDependencies.get( module ) ) {
				if ( !handled.contains( dependency ) ) {
					throw new RuntimeException(
							"Unable to determine legal module boostrap order, possible cyclic dependency on module " + dependency.getName() );
				}
			}

			// Remove module if already handled
			if ( handled.contains( module ) ) {
				iterator.remove();
			}
			else {
				handled.add( module );
			}
		}
	}

	private void buildDependencies( AcrossModule module ) {
		Annotation depends = AnnotationUtils.getAnnotation( module.getClass(), AcrossDepends.class );

		Set<AcrossModule> requiredByModule = new LinkedHashSet<AcrossModule>();
		Set<AcrossModule> optionalForModule = new LinkedHashSet<AcrossModule>();

		if ( depends != null ) {
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes( depends );
			String[] required = (String[]) attributes.get( "required" );
			String[] optional = (String[]) attributes.get( "optional" );

			for ( String requiredModule : required ) {
				if ( !modulesById.containsKey( requiredModule ) ) {
					throw new RuntimeException(
							"Unable to bootstrap AcrossContext as module " + module.getName() + " requires module " + requiredModule + ".  Module " + requiredModule + " is not present in the context." );
				}

				requiredByModule.add( modulesById.get( requiredModule ) );
			}

			for ( String optionalModule : optional ) {
				if ( modulesById.containsKey( optionalModule ) ) {
					optionalForModule.add( modulesById.get( optionalModule ) );
				}
			}
		}

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

	public static Collection<AcrossModule> create( Collection<AcrossModule> modules ) {
		return new BootstrapAcrossModuleOrder( modules ).getOrderedModules();
	}
}
