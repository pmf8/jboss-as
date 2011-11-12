package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.FluentConfiguration;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class ReplicatedCacheAdd extends ClusteredCacheAdd implements DescriptionProvider {

    static final ReplicatedCacheAdd INSTANCE = new ReplicatedCacheAdd();

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        // transfer the model data from operation to model
        populateClusteredCacheModelNode(operation, model);

        // process additional element
        if (operation.hasDefined(ModelKeys.STATE_TRANSFER)) {
            // indicate that a stare transfer child is defined
            model.get(ModelKeys.STATE_TRANSFER).set(operation.get(ModelKeys.STATE_TRANSFER));
            List<String> attributeList = Arrays.asList(ModelKeys.ENABLED, ModelKeys.TIMEOUT, ModelKeys.FLUSH_TIMEOUT);
            copyFlattenedElementsToModel(operation, model, ModelKeys.STATE_TRANSFER, attributeList);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        // create a Configuration holding the operation data
        Configuration overrides = new Configuration();
        // create a list for dependencies which may need to be added during processing
        List<CacheAdd.AdditionalDependency> additionalDeps = new LinkedList<AdditionalDependency>();

        processReplicatedCacheModelNode(operation, overrides, additionalDeps);

        // this stuff can go into a common routine in CacheAdd

        // get container and cache addresses
        PathAddress cacheAddress = PathAddress.pathAddress(operation.get(OP_ADDR)) ;
        PathAddress containerAddress = cacheAddress.subAddress(0, cacheAddress.size()-1) ;

        // get container and cache names
        String cacheName = cacheAddress.getLastElement().getValue() ;
        String containerName = containerAddress.getLastElement().getValue() ;

        // get container and cache service names
        ServiceName containerServiceName = EmbeddedCacheManagerService.getServiceName(containerName) ;
        ServiceName cacheServiceName = containerServiceName.append(cacheName) ;

        // get container Model
        Resource rootResource = context.getRootResource() ;
        ModelNode container = rootResource.navigate(containerAddress).getModel() ;

        // get default cache of the container
        String defaultCache = container.require(ModelKeys.DEFAULT_CACHE).asString() ;

        // get start mode of the cache
        StartMode startMode = operation.hasDefined(ModelKeys.START) ? StartMode.valueOf(operation.get(ModelKeys.START).asString()) : StartMode.LAZY;

        // get the JNDI name of the container and its binding info
        String jndiName = CacheContainerAdd.getContainerJNDIName(container, containerName);
        final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(jndiName) ;

        // install the cache service
        ServiceTarget target = context.getServiceTarget() ;
        // create the CacheService name
        ServiceName serviceName = EmbeddedCacheManagerService.getServiceName(containerName).append(cacheName) ;
        // create the CacheService instance
        // need to add in overrides
        ServiceBuilder<Cache<Object, Object>> builder = new CacheService<Object, Object>(cacheName, overrides).build(target, containerServiceName) ;
        builder.addDependency(bindInfo.getBinderServiceName()) ;

        // add in any additional dependencies
        for (AdditionalDependency dep : additionalDeps) {
            builder.addDependency(dep.getName(), dep.getType(), dep.getTarget()) ;
        }

        builder.setInitialMode(startMode.getMode());
        // add an alias for the default cache
        if (cacheName.equals(defaultCache)) {
            builder.addAliases(CacheService.getServiceName(containerName,  null));
        }
        // blah
        if (startMode.getMode() == ServiceController.Mode.ACTIVE) {
            builder.addListener(verificationHandler);
        }

        newControllers.add(builder.install());

    }

    Configuration processReplicatedCacheModelNode(ModelNode cache, Configuration configuration, List<AdditionalDependency> additionalDeps) {
        // process the basic clustered configuration
        processClusteredCacheModelNode(cache, configuration, additionalDeps);

        // process the replicated-cache attributes and elements
        FluentConfiguration fluent = configuration.fluent();
        if (cache.hasDefined(ModelKeys.STATE_TRANSFER)) {
            FluentConfiguration.StateRetrievalConfig fluentStateTransfer = fluent.stateRetrieval();
            if (cache.hasDefined(flatten(ModelKeys.STATE_TRANSFER, ModelKeys.ENABLED))) {
                fluentStateTransfer.fetchInMemoryState(cache.get(flatten(ModelKeys.STATE_TRANSFER, ModelKeys.ENABLED)).asBoolean());
            }
            if (cache.hasDefined(flatten(ModelKeys.STATE_TRANSFER,ModelKeys.TIMEOUT))) {
                fluentStateTransfer.timeout(cache.get(flatten(ModelKeys.STATE_TRANSFER, ModelKeys.TIMEOUT)).asLong());
            }
            if (cache.hasDefined(flatten(ModelKeys.STATE_TRANSFER, ModelKeys.FLUSH_TIMEOUT))) {
                fluentStateTransfer.logFlushTimeout(cache.get(flatten(ModelKeys.STATE_TRANSFER, ModelKeys.FLUSH_TIMEOUT)).asLong());
            }
        }
        return configuration;
    }

    public ModelNode getModelDescription(Locale locale) {
        return InfinispanDescriptions.getReplicatedCacheAddDescription(locale);
    }
}
