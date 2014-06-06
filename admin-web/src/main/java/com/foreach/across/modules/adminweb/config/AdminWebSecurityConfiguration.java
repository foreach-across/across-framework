package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.spring.security.config.WebSecurityModuleConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminWebSecurityConfiguration implements WebSecurityModuleConfigurer
{
	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AdminWebModule adminWebModule;

	@Override
	public void configure( HttpSecurity http ) throws Exception {
		http.antMatcher(
				adminWebModule.getRootPath() + "/**" ).authorizeRequests().anyRequest().authenticated().and().formLogin().loginPage(
				adminWebModule.getRootPath() + "/login" ).permitAll().and().logout().permitAll();
	}
}
