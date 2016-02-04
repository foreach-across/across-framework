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
package com.foreach.across.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds all properties of an installer.
 *
 * @author Arne Vandamme
 */
public class InstallerMetaData
{
	private String name, description, group;
	private int version;
	private InstallerPhase installerPhase;
	private InstallerRunCondition runCondition;
	private Class<?> installerClass;

	public String getName() {
		return name;
	}

	protected void setName( String name ) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	protected void setDescription( String description ) {
		this.description = description;
	}

	/**
	 * @return the installer group this installer belongs to (can be {@code null})
	 */
	public String getGroup() {
		return group;
	}

	protected void setGroup( String group ) {
		this.group = group;
	}

	public int getVersion() {
		return version;
	}

	protected void setVersion( int version ) {
		this.version = version;
	}

	public InstallerPhase getInstallerPhase() {
		return installerPhase;
	}

	protected void setInstallerPhase( InstallerPhase installerPhase ) {
		this.installerPhase = installerPhase;
	}

	public InstallerRunCondition getRunCondition() {
		return runCondition;
	}

	protected void setRunCondition( InstallerRunCondition runCondition ) {
		this.runCondition = runCondition;
	}

	public Class<?> getInstallerClass() {
		return installerClass;
	}

	protected void setInstallerClass( Class<?> installerClass ) {
		this.installerClass = installerClass;
	}

	/**
	 * Returns the list of all methods annotated with {@link com.foreach.across.core.annotations.InstallerMethod}
	 * without method parameters.  Methods can be ordered using {@link org.springframework.core.annotation.Order}.
	 *
	 * @return list of installer methods
	 */
	public Method[] getInstallerMethods() {
		List<Method> methods = new ArrayList<>();

		for ( Method method : ReflectionUtils.getUniqueDeclaredMethods( installerClass ) ) {
			if ( AnnotationUtils.findAnnotation( method, InstallerMethod.class ) != null
					&& method.getParameterCount() == 0 ) {
				methods.add( method );
			}
		}

		AnnotationAwareOrderComparator.sort( methods );

		return methods.toArray( new Method[methods.size()] );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		InstallerMetaData metaData = (InstallerMetaData) o;
		return Objects.equals( getVersion(), metaData.getVersion() ) &&
				Objects.equals( getName(), metaData.getName() ) &&
				Objects.equals( getDescription(), metaData.getDescription() ) &&
				Objects.equals( getGroup(), metaData.getGroup() ) &&
				Objects.equals( getInstallerPhase(), metaData.getInstallerPhase() ) &&
				Objects.equals( getRunCondition(), metaData.getRunCondition() ) &&
				Objects.equals( getInstallerClass(), metaData.getInstallerClass() );
	}

	@Override
	public int hashCode() {
		return Objects.hash( getName(), getDescription(), getGroup(), getVersion(), getInstallerPhase(),
		                     getRunCondition(),
		                     getInstallerClass() );
	}

	/**
	 * Generates the metadata for an installer class.
	 * If the class is not annotated with {@link com.foreach.across.core.annotations.Installer}, an exception will
	 * be thrown.
	 *
	 * @param installerClass of the installer
	 * @return generated metadata
	 */
	public static InstallerMetaData forClass( Class<?> installerClass ) {
		Class<?> actual = ClassUtils.getUserClass( installerClass );

		if ( actual.isInterface() || Modifier.isAbstract( actual.getModifiers() ) ) {
			throw new IllegalArgumentException(
					"@Installer annotated class must be a concrete implementation: " + installerClass );
		}

		Installer metadata = AnnotationUtils.getAnnotation( actual, Installer.class );

		if ( metadata == null ) {
			throw new IllegalArgumentException( "@Installer annotation missing on class: " + installerClass );
		}

		InstallerMetaData profile = new InstallerMetaData();
		profile.setInstallerClass( actual );

		if ( StringUtils.isEmpty( metadata.name() ) ) {
			profile.setName( actual.getName() );
		}
		else {
			profile.setName( metadata.name() );
		}

		profile.setRunCondition( metadata.runCondition() );
		profile.setDescription( metadata.description() );
		profile.setInstallerPhase( metadata.phase() );
		profile.setVersion( metadata.version() );

		InstallerGroup groupAnnotation = AnnotationUtils.findAnnotation( actual, InstallerGroup.class );

		if ( groupAnnotation != null ) {
			profile.setGroup( groupAnnotation.value() );
		}

		return profile;
	}
}
