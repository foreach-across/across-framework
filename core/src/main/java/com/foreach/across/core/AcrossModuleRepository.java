package com.foreach.across.core;

import com.foreach.across.core.events.AcrossBootstrapFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean that holds the list of all configured AcrossModule instances.
 * Used for bootstrapping after module beans have been constructed.
 */
@Repository
public class AcrossModuleRepository
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossModuleRepository.class );

	@Autowired
	private List<AcrossModule> modules;

	@Autowired
	private AcrossContext acrossContext;

	@Autowired
	private ApplicationEventPublisher publisher;

	@PostConstruct
	public void bootstrap() throws Exception {
		LOG.debug( "Bootstrapping Across modules" );

		for ( AcrossModule module : modules ) {
			Class moduleClass = module.getClass();

			try {
				moduleClass.getField( "NAME" );
			}
			catch ( NoSuchFieldException nsfe ) {
				LOG.warn(
						"{} does not appear to define a static public field NAME.  By convention it is advised to define this field and use it for the bean name, so other modules can refer it using @DependsOn annotations.",
						module.getClass() );
			}
/*
			LOG.debug( "Bootstrapping module: {}", module.getName() );

			module.bootstrap();
			*/
		}

		// Bootstrapping finished - publish the event
		AcrossBootstrapFinishedEvent e =
				new AcrossBootstrapFinishedEvent( this, acrossContext, new ArrayList<AcrossModule>( modules ) );

		publisher.publishEvent( e );
	}
}
