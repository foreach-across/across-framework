/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core;

import com.foreach.across.core.context.AcrossEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Contains version information for an {@link com.foreach.across.core.context.AcrossEntity}.
 *
 * @author Arne Vandamme
 */
public class AcrossVersionInfo
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossVersionInfo.class );

	private static final Map<Class, AcrossVersionInfo> versionInfoCache = new HashMap<>();

	public static final AcrossVersionInfo UNKNOWN = new AcrossVersionInfo();

	private static final String UNKNOWN_VALUE = "unknown";

	private Manifest manifest;
	private Date buildTime;
	private String version = UNKNOWN_VALUE;
	private String projectName = UNKNOWN_VALUE;
	private boolean available;

	private AcrossVersionInfo() {
	}

	@SuppressWarnings("all")
	public AcrossVersionInfo( Date buildTime, String version, String projectName ) {
		this.buildTime = buildTime;
		this.version = version;
		this.projectName = projectName;
	}

	@SuppressWarnings("all")
	public AcrossVersionInfo( Manifest manifest, Date buildTime, String version, String projectName ) {
		this.manifest = manifest;
		this.buildTime = buildTime;
		this.version = version;
		this.projectName = projectName;
		this.available = manifest != null;
	}

	public Manifest getManifest() {
		return manifest;
	}

	@SuppressWarnings("all")
	public Date getBuildTime() {
		return buildTime;
	}

	public String getVersion() {
		return version;
	}

	public String getProjectName() {
		return projectName;
	}

	public boolean isSnapshot() {
		return StringUtils.endsWith( version, "-SNAPSHOT" ) || StringUtils.equalsIgnoreCase( version, UNKNOWN_VALUE );
	}

	/**
	 * @return true if version info was found on the classpath (MANIFEST.MF was present)
	 */
	public boolean isAvailable() {
		return available;
	}

	protected void setManifest( Manifest manifest ) {
		this.manifest = manifest;
	}

	protected void setBuildTime( Date buildTime ) {
		this.buildTime = buildTime;
	}

	protected void setVersion( String version ) {
		this.version = version;
	}

	protected void setProjectName( String projectName ) {
		this.projectName = projectName;
	}

	protected void setAvailable( boolean available ) {
		this.available = available;
	}

	/**
	 * Retrieve an {@link AcrossVersionInfo} instance for a given class.  This attempts to read the
	 * {@link Manifest} of the jar/war the class belongs to, and parses common attributes from it.
	 * This method will never return {@code null} but will return a special instance even if no
	 * version information could be loaded.
	 *
	 * @param clazz instance
	 * @return version info instance
	 */
	public static AcrossVersionInfo load( Class<?> clazz ) {
		AcrossVersionInfo versionInfo = versionInfoCache.get( clazz );

		if ( versionInfo == null ) {
			Class<?> c = ClassUtils.getUserClass( clazz );

			versionInfo = UNKNOWN;

			// Retrieve the manifest
			String className = c.getSimpleName() + ".class";
			URL resource = c.getResource( className );

			if ( resource != null ) {
				String classPath = resource.toString();

				if ( classPath.contains( ".jar" ) ) {
					String packageSuffix = c.getName().replace( ".", "/" );
					String manifestPath = classPath.replace( "/" + packageSuffix + ".class", "/META-INF/MANIFEST.MF" );

					LOG.trace( "Loading manifest: {}", manifestPath );

					try (InputStream is = new URL( manifestPath ).openStream()) {
						Manifest manifest = new Manifest( is );
						Attributes attr = manifest.getMainAttributes();

						versionInfo = new AcrossVersionInfo();
						versionInfo.manifest = manifest;
						versionInfo.available = true;
						versionInfo.projectName = StringUtils.defaultString(
								attr.getValue( "Implementation-Title" ), UNKNOWN_VALUE
						);
						versionInfo.version = StringUtils.defaultString(
								attr.getValue( "Implementation-Version" ), UNKNOWN_VALUE
						);

						String buildTime = attr.getValue( "Build-Time" );

						if ( buildTime != null ) {
							try {
								versionInfo.buildTime = DateUtils.parseDate( buildTime, "yyyyMMdd-HHmm",
								                                             "yyyy-MM-dd'T'HH:mm:ss'Z'" );
							}
							catch ( ParseException pe ) {
								LOG.error(
										"Manifest {} specifies Build-Time attribute with value {}, but not in the expected format of yyyyMMdd-HHmm",
										manifestPath, buildTime );
							}
						}
					}
					catch ( IOException ioe ) {
						LOG.warn( "No MANIFEST.MF found at {}", manifestPath );
					}
				}
			}

			if ( AcrossEntity.class.isAssignableFrom( clazz ) ) {
				versionInfoCache.put( clazz, versionInfo );
			}

		}

		return versionInfo;
	}
}
