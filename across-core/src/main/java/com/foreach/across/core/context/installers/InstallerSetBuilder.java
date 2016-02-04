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
package com.foreach.across.core.context.installers;

import com.foreach.across.core.installers.InstallerMetaData;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Merges support for manually added installers with installers scanned from packages.
 *
 * @author Arne Vandamme
 */
public class InstallerSetBuilder
{
	private Map<String, Object> candidateMap = new LinkedHashMap<>();

	/**
	 * Add one or more installers either as instance or class.
	 *
	 * @param installerOrClass installer instance of class
	 */
	public void add( Object... installerOrClass ) {
		for ( Object candidate : installerOrClass ) {
			validateAndAddCandidate( candidate );
		}
	}

	/**
	 * Scan one or more packages for installers.
	 *
	 * @param basePackages to scan
	 */
	public void scan( String... basePackages ) {
		new ClassPathScanningInstallerProvider().scan( basePackages ).forEach( this::validateAndAddCandidate );
	}

	/**
	 * @return list of unique installers
	 */
	public Object[] build() {
		List<Object> installers = new ArrayList<>();
		installers.addAll( candidateMap.values() );

		final Map<Object, Integer> registrationOrderMap = new HashMap<>();
		int ix = 0;
		for ( Object installer : installers ) {
			registrationOrderMap.put( installer, ix++ );
		}

		Collections.sort( installers, ( left, right ) -> {
			Class<?> leftType = left instanceof Class ? (Class<?>) left : left.getClass();
			Class<?> rightType = right instanceof Class ? (Class<?>) right : right.getClass();

			int leftOrder = OrderUtils.getOrder( leftType, 0 );
			int rightOrder = OrderUtils.getOrder( rightType, 0 );

			int comparison = Integer.compare( leftOrder, rightOrder );

			if ( comparison == 0 ) {
				return Integer.compare( registrationOrderMap.getOrDefault( left, Ordered.LOWEST_PRECEDENCE ),
				                        registrationOrderMap.getOrDefault( right, Ordered.LOWEST_PRECEDENCE ) );
			}

			return comparison;
		} );

		return installers.toArray();
	}

	private void validateAndAddCandidate( Object candidate ) {
		Assert.notNull( candidate );

		if ( !candidateMap.containsValue( candidate ) ) {
			Class<?> installerClass = installerClass( candidate );

			InstallerMetaData metaData = InstallerMetaData.forClass( installerClass );

			if ( candidateMap.containsKey( metaData.getName() ) ) {
				Class<?> otherInstaller = installerClass( candidateMap.get( metaData.getName() ) );

				if ( !otherInstaller.equals( installerClass ) ) {
					throw new IllegalArgumentException(
							"Another installer was already registered with name: " + metaData.getName() );
				}
			}
			else {
				candidateMap.put( metaData.getName(), candidate );
			}
		}
	}

	private Class<?> installerClass( Object installer ) {
		if ( installer instanceof Class ) {
			return (Class<?>) installer;
		}

		return installer.getClass();
	}
}
