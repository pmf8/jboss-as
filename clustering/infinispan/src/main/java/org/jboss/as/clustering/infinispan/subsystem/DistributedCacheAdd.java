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
public class DistributedCacheAdd extends ClusteredCacheAdd implements DescriptionProvider {

    static final DistributedCacheAdd INSTANCE = new DistributedCacheAdd();

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        // transfer the model data from operation to model
        populateClusteredCacheModelNode(operation, model);

        // process additional attributes
        if (operation.hasDefined(ModelKeys.OWNERS)) {
            model.get(ModelKeys.OWNERS).set(operation.get(ModelKeys.OWNERS)) ;
        }
        if (operation.hasDefined(ModelKeys.VIRTUAL_NODES)) {
            model.get(ModelKeys.VIRTUAL_NODES).set(operation.get(ModelKeys.VIRTUAL_NODES)) ;
        }
        if (operation.hasDefined(ModelKeys.L1_LIFESPAN)) {
            model.get(ModelKeys.L1_LIFESPAN).set(operation.get(ModelKeys.L1_LIFESPAN)) ;
        }
        if (operation.hasDefined(ModelKeys.REHASHING)) {
            // indicate that a rehashing child is defined
            model.get(ModelKeys.REHASHING).set(operation.get(ModelKeys.REHASHING));
            List<String> attributeList = Arrays.asList(ModelKeys.ENABLED, ModelKeys.TIMEOUT);
            copyFlattenedElementsToModel(operation, model, ModelKeys.REHASHING, attributeList);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {

        Configuration overrides = new Configuration() ;
        // create a list for dependencies which may need to be added during processing
        List<AdditionalDependency> additionalDeps = new LinkedList<AdditionalDependency>() ;

        processDistributedCacheModelNode(model, overrides, additionalDeps) ;

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

    Configuration processDistributedCacheModelNode(ModelNode cache, Configuration configuration, List<AdditionalDependency> additionalDeps) {

        // process the basic clustered configuration
        processClusteredCacheModelNode(cache, configuration, additionalDeps) ;

        // process the additional distributed attributes and elements
        FluentConfiguration fluent = configuration.fluent();
        if (cache.hasDefined(ModelKeys.OWNERS)) {
            fluent.hash().numOwners(cache.get(ModelKeys.OWNERS).asInt());
        }
        if (cache.hasDefined(ModelKeys.VIRTUAL_NODES)) {
            fluent.hash().numVirtualNodes(cache.get(ModelKeys.VIRTUAL_NODES).asInt());
        }
        if (cache.hasDefined(ModelKeys.L1_LIFESPAN)) {
            long lifespan = cache.get(ModelKeys.L1_LIFESPAN).asLong();
            if (lifespan > 0) {
                fluent.l1().lifespan(lifespan);
            } else {
                fluent.l1().disable();
            }
        }
        if (cache.hasDefined(ModelKeys.REHASHING)) {
            FluentConfiguration.HashConfig fluentHash = fluent.hash();
            if (cache.hasDefined(flatten(ModelKeys.REHASHING, ModelKeys.ENABLED))) {
                fluentHash.rehashEnabled(cache.get(flatten(ModelKeys.REHASHING, ModelKeys.ENABLED)).asBoolean());
            }
            if (cache.hasDefined(flatten(ModelKeys.REHASHING,ModelKeys.TIMEOUT))) {
                fluentHash.rehashRpcTimeout(cache.get(flatten(ModelKeys.REHASHING, ModelKeys.TIMEOUT)).asLong());
            }
        }
        return configuration;
    }

    public ModelNode getModelDescription(Locale locale) {
        return InfinispanDescriptions.getDistributedCacheAddDescription(locale);
    }

}
