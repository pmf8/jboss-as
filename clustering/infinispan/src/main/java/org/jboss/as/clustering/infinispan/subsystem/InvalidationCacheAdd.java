package org.jboss.as.clustering.infinispan.subsystem;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

import java.util.Locale;

/**
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class InvalidationCacheAdd extends ClusteredCacheAdd implements DescriptionProvider {

    static final InvalidationCacheAdd INSTANCE = new InvalidationCacheAdd();

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        // transfer the model data from operation to model
        populateClusteredCacheModelNode(operation, model);
    }

    /*
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {

        // create a Configuration holding the operation data
        Configuration overrides = new Configuration() ;
        // create a list for dependencies which may need to be added during processing
        List<CacheAdd.AdditionalDependency> additionalDeps = new LinkedList<AdditionalDependency>() ;

        processClusteredCacheModelNode(operation, overrides, additionalDeps) ;

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
    */

    public ModelNode getModelDescription(Locale locale) {
        return InfinispanDescriptions.getInvalidationCacheAddDescription(locale);
    }

}
