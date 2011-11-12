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
            if (operation.hasDefined(flatten(element, attribute)))
                model.get(flatten(element, attribute)).set(operation.get(flatten(element, attribute))) ;
        }
    }

    protected static String flatten(String group, String key) {
        return group+"."+key;
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
            FluentConfiguration.LockingConfig fluentLocking = fluent.locking();
            if (cache.hasDefined(flatten(ModelKeys.LOCKING,ModelKeys.ISOLATION))) {
                fluentLocking.isolationLevel(IsolationLevel.valueOf(cache.get(flatten(ModelKeys.LOCKING,ModelKeys.ISOLATION)).asString()));
            }
            if (cache.hasDefined(flatten(ModelKeys.LOCKING,ModelKeys.STRIPING))) {
                fluentLocking.useLockStriping(cache.get(flatten(ModelKeys.LOCKING,ModelKeys.STRIPING)).asBoolean());
            }
            if (cache.hasDefined(flatten(ModelKeys.LOCKING,ModelKeys.ACQUIRE_TIMEOUT))) {
                fluentLocking.lockAcquisitionTimeout(cache.get(flatten(ModelKeys.LOCKING,ModelKeys.ACQUIRE_TIMEOUT)).asLong());
            }
            if (cache.hasDefined(flatten(ModelKeys.LOCKING,ModelKeys.CONCURRENCY_LEVEL))) {
                fluentLocking.concurrencyLevel(cache.get(flatten(ModelKeys.LOCKING,ModelKeys.CONCURRENCY_LEVEL)).asInt());
            }
        }

        // set transaction configuration
        FluentConfiguration.TransactionConfig fluentTx = fluent.transaction();
        TransactionMode txMode = TransactionMode.NON_XA;
        LockingMode lockingMode = LockingMode.OPTIMISTIC;
        if (cache.hasDefined(ModelKeys.TRANSACTION)) {
            if (cache.hasDefined(flatten(ModelKeys.TRANSACTION,ModelKeys.STOP_TIMEOUT))) {
                fluentTx.cacheStopTimeout(cache.get(flatten(ModelKeys.TRANSACTION,ModelKeys.STOP_TIMEOUT)).asInt());
            }
            if (cache.hasDefined(flatten(ModelKeys.TRANSACTION,ModelKeys.MODE))) {
                txMode = TransactionMode.valueOf(cache.get(flatten(ModelKeys.TRANSACTION,ModelKeys.MODE)).asString());
            }
            if (cache.hasDefined(flatten(ModelKeys.TRANSACTION,ModelKeys.LOCKING))) {
                lockingMode = LockingMode.valueOf(cache.get(flatten(ModelKeys.TRANSACTION,ModelKeys.LOCKING)).asString());
            }
            if (cache.hasDefined(flatten(ModelKeys.TRANSACTION,ModelKeys.EAGER_LOCKING))) {
                EagerLocking eager = EagerLocking.valueOf(cache.get(flatten(ModelKeys.TRANSACTION,ModelKeys.EAGER_LOCKING)).asString());
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
            FluentConfiguration.EvictionConfig fluentEviction = fluent.eviction();
            if (cache.hasDefined(flatten(ModelKeys.EVICTION,ModelKeys.STRATEGY))) {
                fluentEviction.strategy(EvictionStrategy.valueOf(cache.get(flatten(ModelKeys.EVICTION,ModelKeys.STRATEGY)).asString()));
            }
            if (cache.hasDefined(flatten(ModelKeys.EVICTION,ModelKeys.MAX_ENTRIES))) {
                fluentEviction.maxEntries(cache.get(flatten(ModelKeys.EVICTION,ModelKeys.MAX_ENTRIES)).asInt());
            }
        }

        // set expiration configuration
        if (cache.hasDefined(ModelKeys.EXPIRATION)) {
            FluentConfiguration.ExpirationConfig fluentExpiration = fluent.expiration();
            if (cache.hasDefined(flatten(ModelKeys.EXPIRATION,ModelKeys.MAX_IDLE))) {
                fluentExpiration.maxIdle(cache.get(flatten(ModelKeys.EXPIRATION,ModelKeys.MAX_IDLE)).asLong());
            }
            if (cache.hasDefined(flatten(ModelKeys.EXPIRATION,ModelKeys.LIFESPAN))) {
                fluentExpiration.lifespan(cache.get(flatten(ModelKeys.EXPIRATION,ModelKeys.LIFESPAN)).asLong());
            }
            if (cache.hasDefined(flatten(ModelKeys.EXPIRATION,ModelKeys.INTERVAL))) {
                fluentExpiration.wakeUpInterval(cache.get(flatten(ModelKeys.EXPIRATION,ModelKeys.INTERVAL)).asLong());
            }
        }

        // set store configuration
        if (cache.hasDefined(ModelKeys.STORE)) {
            FluentConfiguration.LoadersConfig fluentStores = fluent.loaders();
            fluentStores.shared(cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.SHARED)) ? cache.get(flatten(ModelKeys.STORE,ModelKeys.SHARED)).asBoolean() : false);
            fluentStores.preload(cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.PRELOAD)) ? cache.get(flatten(ModelKeys.STORE,ModelKeys.PRELOAD)).asBoolean() : false);
            fluentStores.passivation(cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.PASSIVATION)) ? cache.get(flatten(ModelKeys.STORE,ModelKeys.PASSIVATION)).asBoolean() : true);

            // FIX-ME
            CacheStoreConfig storeConfig = buildCacheStore(cacheName, cache, additionalDeps) ;

            storeConfig.singletonStore().enabled(cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.SINGLETON)) ? cache.get(flatten(ModelKeys.STORE,ModelKeys.SINGLETON)).asBoolean() : false);
            storeConfig.fetchPersistentState(cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.FETCH_STATE)) ? cache.get(flatten(ModelKeys.STORE,ModelKeys.FETCH_STATE)).asBoolean() : true);
            storeConfig.purgeOnStartup(cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.PURGE)) ? cache.get(flatten(ModelKeys.STORE,ModelKeys.PURGE)).asBoolean() : true);
            // properties
            if (cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.PROPERTIES)) && (storeConfig instanceof AbstractCacheStoreConfig)) {
                Properties properties = new Properties();
                for (Property property : cache.get(flatten(ModelKeys.STORE,ModelKeys.PROPERTIES)).asPropertyList()) {
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
    private CacheStoreConfig buildCacheStore(final String name, ModelNode cache, List<AdditionalDependency> additionalDeps) {
        if (cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.CLASS))) {
            String className = cache.get(flatten(ModelKeys.STORE,ModelKeys.CLASS)).asString();
            try {
                CacheStore cacheStore = Class.forName(className).asSubclass(CacheStore.class).newInstance();
                return cacheStore.getConfigurationClass().asSubclass(CacheStoreConfig.class).newInstance();
            } catch (Exception e) {
                throw MESSAGES.invalidCacheStore(e, className);
            }
        }
        // If no class, we assume it's a file cache store
        FileCacheStoreConfig storeConfig = new FileCacheStoreConfig();
        String relativeTo = cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.RELATIVE_TO)) ? cache.get(flatten(ModelKeys.STORE,ModelKeys.RELATIVE_TO)).asString() : ServerEnvironment.SERVER_DATA_DIR;

        // we will add this dependency when the builder is ready
        AdditionalDependency<String> dep = new AdditionalDependency<String>(AbstractPathService.pathNameOf(relativeTo), String.class, storeConfig.getRelativeToInjector()) ;
        additionalDeps.add(dep) ;

        storeConfig.setPath(cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.PATH)) ? cache.get(flatten(ModelKeys.STORE,ModelKeys.PATH)).asString() : name);
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
