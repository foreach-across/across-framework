package com.foreach.across.modules.hibernate;

import com.foreach.across.core.AcrossModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfiguration
{
	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE_QUALIFIER)
	private AcrossHibernateModule module;

}
