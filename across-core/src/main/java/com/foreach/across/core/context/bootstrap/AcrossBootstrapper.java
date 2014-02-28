package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.AcrossModuleBeforeBootstrapEvent;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.core.installers.AcrossInstallerRegistry;
import com.foreach.across.core.installers.InstallerPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collection;
import java.util.Map;

/**
 * Takes care of bootstrapping an entire across context.
 */
public class AcrossBootstrapper {
    private static final Logger LOG = LoggerFactory.getLogger(AcrossBootstrapper.class);

    private final AcrossContext context;
    private BootstrapApplicationContextFactory applicationContextFactory;

    public AcrossBootstrapper(AcrossContext context) {
        this.context = context;

        applicationContextFactory = new AnnotationConfigBootstrapApplicationContextFactory();
    }

    public BootstrapApplicationContextFactory getApplicationContextFactory() {
        return applicationContextFactory;
    }

    public void setApplicationContextFactory(BootstrapApplicationContextFactory applicationContextFactory) {
        this.applicationContextFactory = applicationContextFactory;
    }

    /**
     * Bootstraps all modules in the context.
     */
    public void bootstrap() {
        runModuleBootstrapCustomizations();

        AcrossApplicationContext root = createRootContext();
        AcrossContextUtils.setAcrossApplicationContext(context, root);

        applicationContextFactory.loadApplicationContext(context, root);

        AbstractApplicationContext rootContext = root.getApplicationContext();

        // Register module beans
        ConfigurableListableBeanFactory rootBeanFactory = rootContext.getBeanFactory();

        for (AcrossModule module : context.getModules()) {
            if (BeanFactoryUtils.beansOfTypeIncludingAncestors(rootBeanFactory, module.getClass()).isEmpty()) {
                rootBeanFactory.registerSingleton(module.getClass().getName(), module);
            }
        }

        AcrossBeanCopyHelper beanHelper = new AcrossBeanCopyHelper();

        Collection<AcrossModule> modulesInOrder = createOrderedModulesList(context);

        LOG.debug("Bootstrapping {} modules in the following order:", modulesInOrder.size());
        int order = 1;
        for (AcrossModule module : modulesInOrder) {
            LOG.debug("{} - {}: {}", order++, module.getName(), module.getClass());
        }

        AcrossInstallerRegistry installerRegistry = new AcrossInstallerRegistry(context, modulesInOrder);

        // Run installers that don't need anything bootstrapped
        installerRegistry.runInstallers(InstallerPhase.BeforeContextBootstrap);


        for (AcrossModule module : modulesInOrder) {
            LOG.debug("Bootstrapping {} module", module.getName());

            // Run installers before bootstrapping this particular module
            installerRegistry.runInstallersForModule(module, InstallerPhase.BeforeModuleBootstrap);

            // Create the module context
            AbstractApplicationContext child =
                    applicationContextFactory.createApplicationContext(context, module, root);

            AcrossApplicationContext moduleApplicationContext = new AcrossApplicationContext(child, root);
            AcrossContextUtils.setAcrossApplicationContext(module, moduleApplicationContext);

            context.publishEvent(new AcrossModuleBeforeBootstrapEvent(context, module));

            applicationContextFactory.loadApplicationContext(context, module, moduleApplicationContext);

            // Bootstrap the module
            module.bootstrap();

            // Send event that this module has bootstrapped
            context.publishEvent(new AcrossModuleBootstrappedEvent(context, module));

            // Run installers after module itself has bootstrapped
            installerRegistry.runInstallersForModule(module, InstallerPhase.AfterModuleBootstrap);

            // Copy the beans to the parent context
            beanHelper.copy(child, rootContext, module.getExposeFilter());

            AcrossContextUtils.autoRegisterEventHandlers(child, rootContext.getBean(AcrossEventPublisher.class));
        }

        // Bootstrapping done, run installers that require context bootstrap finished
        installerRegistry.runInstallers(InstallerPhase.AfterContextBoostrap);

        LOG.debug("Bootstrapping {} modules - finished", modulesInOrder.size());

        if (rootContext.getParent() != null && rootContext.getParent() instanceof ConfigurableApplicationContext) {
            pushDefinitionsToParent(beanHelper, (ConfigurableApplicationContext) rootContext.getParent());
        }

        // Refresh beans
        AcrossContextUtils.refreshBeans(context);

        // Bootstrap finished - publish the event
        context.publishEvent(new AcrossContextBootstrappedEvent(context));
    }

    private Collection<AcrossModule> createOrderedModulesList(AcrossContext context) {
        return BootstrapAcrossModuleOrder.create(context.getModules());
    }

    private void runModuleBootstrapCustomizations() {
        for (AcrossModule module : context.getModules()) {
            if (module instanceof BootstrapAdapter) {
                ((BootstrapAdapter) module).customizeBootstrapper(this);
            }
        }
    }

    private AcrossApplicationContext createRootContext() {
        ApplicationContext parent = context.getParentApplicationContext();

        AbstractApplicationContext rootApplicationContext =
                applicationContextFactory.createApplicationContext(context, parent);

        return new AcrossApplicationContext(rootApplicationContext);
    }

    private void pushDefinitionsToParent(AcrossBeanCopyHelper beanCopyHelper,
                                         ConfigurableApplicationContext applicationContext) {
        for (Map.Entry<String, Object> singleton : beanCopyHelper.getSingletonsCopied().entrySet()) {
            applicationContext.getBeanFactory().registerSingleton(singleton.getKey(), singleton.getValue());
        }

        if (applicationContext instanceof GenericApplicationContext) {
            for (Map.Entry<String, BeanDefinition> beanDef : beanCopyHelper.getDefinitionsCopied().entrySet()) {
                ((GenericApplicationContext) applicationContext).registerBeanDefinition(beanDef.getKey(),
                        beanDef.getValue());
            }
        }
    }
}
