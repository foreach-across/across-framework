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
package com.foreach.across.database.support;

import com.zaxxer.hikari.HikariConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public final class HikariDataSourceHelper
{
    private static final String DATASOURCES_PREFIX = "datasources.";
    private HikariDataSourceHelper() {
    }

    public static HikariConfig create( String datasourcePrefix, ConfigurableEnvironment propertyResolver ) {
        Properties applicationProperties = getProperties(propertyResolver);
        Properties datasourcesProperties = new Properties();
        datasourcePrefix += ".";
        for( Map.Entry<Object, Object> props : applicationProperties.entrySet() ) {
            Object key = props.getKey();
            String cleanedKey =  DATASOURCES_PREFIX + datasourcePrefix;
            if( key.toString().startsWith(DATASOURCES_PREFIX) && key.toString().startsWith( cleanedKey) ) {
                datasourcesProperties.put( key.toString().substring( cleanedKey.length() ), props.getValue() );
            }
        }

        String jndiProperty = applicationProperties.getProperty(DATASOURCES_PREFIX + datasourcePrefix + "jndi");
        if (StringUtils.isNotEmpty(jndiProperty)) {
            final JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
            dataSourceLookup.setResourceRef(true);
            DataSource dataSourceTemp;
            try {
                dataSourceTemp = dataSourceLookup.getDataSource(jndiProperty);
            } catch (DataSourceLookupFailureException e) {
                throw new IllegalArgumentException("Datasource " + jndiProperty + " not found", e);
            }

            datasourcesProperties.remove("jndi");
            HikariConfig config = new HikariConfig(datasourcesProperties);
            config.setDataSource( dataSourceTemp );
            return config;
        } else {
            return new HikariConfig(datasourcesProperties);
        }
    }

    private static Properties getProperties( ConfigurableEnvironment propertyResolver ) {
        MutablePropertySources propertySources = propertyResolver.getPropertySources();
        Properties properties = new Properties();
        for (org.springframework.core.env.PropertySource<?> source : propertySources) {
            if( source instanceof EnumerablePropertySource) {
                EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) source;
                String[] props = enumerablePropertySource.getPropertyNames();
                for( String prop : props ) {
                    properties.put( prop, enumerablePropertySource.getProperty(prop) );
                }
            }
        }
        return properties;
    }
}
