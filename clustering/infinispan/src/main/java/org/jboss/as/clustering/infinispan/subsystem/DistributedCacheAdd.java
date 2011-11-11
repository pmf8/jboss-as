package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.config.Configuration;
import org.infinispan.config.FluentConfiguration;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

    /*
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        // create a Configuration holding the operation data
        // blend in the configuration data from Defaults
        // register the cache definition
        // start the cache service
    }

*/
    Configuration processDistributedModelNode(ModelNode cache, Configuration configuration, List<AdditionalDependency> additionalDeps) {

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
            ModelNode rehashing = cache.get(ModelKeys.REHASHING);
            FluentConfiguration.HashConfig fluentHash = fluent.hash();
            if (rehashing.hasDefined(ModelKeys.ENABLED)) {
                fluentHash.rehashEnabled(rehashing.get(ModelKeys.ENABLED).asBoolean());
            }
            if (rehashing.hasDefined(ModelKeys.TIMEOUT)) {
                fluentHash.rehashRpcTimeout(rehashing.get(ModelKeys.TIMEOUT).asLong());
            }
        }
        return configuration;
    }

    public ModelNode getModelDescription(Locale locale) {
        return InfinispanDescriptions.getDistributedCacheAddDescription(locale);
    }

}
