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
package com.foreach.across.modules.web.context;

/**
 * API for building client-side valid links from within application code.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public interface WebAppLinkBuilder
{
	/**
	 * Convert the base link to an actual client-side link by processing prefixes,
	 * adding the context path and encoding the resulting url.
	 *
	 * @param baseLink to process
	 * @return processed link
	 */
	String buildLink( String baseLink );

	/**
	 * Convert the base link to an actual client-side link by processing prefixes,
	 * adding the context path and (optionally) encoding the resulting url.
	 *
	 * @param baseLink  to process
	 * @param encodeUrl true if the url should be encoded afterwards (appends session id)
	 * @return processed link
	 */
	String buildLink( String baseLink, boolean encodeUrl );
}
