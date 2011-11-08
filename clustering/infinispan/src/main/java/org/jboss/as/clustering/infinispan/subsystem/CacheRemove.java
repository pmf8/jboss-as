package org.jboss.as.clustering.infinispan.subsystem;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

import java.util.Locale;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class CacheRemove extends AbstractRemoveStepHandler implements DescriptionProvider {

    static final CacheRemove INSTANCE = new CacheRemove();

    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) {
        // get container and cache addresses
        final PathAddress cacheAddress = PathAddress.pathAddress(operation.get(OP_ADDR)) ;
        final PathAddress containerAddress = cacheAddress.subAddress(0, cacheAddress.size()-1) ;
        // get container and cache names
        final String cacheName = cacheAddress.getLastElement().getValue() ;
        final String containerName = containerAddress.getLastElement().getValue() ;

        // remove the CacheService instance
        context.removeService(EmbeddedCacheManagerService.getServiceName(containerName).append(cacheName));
    }

    protected void recoverServices(OperationContext context, ModelNode operation, ModelNode model) {
        // TODO:  RE-ADD SERVICES
    }

    public ModelNode getModelDescription(Locale locale) {
        return LocalDescriptions.getCacheRemoveDescription(locale);
    }

}
