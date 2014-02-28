package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collection;

public class AnnotationConfigBootstrapApplicationContextFactory implements BootstrapApplicationContextFactory {
    /**
     * Create the Spring ApplicationContext for the root of the AcrossContext.
     * Optionally a parent ApplicationContext can be specified.
     *
     * @param across                   AcrossContext being created.
     * @param parentApplicationContext Parent ApplicationContext, can be null.
     * @return Spring ApplicationContext instance implementing AbstractApplicationContext.
     */
    public AbstractApplicationContext createApplicationContext(AcrossContext across,
                                                               ApplicationContext parentApplicationContext) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        if (parentApplicationContext == null) {
            AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
            parent.refresh();
            parent.start();

            parent.getBeanFactory().registerSingleton("acrossContext", across);

            parentApplicationContext = parent;
        }

        applicationContext.setParent(parentApplicationContext);

        if (parentApplicationContext.getEnvironment() instanceof ConfigurableEnvironment) {
            applicationContext.setEnvironment(
                    (ConfigurableEnvironment) parentApplicationContext.getEnvironment());
        }

        return applicationContext;
    }

    /**
     * Create the Spring ApplicationContext.
     *
     * @param across        AcrossContext being loaded.
     * @param module        AcrossModule being loaded.
     * @param parentContext Contains the parent context.
     * @return Spring ApplicationContext instance implementing AbstractApplicationContext.
     */
    public AbstractApplicationContext createApplicationContext(AcrossContext across,
                                                               AcrossModule module,
                                                               AcrossApplicationContext parentContext) {
        AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();
        child.setParent(parentContext.getApplicationContext());
        child.setEnvironment(parentContext.getApplicationContext().getEnvironment());

        return child;
    }

    /**
     * Loads beans and definitions in the root ApplicationContext.
     *
     * @param across  AcrossContext being loaded.
     * @param context Contains the root Spring ApplicationContext.
     */
    public void loadApplicationContext(AcrossContext across, AcrossApplicationContext context) {
        AnnotationConfigApplicationContext root = (AnnotationConfigApplicationContext) context.getApplicationContext();
        Collection<ApplicationContextConfigurer> configurers = across.getApplicationContextConfigurers().keySet();

        loadApplicationContext(root, configurers);
    }

    /**
     * Loads beans and definitions in the module ApplicationContext.
     *
     * @param across  AcrossContext being loaded.
     * @param module  AcrossModule being loaded.
     * @param context Contains the Spring ApplicationContext for the module.
     */
    public void loadApplicationContext(AcrossContext across, AcrossModule module, AcrossApplicationContext context) {
        AnnotationConfigApplicationContext child = (AnnotationConfigApplicationContext) context.getApplicationContext();
        Collection<ApplicationContextConfigurer> configurers =
                AcrossContextUtils.getConfigurersToApply(across, module);

        loadApplicationContext(child, configurers);
    }

    private void loadApplicationContext(AnnotationConfigApplicationContext context,
                                        Collection<ApplicationContextConfigurer> configurers) {
        for (ApplicationContextConfigurer configurer : configurers) {
            for (BeanFactoryPostProcessor postProcessor : configurer.postProcessors()) {
                context.addBeanFactoryPostProcessor(postProcessor);
            }

            if (!ArrayUtils.isEmpty(configurer.annotatedClasses())) {
                context.register(configurer.annotatedClasses());
            }

            if (!ArrayUtils.isEmpty(configurer.componentScanPackages())) {
                context.scan(configurer.componentScanPackages());
            }
        }

        context.refresh();
        context.start();
    }
}
