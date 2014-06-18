package com.foreach.across.modules.spring.security.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.registry.RefreshableRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@AcrossDepends(required = "AcrossWebModule")
@EnableWebMvcSecurity
public class GlobalWebSecurityConfiguration
{
//	public static class Test
//	{
//		private RefreshableRegistry<WebSecurityConfigurer> registry;
//
//		Test( RefreshableRegistry<WebSecurityConfigurer> registry ) {
//			this.registry = registry;
//		}
//
//		public Collection<WebSecurityConfigurer> getWebSecurityConfigurers() {
//			Collection<WebSecurityConfigurer> list = registry.getMembers();
//			Collection<WebSecurityConfigurer> delegates = new ArrayList<>( list.size() );
//
//			int pos = 0;
//			for ( WebSecurityConfigurer configurer : list ) {
//				delegates.add( new OrderedWebSecurityConfigurerDelegate( configurer, pos++ ) );
//			}
//
//			return delegates;
//		}
//	}
//
//	public static class OrderedWebSecurityConfigurerDelegate extends WebSecurityConfigurerAdapter implements WebSecurityConfigurer, Ordered
//	{
//		private int order;
//		private WebSecurityConfigurer target;
//
//		public OrderedWebSecurityConfigurerDelegate( WebSecurityConfigurer target, int order ) {
//			this.target = target;
//			this.order = order;
//		}
//
//		@Override
//		public int getOrder() {
//			return order;
//		}
//
//		@Override
//		public void init( SecurityBuilder builder ) throws Exception {
//			target.init( builder );
//		}
//
//		@Override
//		public void configure( SecurityBuilder builder ) throws Exception {
//			target.configure( builder );
//		}
//	}
//
//	@Bean
//	RefreshableRegistry<WebSecurityConfigurer> configurers() {
//		return new RefreshableRegistry<>( WebSecurityConfigurer.class, true );
//	}
//
//	@Bean
//	public Test autowiredWebSecurityConfigurersIgnoreParents() {
//		return new Test( configurers() );
//	}
}
