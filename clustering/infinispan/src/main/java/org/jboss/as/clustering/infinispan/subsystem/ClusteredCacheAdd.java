package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.config.Configuration;
import org.infinispan.config.FluentConfiguration;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.protocol.ProtocolChannelClient;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

import java.util.List;

/**
 * Base class for clustered cache add operations
 *
 * @author Richard Achmatowicz  (c) 2011 RedHat Inc.
 */
public class ClusteredCacheAdd extends CacheAdd {

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {

    }


    /**
     * Transfer operation ModelNode values to model ModelNode values
     *
     * @param operation
     * @param model
     * @throws OperationFailedException
     */
    void populateClusteredCacheModelNode(ModelNode operation, ModelNode model) throws OperationFailedException {

        // populate attributes
        if (operation.hasDefined(ModelKeys.MODE)) {
            model.get(ModelKeys.MODE).set(operation.get(ModelKeys.MODE)) ;
        }
        if (operation.hasDefined(ModelKeys.QUEUE_SIZE)) {
            model.get(ModelKeys.QUEUE_SIZE).set(operation.get(ModelKeys.QUEUE_SIZE)) ;
        }
        if (operation.hasDefined(ModelKeys.QUEUE_FLUSH_INTERVAL)) {
            model.get(ModelKeys.QUEUE_FLUSH_INTERVAL).set(operation.get(ModelKeys.QUEUE_FLUSH_INTERVAL)) ;
        }
        if (operation.hasDefined(ModelKeys.REMOTE_TIMEOUT)) {
            model.get(ModelKeys.REMOTE_TIMEOUT).set(operation.get(ModelKeys.REMOTE_TIMEOUT)) ;
        }
    }

    /**
     * Create a Configuration object initialized from the data in the operation.
     *
     * @param cache data representing cache configuration
     * @param configuration Configuration to add the data to
     * @return initialised Configuration object
     */
    Configuration processClusteredCacheModelNode(ModelNode cache, Configuration configuration, List<AdditionalDependency> additionalDeps) {

        // process clustered cache attributes and elements
        FluentConfiguration fluent = configuration.fluent();
        if (cache.hasDefined(ModelKeys.QUEUE_SIZE)) {
            fluent.async().replQueueMaxElements(cache.get(ModelKeys.QUEUE_SIZE).asInt());
        }
        if (cache.hasDefined(ModelKeys.QUEUE_FLUSH_INTERVAL)) {
            fluent.async().replQueueInterval(cache.get(ModelKeys.QUEUE_FLUSH_INTERVAL).asLong());
        }
        if (cache.hasDefined(ModelKeys.REMOTE_TIMEOUT)) {
            fluent.sync().replTimeout(cache.get(ModelKeys.REMOTE_TIMEOUT).asLong());
        }

        return configuration ;
    }
}
