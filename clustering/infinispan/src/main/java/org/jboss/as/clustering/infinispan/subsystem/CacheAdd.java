package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.config.Configuration;
import org.infinispan.config.FluentConfiguration;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.loaders.AbstractCacheStoreConfig;
import org.infinispan.loaders.CacheStore;
import org.infinispan.loaders.CacheStoreConfig;
import org.infinispan.transaction.LockingMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.services.path.AbstractPathService;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.jboss.as.clustering.infinispan.InfinispanMessages.MESSAGES;

/**
 * Base class for cache add handlers
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class CacheAdd extends AbstractAddStepHandler {

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
    void populateCacheModelNode(ModelNode operation, ModelNode model) throws OperationFailedException {

        // populate attributes
        if (operation.hasDefined(ModelKeys.START)) {
            model.get(ModelKeys.START).set(operation.get(ModelKeys.START)) ;
        }
        if (operation.hasDefined(ModelKeys.BATCHING)) {
            model.get(ModelKeys.BATCHING).set(operation.get(ModelKeys.BATCHING)) ;
        }
        if (operation.hasDefined(ModelKeys.INDEXING)) {
            model.get(ModelKeys.INDEXING).set(operation.get(ModelKeys.INDEXING)) ;
        }

        // populate elements
        if (operation.hasDefined(ModelKeys.LOCKING)) {
            // indicate that a locking child is defined
            model.get(ModelKeys.LOCKING).set(operation.get(ModelKeys.LOCKING));
            List<String> attributeList = Arrays.asList(ModelKeys.ISOLATION, ModelKeys.STRIPING, ModelKeys.ACQUIRE_TIMEOUT, ModelKeys.CONCURRENCY_LEVEL);
            copyFlattenedElementsToModel(operation, model, ModelKeys.LOCKING, attributeList);
        }
        if (operation.hasDefined(ModelKeys.TRANSACTION)) {
            // indicate that a transaction child is defined
            model.get(ModelKeys.TRANSACTION).set(operation.get(ModelKeys.TRANSACTION));
            List<String> attributeList = Arrays.asList(ModelKeys.STOP_TIMEOUT, ModelKeys.MODE, ModelKeys.LOCKING, ModelKeys.EAGER_LOCKING);
            copyFlattenedElementsToModel(operation, model, ModelKeys.TRANSACTION, attributeList);
        }
        if (operation.hasDefined(ModelKeys.EVICTION)) {
            // indicate that a eviction child is defined
            model.get(ModelKeys.EVICTION).set(operation.get(ModelKeys.EVICTION));
            List<String> attributeList = Arrays.asList(ModelKeys.STRATEGY, ModelKeys.MAX_ENTRIES, ModelKeys.INTERVAL);
            copyFlattenedElementsToModel(operation, model, ModelKeys.EVICTION, attributeList);
          }
        if (operation.hasDefined(ModelKeys.EXPIRATION)) {
            // indicate that a expiration child is defined
            model.get(ModelKeys.EXPIRATION).set(operation.get(ModelKeys.EXPIRATION));
            List<String> attributeList = Arrays.asList(ModelKeys.MAX_IDLE, ModelKeys.LIFESPAN, ModelKeys.INTERVAL);
            copyFlattenedElementsToModel(operation, model, ModelKeys.EXPIRATION, attributeList);
        }
        if (operation.hasDefined(ModelKeys.STORE)) {
            // indicate that a store child is defined
            model.get(ModelKeys.STORE).set(operation.get(ModelKeys.STORE));
            List<String> attributeList = Arrays.asList(ModelKeys.CLASS, ModelKeys.SHARED, ModelKeys.PRELOAD, ModelKeys.PASSIVATION, ModelKeys.FETCH_STATE, ModelKeys.PURGE, ModelKeys.SINGLETON);
            copyFlattenedElementsToModel(operation, model, ModelKeys.STORE, attributeList);
            copyFlattenedStorePropertiesToModel(operation, model, ModelKeys.STORE+"."+ModelKeys.PROPERTIES);
        }
        if (operation.hasDefined(ModelKeys.FILE_STORE)) {
            // indicate that a filestore child is defined
            model.get(ModelKeys.FILE_STORE).set(operation.get(ModelKeys.FILE_STORE));
            List<String> attributeList = Arrays.asList(ModelKeys.RELATIVE_TO, ModelKeys.PATH, ModelKeys.SHARED, ModelKeys.PRELOAD, ModelKeys.PASSIVATION, ModelKeys.FETCH_STATE, ModelKeys.PURGE, ModelKeys.SINGLETON);
            copyFlattenedElementsToModel(operation, model, ModelKeys.FILE_STORE, attributeList);
            copyFlattenedStorePropertiesToModel(operation, model, ModelKeys.FILE_STORE+"."+ModelKeys.PROPERTIES);
        }
    }

    protected void copyFlattenedElementsToModel(ModelNode operation, ModelNode model, String element, List<String> attributes) {
        for (String attribute : attributes) {
            if (operation.hasDefined(element+"."+attribute))
                model.get(element+"."+attribute).set(operation.get(element+"."+attribute)) ;
        }
    }

    // these properties come in the form {"\""name"\""="\""value"\"", ...}}
    protected void copyFlattenedStorePropertiesToModel(ModelNode operation, ModelNode model, String element) {

        if (operation.has(element)) {
            String propertyList = operation.get(element).asString();
            // remove braces
            propertyList = propertyList.substring(1, propertyList.length() - 2);
            // tokenize
            String[] tokens = propertyList.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];
                // remove embedded double quotes
                token = token.replace('\"',' ') ;
                String[] keyValuePair = token.split("=");
                // now store in the model as a property
                model.get(element).add(keyValuePair[0].trim(), keyValuePair[1].trim());
            }
        }
    }


    /**
     * Create a Configuration object initialized from the data in the operation.
     *
     * @param cache data representing cache configuration
     * @param configuration Configuration object to add data to
     * @return initialised Configuration object
     */
    Configuration processCacheModelNode(ModelNode cache, Configuration configuration, List<AdditionalDependency> additionalDeps) {

        String cacheName = cache.require(ModelKeys.NAME).asString();

        configuration.setClassLoader(this.getClass().getClassLoader());
        FluentConfiguration fluent = configuration.fluent();

        // set cache mode
        Configuration.CacheMode mode = Configuration.CacheMode.valueOf(cache.require(ModelKeys.MODE).asString());
        fluent.mode(mode);

        // set batching batching configuration
        if (cache.hasDefined(ModelKeys.BATCHING)) {
            if (cache.get(ModelKeys.BATCHING).asBoolean()) {
                fluent.invocationBatching();
            }
        }
        // set indexing configuration
        if (cache.hasDefined(ModelKeys.INDEXING)) {
            Indexing indexing = Indexing.valueOf(cache.get(ModelKeys.INDEXING).asString());
            if (indexing.isEnabled()) {
                fluent.indexing().indexLocalOnly(indexing.isLocalOnly());
            }
        }

        // set locking configuration
        if (cache.hasDefined(ModelKeys.LOCKING)) {
            ModelNode locking = cache.get(ModelKeys.LOCKING);
            FluentConfiguration.LockingConfig fluentLocking = fluent.locking();
            if (locking.hasDefined(ModelKeys.ISOLATION)) {
                fluentLocking.isolationLevel(IsolationLevel.valueOf(locking.get(ModelKeys.ISOLATION).asString()));
            }
            if (locking.hasDefined(ModelKeys.STRIPING)) {
                fluentLocking.useLockStriping(locking.get(ModelKeys.STRIPING).asBoolean());
            }
            if (locking.hasDefined(ModelKeys.ACQUIRE_TIMEOUT)) {
                fluentLocking.lockAcquisitionTimeout(locking.get(ModelKeys.ACQUIRE_TIMEOUT).asLong());
            }
            if (locking.hasDefined(ModelKeys.CONCURRENCY_LEVEL)) {
                fluentLocking.concurrencyLevel(locking.get(ModelKeys.CONCURRENCY_LEVEL).asInt());
            }
        }

        // set transaction configuration
        FluentConfiguration.TransactionConfig fluentTx = fluent.transaction();
        TransactionMode txMode = TransactionMode.NON_XA;
        LockingMode lockingMode = LockingMode.OPTIMISTIC;
        if (cache.hasDefined(ModelKeys.TRANSACTION)) {
            ModelNode transaction = cache.get(ModelKeys.TRANSACTION);
            if (transaction.hasDefined(ModelKeys.STOP_TIMEOUT)) {
                fluentTx.cacheStopTimeout(transaction.get(ModelKeys.STOP_TIMEOUT).asInt());
            }
            if (transaction.hasDefined(ModelKeys.MODE)) {
                txMode = TransactionMode.valueOf(transaction.get(ModelKeys.MODE).asString());
            }
            if (transaction.hasDefined(ModelKeys.LOCKING)) {
                lockingMode = LockingMode.valueOf(transaction.get(ModelKeys.LOCKING).asString());
            }
            if (transaction.hasDefined(ModelKeys.EAGER_LOCKING)) {
                EagerLocking eager = EagerLocking.valueOf(transaction.get(ModelKeys.EAGER_LOCKING).asString());
                fluentTx.lockingMode(eager.isEnabled() ? LockingMode.PESSIMISTIC : LockingMode.OPTIMISTIC).eagerLockSingleNode(eager.isSingleOwner());
            }
        }
        fluentTx.transactionMode(txMode.getMode());
        fluentTx.lockingMode(lockingMode);
        FluentConfiguration.RecoveryConfig recovery = fluentTx.useSynchronization(!txMode.isXAEnabled()).recovery();
        if (txMode.isRecoveryEnabled()) {
            recovery.syncCommitPhase(true).syncRollbackPhase(true);
        } else {
            recovery.disable();
        }

        // set eviction configuration
        if (cache.hasDefined(ModelKeys.EVICTION)) {
            ModelNode eviction = cache.get(ModelKeys.EVICTION);
            FluentConfiguration.EvictionConfig fluentEviction = fluent.eviction();
            if (eviction.hasDefined(ModelKeys.STRATEGY)) {
                fluentEviction.strategy(EvictionStrategy.valueOf(eviction.get(ModelKeys.STRATEGY).asString()));
            }
            if (eviction.hasDefined(ModelKeys.MAX_ENTRIES)) {
                fluentEviction.maxEntries(eviction.get(ModelKeys.MAX_ENTRIES).asInt());
            }
        }

        // set expiration configuration
        if (cache.hasDefined(ModelKeys.EXPIRATION)) {
            ModelNode expiration = cache.get(ModelKeys.EXPIRATION);
            FluentConfiguration.ExpirationConfig fluentExpiration = fluent.expiration();
            if (expiration.hasDefined(ModelKeys.MAX_IDLE)) {
                fluentExpiration.maxIdle(expiration.get(ModelKeys.MAX_IDLE).asLong());
            }
            if (expiration.hasDefined(ModelKeys.LIFESPAN)) {
                fluentExpiration.lifespan(expiration.get(ModelKeys.LIFESPAN).asLong());
            }
            if (expiration.hasDefined(ModelKeys.INTERVAL)) {
                fluentExpiration.wakeUpInterval(expiration.get(ModelKeys.INTERVAL).asLong());
            }
        }

        // set store configuration
        if (cache.hasDefined(ModelKeys.STORE)) {
            ModelNode store = cache.get(ModelKeys.STORE);
            FluentConfiguration.LoadersConfig fluentStores = fluent.loaders();
            fluentStores.shared(store.hasDefined(ModelKeys.SHARED) ? store.get(ModelKeys.SHARED).asBoolean() : false);
            fluentStores.preload(store.hasDefined(ModelKeys.PRELOAD) ? store.get(ModelKeys.PRELOAD).asBoolean() : false);
            fluentStores.passivation(store.hasDefined(ModelKeys.PASSIVATION) ? store.get(ModelKeys.PASSIVATION).asBoolean() : true);

            // FIX-ME
            CacheStoreConfig storeConfig = buildCacheStore(cacheName, store, additionalDeps) ;

            storeConfig.singletonStore().enabled(store.hasDefined(ModelKeys.SINGLETON) ? store.get(ModelKeys.SINGLETON).asBoolean() : false);
            storeConfig.fetchPersistentState(store.hasDefined(ModelKeys.FETCH_STATE) ? store.get(ModelKeys.FETCH_STATE).asBoolean() : true);
            storeConfig.purgeOnStartup(store.hasDefined(ModelKeys.PURGE) ? store.get(ModelKeys.PURGE).asBoolean() : true);
            if (store.hasDefined(ModelKeys.PROPERTY) && (storeConfig instanceof AbstractCacheStoreConfig)) {
                Properties properties = new Properties();
                for (Property property : store.get(ModelKeys.PROPERTY).asPropertyList()) {
                    properties.setProperty(property.getName(), property.getValue().asString());
                }
                ((AbstractCacheStoreConfig) storeConfig).setProperties(properties);
            }
            fluentStores.addCacheLoader(storeConfig);
        }

      return configuration ;
    }

    /*
     * Problem here with dependency setting
     */
    private CacheStoreConfig buildCacheStore(final String name, ModelNode store, List<AdditionalDependency> additionalDeps) {
        if (store.hasDefined(ModelKeys.CLASS)) {
            String className = store.get(ModelKeys.CLASS).asString();
            try {
                CacheStore cacheStore = Class.forName(className).asSubclass(CacheStore.class).newInstance();
                return cacheStore.getConfigurationClass().asSubclass(CacheStoreConfig.class).newInstance();
            } catch (Exception e) {
                throw MESSAGES.invalidCacheStore(e, className);
            }
        }
        // If no class, we assume it's a file cache store
        FileCacheStoreConfig storeConfig = new FileCacheStoreConfig();
        String relativeTo = store.hasDefined(ModelKeys.RELATIVE_TO) ? store.get(ModelKeys.RELATIVE_TO).asString() : ServerEnvironment.SERVER_DATA_DIR;

        // we will add this dependency when the builder is ready
        AdditionalDependency<String> dep = new AdditionalDependency<String>(AbstractPathService.pathNameOf(relativeTo), String.class, storeConfig.getRelativeToInjector()) ;
        additionalDeps.add(dep) ;

        storeConfig.setPath(store.hasDefined(ModelKeys.PATH) ? store.get(ModelKeys.PATH).asString() : name);
        return storeConfig;
    }


    /*
     * Allows us to store dependency requirements for later processing.
     */
    protected class AdditionalDependency<I> {
        private final ServiceName name ;
        private final Class<I> type ;
        private final Injector<I> target ;

        AdditionalDependency(ServiceName name, Class<I> type, Injector<I> target) {
            this.name = name ;
            this.type = type ;
            this.target = target ;
        }

        ServiceName getName() {
            return name ;
        }
        public Class<I> getType() {
            return type;
        }

        public Injector<I> getTarget() {
            return target;
        }
    }

}
