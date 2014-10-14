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
package com.foreach.across.test;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by marc on 14/10/2014.
 */
public class MockServletRegistration implements ServletRegistration.Dynamic
{
	@Override
	public void setLoadOnStartup( int loadOnStartup ) {

	}

	@Override
	public Set<String> setServletSecurity( ServletSecurityElement constraint ) {
		return null;
	}

	@Override
	public void setMultipartConfig( MultipartConfigElement multipartConfig ) {

	}

	@Override
	public void setRunAsRole( String roleName ) {

	}

	@Override
	public void setAsyncSupported( boolean isAsyncSupported ) {

	}

	@Override
	public Set<String> addMapping( String... urlPatterns ) {
		return null;
	}

	@Override
	public Collection<String> getMappings() {
		return null;
	}

	@Override
	public String getRunAsRole() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getClassName() {
		return null;
	}

	@Override
	public boolean setInitParameter( String name, String value ) {
		return false;
	}

	@Override
	public String getInitParameter( String name ) {
		return null;
	}

	@Override
	public Set<String> setInitParameters( Map<String, String> initParameters ) {
		return null;
	}

	@Override
	public Map<String, String> getInitParameters() {
		return null;
	}
}
