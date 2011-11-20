package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.config.Configuration;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.transaction.LockingMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.sql.SQLOutput;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.ArrayList;

import static org.jboss.as.clustering.infinispan.InfinispanLogger.ROOT_LOGGER;

/**
 * Parser which creates a flattened ModelNode representation of cache-container and cache.
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class InfinispanSubsystemParser_1_1 implements XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    /**
     * {@inheritDoc}
     * @see org.jboss.staxmapper.XMLElementReader#readElement(org.jboss.staxmapper.XMLExtendedStreamReader, java.lang.Object)
     */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> operations) throws XMLStreamException {
        ModelNode address = new ModelNode();
        address.add(ModelDescriptionConstants.SUBSYSTEM, InfinispanExtension.SUBSYSTEM_NAME);
        address.protect();

        ModelNode subsystem = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            ParseUtils.requireNoNamespaceAttribute(reader, i);
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case DEFAULT_CACHE_CONTAINER: {
                    subsystem.get(ModelKeys.DEFAULT_CACHE_CONTAINER).set(value);
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }

        if (!subsystem.hasDefined(ModelKeys.DEFAULT_CACHE_CONTAINER)) {
            throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.DEFAULT_CACHE_CONTAINER));
        }

        operations.add(subsystem);

        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case INFINISPAN_1_0: {
                    Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case CACHE_CONTAINER: {
                            // need to add the cache container before adding the caches contained within it
                            List<ModelNode> addCacheOperations = new ArrayList<ModelNode>() ;

                            // parseContainer() now generates separate operations for adding caches
                            operations.add(this.parseContainer(reader, addCacheOperations, address));

                            // now add the caches
                            for (ModelNode addCacheOperation: addCacheOperations) {
                                operations.add(addCacheOperation);
                            }
                            break;
                        }
                        default: {
                            throw ParseUtils.unexpectedElement(reader);
                        }
                    }
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
        }
    }

    private ModelNode parseContainer(XMLExtendedStreamReader reader, List<ModelNode> operations, ModelNode address) throws XMLStreamException {

        ModelNode container = Util.getEmptyOperation(ModelDescriptionConstants.ADD, null);
        container.get(ModelDescriptionConstants.OP_ADDR).set(address);
        String name = null;

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            ParseUtils.requireNoNamespaceAttribute(reader, i);
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME: {
                    name = value;
                    // container.get(ModelKeys.NAME).set(value);
                    break;
                }
                case DEFAULT_CACHE: {
                    container.get(ModelKeys.DEFAULT_CACHE).set(value);
                    break;
                }
                case JNDI_NAME: {
                    container.get(ModelKeys.JNDI_NAME).set(value);
                    break;
                }
                case LISTENER_EXECUTOR: {
                    container.get(ModelKeys.LISTENER_EXECUTOR).set(value);
                    break;
                }
                case EVICTION_EXECUTOR: {
                    container.get(ModelKeys.EVICTION_EXECUTOR).set(value);
                    break;
                }
                case REPLICATION_QUEUE_EXECUTOR: {
                    container.get(ModelKeys.REPLICATION_QUEUE_EXECUTOR).set(value);
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }

        if ((name == null) || !container.hasDefined(ModelKeys.DEFAULT_CACHE)) {
            throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.NAME, Attribute.DEFAULT_CACHE));
        }

        container.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.CACHE_CONTAINER, name);

        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case ALIAS: {
                    container.get(ModelKeys.ALIAS).add(reader.getElementText());
                    break;
                }
                case TRANSPORT: {
                    this.parseTransportAndFlatten(reader, container);
                    break;
                }
                case LOCAL_CACHE: {
                    // create an add operation for /subsystem=infinispan/cache-container=<name>
                    ModelNode localCache = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
                    localCache.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.CACHE_CONTAINER, name);
                    // parse the <local-cache/> element
                    this.parseLocalCache(reader, localCache);
                    // set resource address to /subsystem=infinispan/cache-container=<name>/local-cache=<localCacheName>
                    ModelNode localCacheName = localCache.get(ModelKeys.NAME);
                    localCache.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.LOCAL_CACHE, localCacheName);
                    operations.add(localCache);
                    break;
                }
                case INVALIDATION_CACHE: {
                    // create an add operation for /subsystem=infinispan/cache-container=<name>
                    ModelNode invalidationCache = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
                    invalidationCache.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.CACHE_CONTAINER, name);
                    // parse the <invalidation-cache/> element
                    this.parseInvalidationCache(reader, invalidationCache);
                    // update resource address to /subsystem=infinispan/cache-container=<name>/invalidation-cache=<invalidationCacheName>
                    ModelNode invalidationCacheName = invalidationCache.get(ModelKeys.NAME);
                    invalidationCache.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.INVALIDATION_CACHE, invalidationCacheName);
                    operations.add(invalidationCache);
                    break;
                }
                case REPLICATED_CACHE: {
                    // create an add operation for /subsystem=infinispan/cache-container=<name>
                    ModelNode replicatedCache = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
                    replicatedCache.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.CACHE_CONTAINER, name);
                     // parse the <invalidation-cache/> element
                    this.parseReplicatedCache(reader, replicatedCache);
                    // update resource address to /subsystem=infinispan/cache-container=<name>/replicated-cache=<replicatedCacheName>
                    ModelNode replicatedCacheName = replicatedCache.get(ModelKeys.NAME);
                    replicatedCache.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.REPLICATED_CACHE, replicatedCacheName);
                    operations.add(replicatedCache);
                    break;
                }
                case DISTRIBUTED_CACHE: {
                    // create an add operation for /subsystem=infinispan/cache-container=<name>
                    ModelNode distributedCache = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
                    distributedCache.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.CACHE_CONTAINER, name);
                    // parse the <invalidation-cache/> element
                    this.parseDistributedCache(reader, distributedCache);
                    // update resource address to /subsystem=infinispan/cache-container=<name>/distributed-cache=<distName>
                    ModelNode distributedCacheName = distributedCache.get(ModelKeys.NAME);
                    distributedCache.get(ModelDescriptionConstants.OP_ADDR).add(ModelKeys.DISTRIBUTED_CACHE, distributedCacheName);
                    operations.add(distributedCache);
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
        }

        return container;
    }

    private void parseTransportAndFlatten(XMLExtendedStreamReader reader, ModelNode container) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           container.get(ModelKeys.TRANSPORT).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case STACK: {
                    container.get(flatten(ModelKeys.TRANSPORT, ModelKeys.STACK)).set(value);
                    break;
                }
                case EXECUTOR: {
                    container.get(flatten(ModelKeys.TRANSPORT, ModelKeys.EXECUTOR)).set(value);
                    break;
                }
                case LOCK_TIMEOUT: {
                    container.get(flatten(ModelKeys.TRANSPORT, ModelKeys.LOCK_TIMEOUT)).set(Long.parseLong(value));
                    break;
                }
                case SITE: {
                    container.get(flatten(ModelKeys.TRANSPORT, ModelKeys.SITE)).set(value);
                    break;
                }
                case RACK: {
                    container.get(flatten(ModelKeys.TRANSPORT, ModelKeys.RACK)).set(value);
                    break;
                }
                case MACHINE: {
                    container.get(flatten(ModelKeys.TRANSPORT, ModelKeys.MACHINE)).set(value);
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }
        ParseUtils.requireNoContent(reader);
    }

    private void parseCacheAttribute(XMLExtendedStreamReader reader, int index, Attribute attribute, String value, ModelNode cache) throws XMLStreamException {
        switch (attribute) {
            case NAME: {
                cache.get(ModelKeys.NAME).set(value);
                break;
            }
            case START: {
                try {
                    StartMode mode = StartMode.valueOf(value);
                    cache.get(ModelKeys.START).set(mode.name());
                } catch (IllegalArgumentException e) {
                    throw ParseUtils.invalidAttributeValue(reader, index);
                }
                break;
            }
            case BATCHING: {
                cache.get(ModelKeys.BATCHING).set(Boolean.parseBoolean(value));
                break;
            }
            case INDEXING: {
                try {
                    Indexing indexing = Indexing.valueOf(value);
                    cache.get(ModelKeys.INDEXING).set(indexing.name());
                } catch (IllegalArgumentException e) {
                    throw ParseUtils.invalidAttributeValue(reader, index);
                }
                break;
            }
            default: {
                throw ParseUtils.unexpectedAttribute(reader, index);
            }
        }
    }

    private void parseClusteredCacheAttribute(XMLExtendedStreamReader reader, int index, Attribute attribute, String value, ModelNode cache, Configuration.CacheMode cacheMode) throws XMLStreamException {
        switch (attribute) {
            case MODE: {
                /*
                // move MODE processing into the ADD handlers as it is common to parsing and CLI
                try {
                    Mode mode = Mode.valueOf(value);
                    cache.get(ModelKeys.CACHE_MODE).set(mode.apply(cacheMode).name());
                } catch (IllegalArgumentException e) {
                    throw ParseUtils.invalidAttributeValue(reader, index);
                }
                */
                cache.get(ModelKeys.MODE).set(value);
                break;
            }
            case QUEUE_SIZE: {
                cache.get(ModelKeys.QUEUE_SIZE).set(Integer.parseInt(value));
                break;
            }
            case QUEUE_FLUSH_INTERVAL: {
                cache.get(ModelKeys.QUEUE_FLUSH_INTERVAL).set(Long.parseLong(value));
                break;
            }
            case REMOTE_TIMEOUT: {
                cache.get(ModelKeys.REMOTE_TIMEOUT).set(Long.parseLong(value));
                break;
            }
            default: {
                this.parseCacheAttribute(reader, index, attribute, value, cache);
            }
        }
    }

    private void parseLocalCache(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            this.parseCacheAttribute(reader, i, attribute, value, cache);
        }

        if (!cache.hasDefined(ModelKeys.NAME)) {
            throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.NAME));
        }

        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            this.parseCacheElementAndFlatten(reader, element, cache);
        }
    }

    private void parseDistributedCache(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case OWNERS: {
                    cache.get(ModelKeys.OWNERS).set(Integer.parseInt(value));
                    break;
                }
                case VIRTUAL_NODES: {
                    cache.get(ModelKeys.VIRTUAL_NODES).set(Integer.parseInt(value));
                    break;
                }
                case L1_LIFESPAN: {
                    cache.get(ModelKeys.L1_LIFESPAN).set(Long.parseLong(value));
                    break;
                }
                default: {
                    this.parseClusteredCacheAttribute(reader, i, attribute, value, cache, Configuration.CacheMode.DIST_SYNC);
                }
            }
        }

        if (!cache.hasDefined(ModelKeys.NAME)) {
            throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.NAME));
        }

        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case REHASHING: {
                    this.parseRehashingAndFlatten(reader, cache);
                    break;
                }
                default: {
                    this.parseCacheElementAndFlatten(reader, element, cache);
                }
            }
        }
    }

    private void parseReplicatedCache(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            this.parseClusteredCacheAttribute(reader, i, attribute, value, cache, Configuration.CacheMode.REPL_SYNC);
        }

        if (!cache.hasDefined(ModelKeys.NAME)) {
            throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.NAME));
        }

        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case STATE_TRANSFER: {
                    this.parseStateTransferAndFlatten(reader, cache);
                    break;
                }
                default: {
                    this.parseCacheElementAndFlatten(reader, element, cache);
                }
            }
        }
    }

    private void parseInvalidationCache(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            this.parseClusteredCacheAttribute(reader, i, attribute, value, cache, Configuration.CacheMode.INVALIDATION_SYNC);
        }

        if (!cache.hasDefined(ModelKeys.NAME)) {
            throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.NAME));
        }

        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            this.parseCacheElementAndFlatten(reader, element, cache);
        }
    }

    private void parseCacheElementAndFlatten(XMLExtendedStreamReader reader, Element element, ModelNode cache) throws XMLStreamException {
        switch (element) {
            case LOCKING: {
                this.parseLockingAndFlatten(reader, cache);
                break;
            }
            case TRANSACTION: {
                this.parseTransactionAndFlatten(reader, cache);
                break;
            }
            case EVICTION: {
                this.parseEvictionAndFlatten(reader, cache);
                break;
            }
            case EXPIRATION: {
                this.parseExpirationAndFlatten(reader, cache);
                break;
            }
            case STORE: {
                this.parseCustomStoreAndFlatten(reader, cache);
                break;
            }
            case FILE_STORE: {
                this.parseFileStoreAndFlatten(reader, cache);
                break;
            }
            default: {
                throw ParseUtils.unexpectedElement(reader);
            }
        }
    }

    private void parseRehashingAndFlatten(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           cache.get(ModelKeys.REHASHING).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case ENABLED: {
                    cache.get(flatten(ModelKeys.REHASHING, ModelKeys.ENABLED)).set(Boolean.parseBoolean(value));
                    break;
                }
                case TIMEOUT: {
                    cache.get(flatten(ModelKeys.REHASHING, ModelKeys.TIMEOUT)).set(Long.parseLong(value));
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }
        ParseUtils.requireNoContent(reader);
    }

    private void parseStateTransferAndFlatten(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           cache.get(ModelKeys.STATE_TRANSFER).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case ENABLED: {
                    cache.get(flatten(ModelKeys.STATE_TRANSFER, ModelKeys.ENABLED)).set(Boolean.parseBoolean(value));
                    break;
                }
                case TIMEOUT: {
                    cache.get(flatten(ModelKeys.STATE_TRANSFER, ModelKeys.TIMEOUT)).set(Long.parseLong(value));
                    break;
                }
                case FLUSH_TIMEOUT: {
                    cache.get(flatten(ModelKeys.STATE_TRANSFER, ModelKeys.FLUSH_TIMEOUT)).set(Long.parseLong(value));
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }
        ParseUtils.requireNoContent(reader);
    }

    private void parseLockingAndFlatten(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           cache.get(ModelKeys.LOCKING).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case ISOLATION: {
                    try {
                        IsolationLevel level = IsolationLevel.valueOf(value);
                        cache.get(flatten(ModelKeys.LOCKING, ModelKeys.ISOLATION)).set(level.name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                case STRIPING: {
                    cache.get(flatten(ModelKeys.LOCKING, ModelKeys.STRIPING)).set(Boolean.parseBoolean(value));
                    break;
                }
                case ACQUIRE_TIMEOUT: {
                    cache.get(flatten(ModelKeys.LOCKING, ModelKeys.ACQUIRE_TIMEOUT)).set(Long.parseLong(value));
                    break;
                }
                case CONCURRENCY_LEVEL: {
                    cache.get(flatten(ModelKeys.LOCKING, ModelKeys.CONCURRENCY_LEVEL)).set(Integer.parseInt(value));
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }
        ParseUtils.requireNoContent(reader);
    }

    private void parseTransactionAndFlatten(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           cache.get(ModelKeys.TRANSACTION).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case STOP_TIMEOUT: {
                    cache.get(flatten(ModelKeys.TRANSACTION,ModelKeys.STOP_TIMEOUT)).set(Long.parseLong(value));
                    break;
                }
                case MODE: {
                    try {
                        cache.get(flatten(ModelKeys.TRANSACTION,ModelKeys.MODE)).set(TransactionMode.valueOf(value).name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                case LOCKING: {
                    try {
                        cache.get(flatten(ModelKeys.TRANSACTION,ModelKeys.LOCKING)).set(LockingMode.valueOf(value).name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                case EAGER_LOCKING: {
                    try {
                        cache.get(flatten(ModelKeys.TRANSACTION,ModelKeys.EAGER_LOCKING)).set(EagerLocking.valueOf(value).name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }
        ParseUtils.requireNoContent(reader);
    }

    private void parseEvictionAndFlatten(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           cache.get(ModelKeys.EVICTION).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case STRATEGY: {
                    try {
                        EvictionStrategy strategy = EvictionStrategy.valueOf(value);
                        cache.get(flatten(ModelKeys.EVICTION, ModelKeys.STRATEGY)).set(strategy.name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                case MAX_ENTRIES: {
                    cache.get(flatten(ModelKeys.EVICTION,ModelKeys.MAX_ENTRIES)).set(Integer.parseInt(value));
                    break;
                }
                case INTERVAL: {
                    ROOT_LOGGER.deprecatedAttribute(attribute.getLocalName(), Element.EVICTION.getLocalName(), "ISPN-1268");
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }
        ParseUtils.requireNoContent(reader);
    }

    private void parseExpirationAndFlatten(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           cache.get(ModelKeys.EXPIRATION).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case MAX_IDLE: {
                    cache.get(flatten(ModelKeys.EXPIRATION, ModelKeys.MAX_IDLE)).set(Long.parseLong(value));
                    break;
                }
                case LIFESPAN: {
                    cache.get(flatten(ModelKeys.EXPIRATION, ModelKeys.LIFESPAN)).set(Long.parseLong(value));
                    break;
                }
                case INTERVAL: {
                    cache.get(flatten(ModelKeys.EXPIRATION, ModelKeys.INTERVAL)).set(Long.parseLong(value));
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }
        ParseUtils.requireNoContent(reader);
    }

    private void parseCustomStoreAndFlatten(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           cache.get(ModelKeys.STORE).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case CLASS: {
                    cache.get(flatten(ModelKeys.STORE, ModelKeys.CLASS)).set(value);
                    break;
                }
                default: {
                    this.parseStoreAttributeAndFlatten(reader, i, attribute, value, cache, ModelKeys.STORE);
                }
            }
        }

        if (!cache.hasDefined(flatten(ModelKeys.STORE, ModelKeys.CLASS))) {
            throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.CLASS));
        }

        this.parseStorePropertiesAndFlatten(reader, cache, ModelKeys.STORE);
    }

    private void parseFileStoreAndFlatten(XMLExtendedStreamReader reader, ModelNode cache) throws XMLStreamException {

        // is child element present?
        if (reader.getAttributeCount() > 0) {
           cache.get(ModelKeys.FILE_STORE).set(Boolean.TRUE);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case RELATIVE_TO: {
                    cache.get(flatten(ModelKeys.FILE_STORE,ModelKeys.RELATIVE_TO)).set(value);
                    break;
                }
                case PATH: {
                    cache.get(flatten(ModelKeys.FILE_STORE, ModelKeys.PATH)).set(value);
                    break;
                }
                default: {
                    this.parseStoreAttributeAndFlatten(reader, i, attribute, value, cache, ModelKeys.FILE_STORE);
                }
            }
        }

        this.parseStorePropertiesAndFlatten(reader, cache, ModelKeys.FILE_STORE);
    }

    private void parseStoreAttributeAndFlatten(XMLExtendedStreamReader reader, int index, Attribute attribute, String value, ModelNode cache, String storeKey) throws XMLStreamException {
        switch (attribute) {
            case SHARED: {
                cache.get(flatten(storeKey,ModelKeys.SHARED)).set(Boolean.parseBoolean(value));
                break;
            }
            case PRELOAD: {
                cache.get(flatten(storeKey,ModelKeys.PRELOAD)).set(Boolean.parseBoolean(value));
                break;
            }
            case PASSIVATION: {
                cache.get(flatten(storeKey, ModelKeys.PASSIVATION)).set(Boolean.parseBoolean(value));
                break;
            }
            case FETCH_STATE: {
                cache.get(flatten(storeKey,ModelKeys.FETCH_STATE)).set(Boolean.parseBoolean(value));
                break;
            }
            case PURGE: {
                cache.get(flatten(storeKey, ModelKeys.PURGE)).set(Boolean.parseBoolean(value));
                break;
            }
            case SINGLETON: {
                cache.get(flatten(storeKey, ModelKeys.SINGLETON)).set(Boolean.parseBoolean(value));
                break;
            }
            default: {
                throw ParseUtils.unexpectedAttribute(reader, index);
            }
        }
    }

    /*
     * transform <store ...><property name="X">Y</property></store> into store.properties={"X"="Y",...}
     */
    private void parseStorePropertiesAndFlatten(XMLExtendedStreamReader reader, ModelNode node, String storeKey) throws XMLStreamException {

        // process all <property/> children
        StringBuilder propertyList = new StringBuilder() ;
        boolean storePropertiesExist = false ;
        String quote = "\"" ;

        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case PROPERTY: {
                    int attributes = reader.getAttributeCount();
                    String property = null;
                    for (int i = 0; i < attributes; i++) {
                        String value = reader.getAttributeValue(i);
                        Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                        switch (attribute) {
                            case NAME: {
                                // property name
                                property = value;
                                break;
                            }
                            default: {
                                throw ParseUtils.unexpectedAttribute(reader, i);
                            }
                        }
                    }
                    if (property == null) {
                        throw ParseUtils.missingRequired(reader, Collections.singleton(Attribute.NAME));
                    }
                    // property value
                    String value = reader.getElementText();
                    // mimic what the CLI will prepare for store.properties={"A"="a", "B"="b"}
                    // the add handler processes these into a list
                    propertyList.append(quote + property + quote + "=" + quote + value + quote) ;
                    propertyList.append(",") ;
                    storePropertiesExist = true;
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
        }

        if (storePropertiesExist) {
            // remove last comma if necessary
            if (propertyList.charAt(propertyList.length()-1) == ',') {
               propertyList.deleteCharAt(propertyList.length()-1);
            }
            node.get(flatten(storeKey, ModelKeys.PROPERTIES)).set("{"+propertyList.toString()+"}");
        }
    }

    /*
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
    }
    */

    /**
     * {@inheritDoc}
     * @see org.jboss.staxmapper.XMLElementWriter#writeContent(org.jboss.staxmapper.XMLExtendedStreamWriter, java.lang.Object)
     */
    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(Namespace.CURRENT.getUri(), false);
        ModelNode model = context.getModelNode();

        if (model.isDefined()) {
            writer.writeAttribute(Attribute.DEFAULT_CACHE_CONTAINER.getLocalName(), model.require(ModelKeys.DEFAULT_CACHE_CONTAINER).asString());
            // process cache container
            for (Property entry: model.get(ModelKeys.CACHE_CONTAINER).asPropertyList()) {

                String containerName = entry.getName();
                ModelNode container = entry.getValue();

                writer.writeStartElement(Element.CACHE_CONTAINER.getLocalName());
                writer.writeAttribute(Attribute.NAME.getLocalName(), containerName);
                this.writeRequired(writer, Attribute.DEFAULT_CACHE, container, ModelKeys.DEFAULT_CACHE);
                this.writeOptional(writer, Attribute.JNDI_NAME, container, ModelKeys.JNDI_NAME);
                this.writeOptional(writer, Attribute.LISTENER_EXECUTOR, container, ModelKeys.LISTENER_EXECUTOR);
                this.writeOptional(writer, Attribute.EVICTION_EXECUTOR, container, ModelKeys.EVICTION_EXECUTOR);
                this.writeOptional(writer, Attribute.REPLICATION_QUEUE_EXECUTOR, container, ModelKeys.REPLICATION_QUEUE_EXECUTOR);

                if (container.hasDefined(ModelKeys.ALIAS)) {
                    for (ModelNode alias: container.get(ModelKeys.ALIAS).asList()) {
                        writer.writeStartElement(Element.ALIAS.getLocalName());
                        writer.writeCharacters(alias.asString());
                        writer.writeEndElement();
                    }
                }

                if (container.hasDefined(ModelKeys.TRANSPORT)) {
                    writer.writeStartElement(Element.TRANSPORT.getLocalName());
                    this.writeOptionalFlattened(writer, Attribute.STACK, container, ModelKeys.STACK, ModelKeys.TRANSPORT);
                    this.writeOptionalFlattened(writer, Attribute.EXECUTOR, container, ModelKeys.EXECUTOR, ModelKeys.TRANSPORT);
                    this.writeOptionalFlattened(writer, Attribute.LOCK_TIMEOUT, container, ModelKeys.LOCK_TIMEOUT, ModelKeys.TRANSPORT);
                    this.writeOptionalFlattened(writer, Attribute.SITE, container, ModelKeys.SITE, ModelKeys.TRANSPORT);
                    this.writeOptionalFlattened(writer, Attribute.RACK, container, ModelKeys.RACK, ModelKeys.TRANSPORT);
                    this.writeOptionalFlattened(writer, Attribute.MACHINE, container, ModelKeys.MACHINE, ModelKeys.TRANSPORT);
                    writer.writeEndElement();
                }

                // process local-cache resources
                ModelNode localCacheObject = model.get(ModelKeys.CACHE_CONTAINER, containerName, ModelKeys.LOCAL_CACHE) ;
                if (localCacheObject.isDefined() && localCacheObject.getType() == ModelType.OBJECT) {
                    for (Property cacheEntry : localCacheObject.asPropertyList()) {
                        String cacheName = cacheEntry.getName();
                        ModelNode cache = cacheEntry.getValue();

                        writer.writeStartElement(Element.LOCAL_CACHE.getLocalName());
                        writeCacheAttributesAndElements(writer, cache, cacheName);
                        writer.writeEndElement();
                    }
                }

                // process invalidation-cache resources
                ModelNode invalidationCacheObject = model.get(ModelKeys.CACHE_CONTAINER, containerName, ModelKeys.INVALIDATION_CACHE) ;
                if (invalidationCacheObject.isDefined() && invalidationCacheObject.getType() == ModelType.OBJECT) {
                    for (Property cacheEntry : invalidationCacheObject.asPropertyList()) {
                        String cacheName = cacheEntry.getName();
                        ModelNode cache = cacheEntry.getValue();

                        writer.writeStartElement(Element.INVALIDATION_CACHE.getLocalName());
                        writeClusteredCacheAttributes(writer, cache);
                        writeCacheAttributesAndElements(writer, cache, cacheName);
                        writer.writeEndElement();
                    }
                }

                // process replicated-cache resources
                ModelNode replicatedCacheObject = model.get(ModelKeys.CACHE_CONTAINER, containerName, ModelKeys.REPLICATED_CACHE) ;
                if (replicatedCacheObject.isDefined() && replicatedCacheObject.getType() == ModelType.OBJECT) {
                    for (Property cacheEntry : replicatedCacheObject.asPropertyList()) {
                        String cacheName = cacheEntry.getName();
                        ModelNode cache = cacheEntry.getValue();

                        writer.writeStartElement(Element.REPLICATED_CACHE.getLocalName());

                        writeClusteredCacheAttributes(writer, cache);
                        writeCacheAttributesAndElements(writer, cache, cacheName);

                        // write replicated cache state transfer element
                        if (cache.hasDefined(ModelKeys.STATE_TRANSFER)) {
                            writer.writeStartElement(Element.STATE_TRANSFER.getLocalName());
                            this.writeOptionalFlattened(writer, Attribute.ENABLED, cache, ModelKeys.ENABLED, ModelKeys.STATE_TRANSFER);
                            this.writeOptionalFlattened(writer, Attribute.TIMEOUT, cache, ModelKeys.TIMEOUT, ModelKeys.STATE_TRANSFER);
                            this.writeOptionalFlattened(writer, Attribute.FLUSH_TIMEOUT, cache, ModelKeys.FLUSH_TIMEOUT, ModelKeys.STATE_TRANSFER);
                            writer.writeEndElement();
                        }
                        writer.writeEndElement();
                    }
                }

                    // process distributed-cache resources
                ModelNode distributedCacheObject = model.get(ModelKeys.CACHE_CONTAINER, containerName, ModelKeys.DISTRIBUTED_CACHE);
                if (distributedCacheObject.isDefined() && distributedCacheObject.getType() == ModelType.OBJECT) {
                    for (Property cacheEntry : distributedCacheObject.asPropertyList()) {
                        String cacheName = cacheEntry.getName();
                        ModelNode cache = cacheEntry.getValue();

                        writer.writeStartElement(Element.DISTRIBUTED_CACHE.getLocalName());

                        // write distributed cache attributes
                        this.writeOptional(writer, Attribute.OWNERS, cache, ModelKeys.OWNERS);
                        this.writeOptional(writer, Attribute.VIRTUAL_NODES, cache, ModelKeys.VIRTUAL_NODES);
                        this.writeOptional(writer, Attribute.L1_LIFESPAN, cache, ModelKeys.L1_LIFESPAN);
                        // write clustered cache attributes
                        writeClusteredCacheAttributes(writer, cache);
                        writeCacheAttributesAndElements(writer, cache, cacheName);
                        // write distributed cache rehashing element
                        if (cache.hasDefined(ModelKeys.REHASHING)) {
                            writer.writeStartElement(Element.REHASHING.getLocalName());
                            this.writeOptionalFlattened(writer, Attribute.ENABLED, cache, ModelKeys.ENABLED, ModelKeys.REHASHING);
                            this.writeOptionalFlattened(writer, Attribute.TIMEOUT, cache, ModelKeys.TIMEOUT, ModelKeys.REHASHING);
                            this.writeOptionalFlattened(writer, Attribute.FLUSH_TIMEOUT, cache, ModelKeys.FLUSH_TIMEOUT, ModelKeys.REHASHING);
                            writer.writeEndElement();
                        }
                        writer.writeEndElement();
                    }
                }

                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    private void writeCacheAttributesAndElements(XMLExtendedStreamWriter writer, ModelNode cache, String cacheName) throws XMLStreamException {

        writer.writeAttribute(Attribute.NAME.getLocalName(), cacheName);

        this.writeOptional(writer, Attribute.START, cache, ModelKeys.START);
        this.writeOptional(writer, Attribute.BATCHING, cache, ModelKeys.BATCHING);
        this.writeOptional(writer, Attribute.INDEXING, cache, ModelKeys.INDEXING);

        if (cache.hasDefined(ModelKeys.LOCKING)) {
            writer.writeStartElement(Element.LOCKING.getLocalName());
            this.writeOptionalFlattened(writer, Attribute.ISOLATION, cache, ModelKeys.ISOLATION, ModelKeys.LOCKING);
            this.writeOptionalFlattened(writer, Attribute.STRIPING, cache, ModelKeys.STRIPING, ModelKeys.LOCKING);
            this.writeOptionalFlattened(writer, Attribute.ACQUIRE_TIMEOUT, cache, ModelKeys.ACQUIRE_TIMEOUT, ModelKeys.LOCKING);
            this.writeOptionalFlattened(writer, Attribute.CONCURRENCY_LEVEL, cache, ModelKeys.CONCURRENCY_LEVEL, ModelKeys.LOCKING);
            writer.writeEndElement();
        }

        if (cache.hasDefined(ModelKeys.TRANSACTION)) {
            writer.writeStartElement(Element.TRANSACTION.getLocalName());
           this.writeOptionalFlattened(writer, Attribute.STOP_TIMEOUT, cache, ModelKeys.STOP_TIMEOUT, ModelKeys.TRANSACTION);
            this.writeOptionalFlattened(writer, Attribute.MODE, cache, ModelKeys.MODE, ModelKeys.TRANSACTION);
            this.writeOptionalFlattened(writer, Attribute.LOCKING, cache, ModelKeys.LOCKING, ModelKeys.TRANSACTION);
            this.writeOptionalFlattened(writer, Attribute.EAGER_LOCKING, cache, ModelKeys.EAGER_LOCKING, ModelKeys.TRANSACTION);
            writer.writeEndElement();
        }

        if (cache.hasDefined(ModelKeys.EVICTION)) {
            writer.writeStartElement(Element.EVICTION.getLocalName());
            this.writeOptionalFlattened(writer, Attribute.STRATEGY, cache, ModelKeys.STRATEGY, ModelKeys.EVICTION);
            this.writeOptionalFlattened(writer, Attribute.MAX_ENTRIES, cache, ModelKeys.MAX_ENTRIES, ModelKeys.EVICTION);
            writer.writeEndElement();
        }

        if (cache.hasDefined(ModelKeys.EXPIRATION)) {
            writer.writeStartElement(Element.EXPIRATION.getLocalName());
            this.writeOptionalFlattened(writer, Attribute.MAX_IDLE, cache, ModelKeys.MAX_IDLE, ModelKeys.EXPIRATION);
            this.writeOptionalFlattened(writer, Attribute.LIFESPAN, cache, ModelKeys.LIFESPAN, ModelKeys.EXPIRATION);
            this.writeOptionalFlattened(writer, Attribute.INTERVAL, cache, ModelKeys.INTERVAL, ModelKeys.EXPIRATION);
            writer.writeEndElement();
        }

        // write store element
        if (cache.hasDefined(ModelKeys.STORE)) {
            if (cache.hasDefined(flatten(ModelKeys.STORE, ModelKeys.CLASS))) {
                writer.writeStartElement(Element.STORE.getLocalName());
                this.writeRequiredFlattened(writer, Attribute.CLASS, cache, ModelKeys.CLASS, ModelKeys.STORE);
            }
            this.writeOptionalFlattened(writer, Attribute.SHARED, cache, ModelKeys.SHARED, ModelKeys.STORE);
            this.writeOptionalFlattened(writer, Attribute.PRELOAD, cache, ModelKeys.PRELOAD, ModelKeys.STORE);
            this.writeOptionalFlattened(writer, Attribute.PASSIVATION, cache, ModelKeys.PASSIVATION, ModelKeys.STORE);
            this.writeOptionalFlattened(writer, Attribute.FETCH_STATE, cache, ModelKeys.FETCH_STATE, ModelKeys.STORE);
            this.writeOptionalFlattened(writer, Attribute.PURGE, cache, ModelKeys.PURGE, ModelKeys.STORE);
            this.writeOptionalFlattened(writer, Attribute.SINGLETON, cache, ModelKeys.SINGLETON, ModelKeys.STORE);

            // we now store store properties in the model as before, but add handler works differently
            if (cache.hasDefined(flatten(ModelKeys.STORE,ModelKeys.PROPERTIES))) {

                for (Property property: cache.get(flatten(ModelKeys.STORE,ModelKeys.PROPERTIES)).asPropertyList()) {
                    try {
                    writer.writeStartElement(Element.PROPERTY.getLocalName());
                    writer.writeAttribute(Attribute.NAME.getLocalName(), property.getName());
                    writer.writeCharacters(property.getValue().asString());
                    writer.writeEndElement();
                    }
                    catch (Exception e) {
                        System.out.println("Printing stack trace");
                        e.printStackTrace();
                    }
                }
            }
            writer.writeEndElement();
        }

        // write file-store element
        if (cache.hasDefined(ModelKeys.FILE_STORE)) {
            writer.writeStartElement(Element.FILE_STORE.getLocalName());
            this.writeOptionalFlattened(writer, Attribute.RELATIVE_TO, cache, ModelKeys.RELATIVE_TO, ModelKeys.FILE_STORE);
            this.writeOptionalFlattened(writer, Attribute.PATH, cache, ModelKeys.PATH, ModelKeys.FILE_STORE);
            this.writeOptionalFlattened(writer, Attribute.SHARED, cache, ModelKeys.SHARED, ModelKeys.FILE_STORE);
            this.writeOptionalFlattened(writer, Attribute.PRELOAD, cache, ModelKeys.PRELOAD, ModelKeys.FILE_STORE);
            this.writeOptionalFlattened(writer, Attribute.PASSIVATION, cache, ModelKeys.PASSIVATION, ModelKeys.FILE_STORE);
            this.writeOptionalFlattened(writer, Attribute.FETCH_STATE, cache, ModelKeys.FETCH_STATE, ModelKeys.FILE_STORE);
            this.writeOptionalFlattened(writer, Attribute.PURGE, cache, ModelKeys.PURGE, ModelKeys.FILE_STORE);
            this.writeOptionalFlattened(writer, Attribute.SINGLETON, cache, ModelKeys.SINGLETON, ModelKeys.FILE_STORE);
            // we now store store properties in the model as before, but add handler works differently
            if (cache.hasDefined(flatten(ModelKeys.FILE_STORE, ModelKeys.PROPERTIES))) {
                for (Property property: cache.get(ModelKeys.PROPERTY).asPropertyList()) {
                    writer.writeStartElement(Element.PROPERTY.getLocalName());
                    writer.writeAttribute(Attribute.NAME.getLocalName(), property.getName());
                    writer.writeCharacters(property.getValue().asString());
                    writer.writeEndElement();
                }
            }
           writer.writeEndElement();
        }


    }

    private void writeClusteredCacheAttributes(XMLExtendedStreamWriter writer, ModelNode cache) throws XMLStreamException {

        Configuration.CacheMode mode = Configuration.CacheMode.valueOf(cache.get(ModelKeys.CACHE_MODE).asString());

        writer.writeAttribute(Attribute.MODE.getLocalName(), Mode.forCacheMode(mode).name());
        this.writeOptional(writer, Attribute.QUEUE_SIZE, cache, ModelKeys.QUEUE_SIZE);
        this.writeOptional(writer, Attribute.QUEUE_FLUSH_INTERVAL, cache, ModelKeys.QUEUE_FLUSH_INTERVAL);
        this.writeOptional(writer, Attribute.REMOTE_TIMEOUT, cache, ModelKeys.REMOTE_TIMEOUT);
    }

    private void writeOptional(XMLExtendedStreamWriter writer, Attribute attribute, ModelNode model, String key) throws XMLStreamException {
        if (model.hasDefined(key)) {
            writer.writeAttribute(attribute.getLocalName(), model.get(key).asString());
        }
    }

    private void writeRequired(XMLExtendedStreamWriter writer, Attribute attribute, ModelNode model, String key) throws XMLStreamException {
        writer.writeAttribute(attribute.getLocalName(), model.require(key).asString());
    }

    private void writeOptionalFlattened(XMLExtendedStreamWriter writer, Attribute attribute, ModelNode model, String key, String group) throws XMLStreamException {
        if (model.hasDefined(flatten(group, key))) {
            writer.writeAttribute(attribute.getLocalName(), model.get(flatten(group, key)).asString());
        }
    }

    private void writeRequiredFlattened(XMLExtendedStreamWriter writer, Attribute attribute, ModelNode model, String key, String group) throws XMLStreamException {
        writer.writeAttribute(attribute.getLocalName(), model.require(flatten(group, key)).asString());
    }

    private String flatten(String group, String key) {
        return group + ModelKeys.SEPARATOR + key ;
    }

}
