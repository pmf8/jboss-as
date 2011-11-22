package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.rpc.RpcManagerImpl;
import org.jboss.as.controller.AbstractRuntimeOnlyHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.EnumSet;
import java.util.Locale;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Handles operation and attribute reads of an RpcManager component.
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class RpcManagerHandler extends AbstractRuntimeOnlyHandler {

    private static final Logger log = Logger.getLogger(RpcManagerHandler.class.getPackage().getName());
    public static final RpcManagerHandler INSTANCE = new RpcManagerHandler();

    // attribute and operation names
    public static final String AVERAGE_REPLICATION_TIME = "average-replication-time" ;
    public static final String REPLICATION_COUNT = "replication-count" ;
    public static final String REPLICATION_FAILURES = "replication-failures" ;
    public static final String SUCCESS_RATIO = "success-ratio" ;
    public static final String SUCCESS_RATIO_FLOATING_POINT = "success-ratio-floating-point" ;
    public static final String STATISTICS_ENABLED = "statistics-enabled" ;
    public static final String RESET_STATISTICS = "reset-statistics" ;

    // attribute definitions
    public static final AttributeDefinition AVERAGE_REPLICATION_TIME_ATTR = new SimpleAttributeDefinition(AVERAGE_REPLICATION_TIME, ModelType.LONG, false, AttributeAccess.Flag.STORAGE_RUNTIME);
    public static final AttributeDefinition REPLICATION_COUNT_ATTR = new SimpleAttributeDefinition(REPLICATION_COUNT, ModelType.LONG, false, AttributeAccess.Flag.STORAGE_RUNTIME);
    public static final AttributeDefinition REPLICATION_FAILURES_ATTR = new SimpleAttributeDefinition(REPLICATION_FAILURES, ModelType.LONG, false, AttributeAccess.Flag.STORAGE_RUNTIME);
    public static final AttributeDefinition SUCCESS_RATIO_ATTR = new SimpleAttributeDefinition(SUCCESS_RATIO, ModelType.STRING, false, AttributeAccess.Flag.STORAGE_RUNTIME);
    public static final AttributeDefinition SUCCESS_RATIO_FLOATING_POINT_ATTR = new SimpleAttributeDefinition(SUCCESS_RATIO_FLOATING_POINT, ModelType.DOUBLE, false, AttributeAccess.Flag.STORAGE_RUNTIME);
    public static final AttributeDefinition STATISTICS_ENABLED_ATTR = new SimpleAttributeDefinition(STATISTICS_ENABLED, ModelType.BOOLEAN, false, AttributeAccess.Flag.STORAGE_RUNTIME);

    private static final AttributeDefinition[] ATTRIBUTES = {
            AVERAGE_REPLICATION_TIME_ATTR, REPLICATION_COUNT_ATTR, REPLICATION_FAILURES_ATTR,
            SUCCESS_RATIO_ATTR, SUCCESS_RATIO_FLOATING_POINT_ATTR, STATISTICS_ENABLED_ATTR
            } ;

    private RpcManagerHandler() {

    }

    public void registerAttributes(final ManagementResourceRegistration registry) {
        for (AttributeDefinition attr : ATTRIBUTES) {
            registry.registerReadOnlyAttribute(attr, this);
        }
    }

    public void registerOperations(final ManagementResourceRegistration registry) {
        final EnumSet<OperationEntry.Flag> readOnly = EnumSet.of(OperationEntry.Flag.READ_ONLY);

        // register all read-only operations supported by this component
        registry.registerOperationHandler(RESET_STATISTICS, this, new DescriptionProvider() {
            public ModelNode getModelDescription(Locale locale) {
              return InfinispanDescriptions.getResetStatisticsDescription(locale);
            }
        }, readOnly);
    }

    @Override
    protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {
        final String operationName = operation.require(OP).asString() ;
        final RpcManagerImpl rpcManager = (RpcManagerImpl) getRpcManager(context, operation) ;

        if (rpcManager == null) {
            ModelNode failureDescription = new ModelNode() ;
            failureDescription.get(DESCRIPTION).set("Cache not started - no RpcManager component available") ;

            throw new OperationFailedException("cache not started", failureDescription) ;
        }

        try {
            // handle any read-attribute operation
            if (READ_ATTRIBUTE_OPERATION.equals(operationName)) {
                handleReadAttribute(context, operation, rpcManager) ;
            // handle reset-statistics
            } else if (RESET_STATISTICS.equals(operationName)) {
                rpcManager.resetStatistics() ;
                context.getResult();
            }
        }
        catch(RuntimeException e) {
           throw e ;
        }
        catch(Exception e) {
           context.getFailureDescription().set(e.getLocalizedMessage()) ;
        }
        context.completeStep();
    }

    private void handleReadAttribute(OperationContext context, ModelNode operation, final RpcManagerImpl rpcManager) {

        final String name = operation.require(ModelDescriptionConstants.NAME).asString() ;

        if (AVERAGE_REPLICATION_TIME_ATTR.getName().equals(name)) {
            long replicationTime = rpcManager.getAverageReplicationTime();
            context.getResult().set(replicationTime);
        } else if (REPLICATION_COUNT_ATTR.getName().equals(name)) {
            long replicationCount = rpcManager.getReplicationCount();
            context.getResult().set(replicationCount);
        } else if (REPLICATION_FAILURES_ATTR.getName().equals(name)) {
            long replicationFailures = rpcManager.getReplicationFailures();
            context.getResult().set(replicationFailures);
        } else if (SUCCESS_RATIO_ATTR.getName().equals(name)) {
            String successRatio = rpcManager.getSuccessRatio();
            context.getResult().set(successRatio);
        } else if (SUCCESS_RATIO_FLOATING_POINT_ATTR.getName().equals(name)) {
            double successRatioFloatingPoint = rpcManager.getSuccessRatioFloatingPoint();
            context.getResult().set(successRatioFloatingPoint);
        } else if (STATISTICS_ENABLED_ATTR.getName().equals(name)) {
            boolean statsEnabled = rpcManager.isStatisticsEnabled();
            context.getResult().set(statsEnabled);
        }
    }

    RpcManager getRpcManager(OperationContext context, ModelNode operation) {
        // get the RpcManager instance from the cache

        // get cache address
        PathAddress rpcManagerAddress = PathAddress.pathAddress(operation.get(OP_ADDR)) ;
        PathAddress cacheAddress = rpcManagerAddress.subAddress(0, rpcManagerAddress.size() - 1) ;
        PathAddress containerAddress = cacheAddress.subAddress(0, cacheAddress.size()-1) ;

        // get cache service name
        String cacheName = cacheAddress.getLastElement().getValue() ;
        String containerName = containerAddress.getLastElement().getValue() ;
        ServiceName cacheServiceName = CacheService.getServiceName(containerName, cacheName) ;
        log.debug("cacheServiceName = " + cacheServiceName.toString());

        // lookup the service and get the advanced cache
        ServiceController<?> cacheService = context.getServiceRegistry(false).getService(cacheServiceName);

        // we can only get the RpcManager instance is the cache has been started
        if (cacheService.getState() == ServiceController.State.UP) {
            Cache cache = Cache.class.cast(cacheService.getValue());
            AdvancedCache advancedCache = cache.getAdvancedCache();
            return advancedCache.getRpcManager();
        }
        return null ;
    }
}
