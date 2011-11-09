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
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

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
                            // parseContainer() now generates separate operations for adding caches
                            operations.add(this.parseContainer(reader, operations, address));
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
                    container.get(ModelKeys.TRANSPORT+"."+ModelKeys.STACK).set(value);
                    break;
                }
                case EXECUTOR: {
                    container.get(ModelKeys.TRANSPORT+"."+ModelKeys.EXECUTOR).set(value);
                    break;
                }
                case LOCK_TIMEOUT: {
                    container.get(ModelKeys.TRANSPORT+"."+ModelKeys.LOCK_TIMEOUT).set(Long.parseLong(value));
                    break;
                }
                case SITE: {
                    container.get(ModelKeys.TRANSPORT+"."+ModelKeys.SITE).set(value);
                    break;
                }
                case RACK: {
                    container.get(ModelKeys.TRANSPORT+"."+ModelKeys.RACK).set(value);
                    break;
                }
                case MACHINE: {
                    container.get(ModelKeys.TRANSPORT+"."+ModelKeys.MACHINE).set(value);
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
                try {
                    Mode mode = Mode.valueOf(value);
                    cache.get(ModelKeys.MODE).set(mode.apply(cacheMode).name());
                } catch (IllegalArgumentException e) {
                    throw ParseUtils.invalidAttributeValue(reader, index);
                }
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
        cache.get(ModelKeys.MODE).set(Configuration.CacheMode.LOCAL.name());
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
                    cache.get(ModelKeys.REHASHING+"."+ModelKeys.ENABLED).set(Boolean.parseBoolean(value));
                    break;
                }
                case TIMEOUT: {
                    cache.get(ModelKeys.REHASHING+"."+ModelKeys.TIMEOUT).set(Long.parseLong(value));
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
                    cache.get(ModelKeys.STATE_TRANSFER+"."+ModelKeys.ENABLED).set(Boolean.parseBoolean(value));
                    break;
                }
                case TIMEOUT: {
                    cache.get(ModelKeys.STATE_TRANSFER+"."+ModelKeys.TIMEOUT).set(Long.parseLong(value));
                    break;
                }
                case FLUSH_TIMEOUT: {
                    cache.get(ModelKeys.STATE_TRANSFER+"."+ModelKeys.FLUSH_TIMEOUT).set(Long.parseLong(value));
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
                        cache.get(ModelKeys.LOCKING+"."+ModelKeys.ISOLATION).set(level.name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                case STRIPING: {
                    cache.get(ModelKeys.LOCKING+"."+ModelKeys.STRIPING).set(Boolean.parseBoolean(value));
                    break;
                }
                case ACQUIRE_TIMEOUT: {
                    cache.get(ModelKeys.LOCKING+"."+ModelKeys.ACQUIRE_TIMEOUT).set(Long.parseLong(value));
                    break;
                }
                case CONCURRENCY_LEVEL: {
                    cache.get(ModelKeys.LOCKING+"."+ModelKeys.CONCURRENCY_LEVEL).set(Integer.parseInt(value));
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
                    cache.get(ModelKeys.TRANSACTION+"."+ModelKeys.STOP_TIMEOUT).set(Long.parseLong(value));
                    break;
                }
                case MODE: {
                    try {
                        cache.get(ModelKeys.TRANSACTION+"."+ModelKeys.MODE).set(TransactionMode.valueOf(value).name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                case LOCKING: {
                    try {
                        cache.get(ModelKeys.TRANSACTION+"."+ModelKeys.LOCKING).set(LockingMode.valueOf(value).name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                case EAGER_LOCKING: {
                    try {
                        cache.get(ModelKeys.TRANSACTION+"."+ModelKeys.EAGER_LOCKING).set(EagerLocking.valueOf(value).name());
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
                        cache.get(ModelKeys.EVICTION+"."+ModelKeys.STRATEGY).set(strategy.name());
                    } catch (IllegalArgumentException e) {
                        throw ParseUtils.invalidAttributeValue(reader, i);
                    }
                    break;
                }
                case MAX_ENTRIES: {
                    cache.get(ModelKeys.EVICTION+"."+ModelKeys.MAX_ENTRIES).set(Integer.parseInt(value));
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
                    cache.get(ModelKeys.EXPIRATION+"."+ModelKeys.MAX_IDLE).set(Long.parseLong(value));
                    break;
                }
                case LIFESPAN: {
                    cache.get(ModelKeys.EXPIRATION+"."+ModelKeys.LIFESPAN).set(Long.parseLong(value));
                    break;
                }
                case INTERVAL: {
                    cache.get(ModelKeys.EXPIRATION+"."+ModelKeys.INTERVAL).set(Long.parseLong(value));
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
                    cache.get(ModelKeys.STORE+"."+ModelKeys.CLASS).set(value);
                    break;
                }
                default: {
                    this.parseStoreAttributeAndFlatten(reader, i, attribute, value, cache, ModelKeys.STORE);
                }
            }
        }

        if (!cache.hasDefined(ModelKeys.STORE+"."+ModelKeys.CLASS)) {
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
                    cache.get(ModelKeys.FILE_STORE+"."+ModelKeys.RELATIVE_TO).set(value);
                    break;
                }
                case PATH: {
                    cache.get(ModelKeys.FILE_STORE+"."+ModelKeys.PATH).set(value);
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
                cache.get(storeKey+"."+ModelKeys.SHARED).set(Boolean.parseBoolean(value));
                break;
            }
            case PRELOAD: {
                cache.get(storeKey+"."+ModelKeys.PRELOAD).set(Boolean.parseBoolean(value));
                break;
            }
            case PASSIVATION: {
                cache.get(storeKey+"."+ModelKeys.PASSIVATION).set(Boolean.parseBoolean(value));
                break;
            }
            case FETCH_STATE: {
                cache.get(storeKey+"."+ModelKeys.FETCH_STATE).set(Boolean.parseBoolean(value));
                break;
            }
            case PURGE: {
                cache.get(storeKey+"."+ModelKeys.PURGE).set(Boolean.parseBoolean(value));
                break;
            }
            case SINGLETON: {
                cache.get(storeKey+"."+ModelKeys.SINGLETON).set(Boolean.parseBoolean(value));
                break;
            }
            default: {
                throw ParseUtils.unexpectedAttribute(reader, index);
            }
        }
    }

    /*
     * transform <property name="X">Y</property> into <storeKey>.property.X=Y
     */
    private void parseStorePropertiesAndFlatten(XMLExtendedStreamReader reader, ModelNode node, String storeKey) throws XMLStreamException {
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
                    node.get(storeKey+"."+ModelKeys.PROPERTY+"."+property).set(value);
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
        }
    }

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
            for (Property entry: model.get(ModelKeys.CACHE_CONTAINER).asPropertyList()) {
                writer.writeStartElement(Element.CACHE_CONTAINER.getLocalName());
                writer.writeAttribute(Attribute.NAME.getLocalName(), entry.getName());
                ModelNode container = entry.getValue();
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
                    ModelNode transport = container.get(ModelKeys.TRANSPORT);
                    this.writeOptional(writer, Attribute.STACK, transport, ModelKeys.STACK);
                    this.writeOptional(writer, Attribute.EXECUTOR, transport, ModelKeys.EXECUTOR);
                    this.writeOptional(writer, Attribute.LOCK_TIMEOUT, transport, ModelKeys.LOCK_TIMEOUT);
                    this.writeOptional(writer, Attribute.SITE, transport, ModelKeys.SITE);
                    this.writeOptional(writer, Attribute.RACK, transport, ModelKeys.RACK);
                    this.writeOptional(writer, Attribute.MACHINE, transport, ModelKeys.MACHINE);
                    writer.writeEndElement();
                }

                for (ModelNode cache: container.get(ModelKeys.CACHE).asList()) {
                    Configuration.CacheMode mode = Configuration.CacheMode.valueOf(cache.get(ModelKeys.MODE).asString());
                    if (mode.isClustered()) {
                        if (mode.isDistributed()) {
                            writer.writeStartElement(Element.DISTRIBUTED_CACHE.getLocalName());
                            this.writeOptional(writer, Attribute.OWNERS, cache, ModelKeys.OWNERS);
                            this.writeOptional(writer, Attribute.VIRTUAL_NODES, cache, ModelKeys.VIRTUAL_NODES);
                            this.writeOptional(writer, Attribute.L1_LIFESPAN, cache, ModelKeys.L1_LIFESPAN);
                        } else if (mode.isInvalidation()) {
                            writer.writeStartElement(Element.INVALIDATION_CACHE.getLocalName());
                        } else {
                            writer.writeStartElement(Element.REPLICATED_CACHE.getLocalName());
                        }
                        writer.writeAttribute(Attribute.MODE.getLocalName(), Mode.forCacheMode(mode).name());
                        this.writeOptional(writer, Attribute.QUEUE_SIZE, cache, ModelKeys.QUEUE_SIZE);
                        this.writeOptional(writer, Attribute.QUEUE_FLUSH_INTERVAL, cache, ModelKeys.QUEUE_FLUSH_INTERVAL);
                        this.writeOptional(writer, Attribute.REMOTE_TIMEOUT, cache, ModelKeys.REMOTE_TIMEOUT);
                    } else {
                        writer.writeStartElement(Element.LOCAL_CACHE.getLocalName());
                    }
                    this.writeRequired(writer, Attribute.NAME, cache, ModelKeys.NAME);
                    this.writeOptional(writer, Attribute.START, cache, ModelKeys.START);
                    this.writeOptional(writer, Attribute.BATCHING, cache, ModelKeys.BATCHING);
                    this.writeOptional(writer, Attribute.INDEXING, cache, ModelKeys.INDEXING);
                    if (cache.hasDefined(ModelKeys.LOCKING)) {
                        writer.writeStartElement(Element.LOCKING.getLocalName());
                        ModelNode locking = cache.get(ModelKeys.LOCKING);
                        this.writeOptional(writer, Attribute.ISOLATION, locking, ModelKeys.ISOLATION);
                        this.writeOptional(writer, Attribute.STRIPING, locking, ModelKeys.STRIPING);
                        this.writeOptional(writer, Attribute.ACQUIRE_TIMEOUT, locking, ModelKeys.ACQUIRE_TIMEOUT);
                        this.writeOptional(writer, Attribute.CONCURRENCY_LEVEL, locking, ModelKeys.CONCURRENCY_LEVEL);
                        writer.writeEndElement();
                    }

                    if (cache.hasDefined(ModelKeys.TRANSACTION)) {
                        writer.writeStartElement(Element.TRANSACTION.getLocalName());
                        ModelNode transaction = cache.get(ModelKeys.TRANSACTION);
                        this.writeOptional(writer, Attribute.STOP_TIMEOUT, transaction, ModelKeys.STOP_TIMEOUT);
                        this.writeOptional(writer, Attribute.MODE, transaction, ModelKeys.MODE);
                        this.writeOptional(writer, Attribute.LOCKING, transaction, ModelKeys.LOCKING);
                        this.writeOptional(writer, Attribute.EAGER_LOCKING, transaction, ModelKeys.EAGER_LOCKING);
                        writer.writeEndElement();
                    }

                    if (cache.hasDefined(ModelKeys.EVICTION)) {
                        writer.writeStartElement(Element.EVICTION.getLocalName());
                        ModelNode eviction = cache.get(ModelKeys.EVICTION);
                        this.writeOptional(writer, Attribute.STRATEGY, eviction, ModelKeys.STRATEGY);
                        this.writeOptional(writer, Attribute.MAX_ENTRIES, eviction, ModelKeys.MAX_ENTRIES);
                        writer.writeEndElement();
                    }

                    if (cache.hasDefined(ModelKeys.EXPIRATION)) {
                        writer.writeStartElement(Element.EXPIRATION.getLocalName());
                        ModelNode expiration = cache.get(ModelKeys.EXPIRATION);
                        this.writeOptional(writer, Attribute.MAX_IDLE, expiration, ModelKeys.MAX_IDLE);
                        this.writeOptional(writer, Attribute.LIFESPAN, expiration, ModelKeys.LIFESPAN);
                        this.writeOptional(writer, Attribute.INTERVAL, expiration, ModelKeys.INTERVAL);
                        writer.writeEndElement();
                    }

                    if (cache.hasDefined(ModelKeys.STORE)) {
                        ModelNode store = cache.get(ModelKeys.STORE);
                        if (store.hasDefined(ModelKeys.CLASS)) {
                            writer.writeStartElement(Element.STORE.getLocalName());
                            this.writeRequired(writer, Attribute.CLASS, store, ModelKeys.CLASS);
                        } else {
                            writer.writeStartElement(Element.FILE_STORE.getLocalName());
                            this.writeOptional(writer, Attribute.RELATIVE_TO, store, ModelKeys.RELATIVE_TO);
                            this.writeOptional(writer, Attribute.PATH, store, ModelKeys.PATH);
                        }
                        this.writeOptional(writer, Attribute.SHARED, store, ModelKeys.SHARED);
                        this.writeOptional(writer, Attribute.PRELOAD, store, ModelKeys.PRELOAD);
                        this.writeOptional(writer, Attribute.PASSIVATION, store, ModelKeys.PASSIVATION);
                        this.writeOptional(writer, Attribute.FETCH_STATE, store, ModelKeys.FETCH_STATE);
                        this.writeOptional(writer, Attribute.PURGE, store, ModelKeys.PURGE);
                        this.writeOptional(writer, Attribute.SINGLETON, store, ModelKeys.SINGLETON);
                        if (store.hasDefined(ModelKeys.PROPERTY)) {
                            for (Property property: store.get(ModelKeys.PROPERTY).asPropertyList()) {
                                writer.writeStartElement(Element.PROPERTY.getLocalName());
                                writer.writeAttribute(Attribute.NAME.getLocalName(), property.getName());
                                writer.writeCharacters(property.getValue().asString());
                                writer.writeEndElement();
                            }
                        }
                        writer.writeEndElement();
                    }

                    if (cache.hasDefined(ModelKeys.STATE_TRANSFER)) {
                        ModelNode stateTransfer = cache.get(ModelKeys.STATE_TRANSFER);
                        writer.writeStartElement(Element.STATE_TRANSFER.getLocalName());
                        this.writeOptional(writer, Attribute.ENABLED, stateTransfer, ModelKeys.ENABLED);
                        this.writeOptional(writer, Attribute.TIMEOUT, stateTransfer, ModelKeys.TIMEOUT);
                        this.writeOptional(writer, Attribute.FLUSH_TIMEOUT, stateTransfer, ModelKeys.FLUSH_TIMEOUT);
                        writer.writeEndElement();
                    }

                    if (cache.hasDefined(ModelKeys.REHASHING)) {
                        ModelNode rehashing = cache.get(ModelKeys.REHASHING);
                        writer.writeStartElement(Element.REHASHING.getLocalName());
                        this.writeOptional(writer, Attribute.ENABLED, rehashing, ModelKeys.ENABLED);
                        this.writeOptional(writer, Attribute.TIMEOUT, rehashing, ModelKeys.TIMEOUT);
                        writer.writeEndElement();
                    }

                    writer.writeEndElement();
                }

                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    private void writeOptional(XMLExtendedStreamWriter writer, Attribute attribute, ModelNode model, String key) throws XMLStreamException {
        if (model.hasDefined(key)) {
            writer.writeAttribute(attribute.getLocalName(), model.get(key).asString());
        }
    }

    private void writeRequired(XMLExtendedStreamWriter writer, Attribute attribute, ModelNode model, String key) throws XMLStreamException {
        writer.writeAttribute(attribute.getLocalName(), model.require(key).asString());
    }

}
