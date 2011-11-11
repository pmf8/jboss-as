package org.jboss.as.clustering.infinispan.subsystem;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

import java.util.Locale;

/**
 * LocalCacheAdd handler
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class LocalCacheAdd extends CacheAdd implements DescriptionProvider {

    static final LocalCacheAdd INSTANCE = new LocalCacheAdd();

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        // transfer the model data from operation to model
        populateCacheModelNode(operation, model);
    }

    /*
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {

        // create a Configuration holding the operation data
        Configuration configuration = new Configuration() ;
        // create a list for dependencies which may need to be added during processing
        List<CacheAdd.AdditionalDependency> additionalDeps = new LinkedList<AdditionalDependency>() ;

        processCacheModelNode(operation, configuration, additionalDeps) ;

        // blend in the configuration data from Defaults
        // register the cache definition
        // start the cache service
    }
    */

    public ModelNode getModelDescription(Locale locale) {
        return InfinispanDescriptions.getLocalCacheAddDescription(locale);
    }

}
