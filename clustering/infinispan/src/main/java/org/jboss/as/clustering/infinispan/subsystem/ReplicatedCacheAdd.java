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

    /*
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        // create a Configuration holding the operation data
        Configuration configuration = new Configuration();
        // create a list for dependencies which may need to be added during processing
        List<CacheAdd.AdditionalDependency> additionalDeps = new LinkedList<AdditionalDependency>();

        processReplicatedCacheModelNode(operation, configuration, additionalDeps);

        // blend in the configuration data from Defaults
        // register the cache definition
        // start the cache service
    }
    */

    Configuration processReplicatedCacheModelNode(ModelNode cache, Configuration configuration, List<AdditionalDependency> additionalDeps) {
        // process the basic clustered configuration
        processClusteredCacheModelNode(cache, configuration, additionalDeps);

        // process the replicated-cache attributes and elements
        FluentConfiguration fluent = configuration.fluent();
        if (cache.hasDefined(ModelKeys.STATE_TRANSFER)) {
            ModelNode stateTransfer = cache.get(ModelKeys.STATE_TRANSFER);
            FluentConfiguration.StateRetrievalConfig fluentStateTransfer = fluent.stateRetrieval();
            if (stateTransfer.hasDefined(ModelKeys.ENABLED)) {
                fluentStateTransfer.fetchInMemoryState(stateTransfer.get(ModelKeys.ENABLED).asBoolean());
            }
            if (stateTransfer.hasDefined(ModelKeys.TIMEOUT)) {
                fluentStateTransfer.timeout(stateTransfer.get(ModelKeys.TIMEOUT).asLong());
            }
            if (stateTransfer.hasDefined(ModelKeys.FLUSH_TIMEOUT)) {
                fluentStateTransfer.logFlushTimeout(stateTransfer.get(ModelKeys.FLUSH_TIMEOUT).asLong());
            }
        }
        return configuration;
    }

    public ModelNode getModelDescription(Locale locale) {
        return InfinispanDescriptions.getReplicatedCacheAddDescription(locale);
    }
}
