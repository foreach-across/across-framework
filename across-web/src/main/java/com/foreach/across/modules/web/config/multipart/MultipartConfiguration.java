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
package com.foreach.across.modules.web.config.multipart;

import javax.servlet.MultipartConfigElement;
import javax.servlet.annotation.MultipartConfig;

/**
 * Custom implementation of {@link javax.servlet.MultipartConfigElement} to workaround an issue
 * in JBoss 6.1 where the 3.0 servlet-api contains a broken MultipartConfigElement implementation that ignores
 * the location property.
 *
 * This appears to be fixed with their servlet spec 3.1:
 * https://github.com/jboss/jboss-servlet-api_spec/commit/b09b4be60cfbcfdc55a96f41cc38f6e1f5fafe3c#diff-83ff4a7744a13e526dfbd84617da61cf
 *
 * @author Arne Vandamme
 */
public class MultipartConfiguration extends MultipartConfigElement
{
	private final String location;

	public MultipartConfiguration( String location ) {
		super( location );
		this.location = location != null ? location : "";
	}

	public MultipartConfiguration( String location, long maxFileSize, long maxRequestSize, int fileSizeThreshold ) {
		super( location, maxFileSize, maxRequestSize, fileSizeThreshold );
		this.location = location != null ? location : "";
	}

	public MultipartConfiguration( MultipartConfig annotation ) {
		super( annotation );
		this.location = annotation.location() != null ? annotation.location() : "";
	}

	@Override
	public String getLocation() {
		return location;
	}
}
