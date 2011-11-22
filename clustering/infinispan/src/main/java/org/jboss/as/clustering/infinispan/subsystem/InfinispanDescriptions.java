/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.clustering.infinispan.subsystem;

import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paul Ferraro
 */
public class InfinispanDescriptions {

    public static final String RESOURCE_NAME = InfinispanDescriptions.class.getPackage().getName() + ".LocalDescriptions";

    private InfinispanDescriptions() {
        // Hide
    }

    // subsystem descriptions
    static ModelNode getSubsystemDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createDescription(resources, "infinispan");
        description.get(ModelDescriptionConstants.HEAD_COMMENT_ALLOWED).set(true);
        description.get(ModelDescriptionConstants.TAIL_COMMENT_ALLOWED).set(true);
        description.get(ModelDescriptionConstants.NAMESPACE).set(Namespace.CURRENT.getUri());

        // information about its child "cache-container"
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.CACHE_CONTAINER, ModelDescriptionConstants.DESCRIPTION).set("A cache container resource");
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.CACHE_CONTAINER, ModelDescriptionConstants.MIN_OCCURS).set(0);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.CACHE_CONTAINER, ModelDescriptionConstants.MAX_OCCURS).set(Integer.MAX_VALUE);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.CACHE_CONTAINER, ModelDescriptionConstants.MODEL_DESCRIPTION);

        return description;
    }

    static ModelNode getSubsystemAddDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createSubsystemOperationDescription(ModelDescriptionConstants.ADD, resources);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.DEFAULT_CACHE_CONTAINER, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.DEFAULT_CACHE_CONTAINER, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.default-container"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.DEFAULT_CACHE_CONTAINER, ModelDescriptionConstants.REQUIRED).set(false);
        return description;
    }

    static ModelNode getSubsystemDescribeDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createSubsystemOperationDescription(ModelDescriptionConstants.DESCRIBE, resources);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES).setEmptyObject();
        description.get(ModelDescriptionConstants.REPLY_PROPERTIES, ModelDescriptionConstants.TYPE).set(ModelType.LIST);
        description.get(ModelDescriptionConstants.REPLY_PROPERTIES, ModelDescriptionConstants.VALUE_TYPE).set(ModelType.OBJECT);
        return description;
    }

    // container descriptions
    static ModelNode getCacheContainerDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createDescription(resources, "infinispan.container");

        // information about its child "local-cache"
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.LOCAL_CACHE, ModelDescriptionConstants.DESCRIPTION).set("A local cache resource");
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.LOCAL_CACHE, ModelDescriptionConstants.MIN_OCCURS).set(0);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.LOCAL_CACHE, ModelDescriptionConstants.MAX_OCCURS).set(Integer.MAX_VALUE);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.LOCAL_CACHE, ModelDescriptionConstants.MODEL_DESCRIPTION);

        // information about its child "invalidation-cache"
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.INVALIDATION_CACHE, ModelDescriptionConstants.DESCRIPTION).set("An invalidation cache resource");
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.INVALIDATION_CACHE, ModelDescriptionConstants.MIN_OCCURS).set(0);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.INVALIDATION_CACHE, ModelDescriptionConstants.MAX_OCCURS).set(Integer.MAX_VALUE);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.INVALIDATION_CACHE, ModelDescriptionConstants.MODEL_DESCRIPTION);

        // information about its child "replicated-cache"
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.REPLICATED_CACHE, ModelDescriptionConstants.DESCRIPTION).set("A replicated cache resource");
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.REPLICATED_CACHE, ModelDescriptionConstants.MIN_OCCURS).set(0);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.REPLICATED_CACHE, ModelDescriptionConstants.MAX_OCCURS).set(Integer.MAX_VALUE);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.REPLICATED_CACHE, ModelDescriptionConstants.MODEL_DESCRIPTION);

        // information about its child "distributed-cache"
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.DISTRIBUTED_CACHE, ModelDescriptionConstants.DESCRIPTION).set("A distributed cache resource");
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.DISTRIBUTED_CACHE, ModelDescriptionConstants.MIN_OCCURS).set(0);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.DISTRIBUTED_CACHE, ModelDescriptionConstants.MAX_OCCURS).set(Integer.MAX_VALUE);
        description.get(ModelDescriptionConstants.CHILDREN, ModelKeys.DISTRIBUTED_CACHE, ModelDescriptionConstants.MODEL_DESCRIPTION);

        return description ;
    }

    static ModelNode getCacheContainerAddDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createCacheContainerOperationDescription(ModelDescriptionConstants.ADD, resources);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.DEFAULT_CACHE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.DEFAULT_CACHE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.default-cache"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LISTENER_EXECUTOR, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LISTENER_EXECUTOR, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.listener-executor"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LISTENER_EXECUTOR, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION_EXECUTOR, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION_EXECUTOR, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.eviction-executor"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION_EXECUTOR, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REPLICATION_QUEUE_EXECUTOR, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REPLICATION_QUEUE_EXECUTOR, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.replication-queue-executor"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REPLICATION_QUEUE_EXECUTOR, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.JNDI_NAME, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.JNDI_NAME, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.jndi-name"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.JNDI_NAME, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.ALIAS, ModelDescriptionConstants.TYPE).set(ModelType.LIST);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.ALIAS, ModelDescriptionConstants.VALUE_TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.ALIAS, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.alias"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.ALIAS, ModelDescriptionConstants.REQUIRED).set(false);

        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.transport"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.STACK, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.STACK, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.transport.stack"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.STACK, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.EXECUTOR, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.EXECUTOR, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.transport.executor"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.EXECUTOR, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.LOCK_TIMEOUT, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.LOCK_TIMEOUT, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.transport.lock-timeout"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.LOCK_TIMEOUT, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.SITE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.SITE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.transport.site"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.SITE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.RACK, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.RACK, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.transport.rack"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.RACK, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.MACHINE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.MACHINE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString("infinispan.container.transport.machine"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSPORT+ModelKeys.SEPARATOR+ModelKeys.MACHINE, ModelDescriptionConstants.REQUIRED).set(false);
        return description;
    }

    static ModelNode getCacheContainerRemoveDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createCacheContainerOperationDescription(ModelDescriptionConstants.REMOVE, resources);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES).setEmptyObject();
        return description;
    }

    // cache descriptions
    static ModelNode getLocalCacheDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createDescription(resources, "infinispan.local-cache");
        // need to add in any parameters!

        return description ;
    }

    static ModelNode getLocalCacheAddDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createLocalCacheOperationDescription(ModelDescriptionConstants.ADD, resources);
        // need to add in the parameters!
        String keyPrefix = "infinispan.cache" ;
        addCacheRequestAttributes(description, resources, keyPrefix) ;

        // these will use resources.getString("infinispan.local-cache.some attribute") to supply descriptions
        // for the REQUEST_PROPERTIES in question
        // also, we can group common cache and clustered cache attributes together
        // we can use the local-cache strings for common cache attributes
        // we can use invalidation-cache strings for common clustered cache attributes
        // or we can define createCacheOperationDescription, createClusteredCacheOperation description and then
        // just add in the ones for replicated and distributed
        return description;
    }

    /*
     * Common cache attributes which qualify as REQUEST_PROPERTIES
     */
    static ModelNode addCacheRequestAttributes(ModelNode description, ResourceBundle resources, String keyPrefix) {

        // name
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.NAME, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.NAME, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".name"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.NAME, ModelDescriptionConstants.REQUIRED).set(true);
        // mode (ASYNC|SYNC)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.MODE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.MODE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".mode"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.MODE, ModelDescriptionConstants.REQUIRED).set(false);
        // start (EAGER|LAZY)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.START, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.START, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".start"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.START, ModelDescriptionConstants.REQUIRED).set(false);
        // batching (true/false)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.BATCHING, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.BATCHING, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".batching"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.BATCHING, ModelDescriptionConstants.REQUIRED).set(false);
        // indexing (NONE|LOCAL|ALL)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.INDEXING, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.INDEXING, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".indexing"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.INDEXING, ModelDescriptionConstants.REQUIRED).set(false);
        // locking (OBJECT)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".locking"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.ISOLATION, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.ISOLATION, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".locking.isolation"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.ISOLATION, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.STRIPING, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.STRIPING, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".locking.striping"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.STRIPING, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.ACQUIRE_TIMEOUT, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.ACQUIRE_TIMEOUT, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".locking.acquire-timeout"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.ACQUIRE_TIMEOUT, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.CONCURRENCY_LEVEL, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.CONCURRENCY_LEVEL, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".locking.concurrency-level"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.LOCKING+ModelKeys.SEPARATOR+ModelKeys.CONCURRENCY_LEVEL, ModelDescriptionConstants.REQUIRED).set(false);
        // transaction (OBJECT)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".transaction"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.STOP_TIMEOUT, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.STOP_TIMEOUT, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".transaction.stop-timeout"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.STOP_TIMEOUT, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.MODE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.MODE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".transaction.mode"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.MODE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.LOCKING, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.LOCKING, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".transaction.locking"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.LOCKING, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.EAGER_LOCKING, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.EAGER_LOCKING, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".transaction.eager-locking"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.TRANSACTION+ModelKeys.SEPARATOR+ModelKeys.EAGER_LOCKING, ModelDescriptionConstants.REQUIRED).set(false);
        // eviction (OBJECT)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".eviction"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.STRATEGY, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.STRATEGY, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".eviction.strategy"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.STRATEGY, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.MAX_ENTRIES, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.MAX_ENTRIES, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".eviction.max-entries"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.MAX_ENTRIES, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.INTERVAL, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.INTERVAL, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".eviction.interval"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EVICTION+ModelKeys.SEPARATOR+ModelKeys.INTERVAL, ModelDescriptionConstants.REQUIRED).set(false);
        // expiration (OBJECT)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".expiration"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.MAX_IDLE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.MAX_IDLE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".expiration.max-idle"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.MAX_IDLE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.LIFESPAN, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.LIFESPAN, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".expiration.lifespan"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.LIFESPAN, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.INTERVAL, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.INTERVAL, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".expiration.interval"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.EXPIRATION+ModelKeys.SEPARATOR+ModelKeys.INTERVAL, ModelDescriptionConstants.REQUIRED).set(false);
        // store (OBJECT)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.CLASS, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.CLASS, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store.class"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.CLASS, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.SHARED, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.SHARED, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store.shared"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.SHARED, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PRELOAD, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PRELOAD, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store.preload"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PRELOAD, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PASSIVATION, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PASSIVATION, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store.passivation"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PASSIVATION, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.FETCH_STATE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.FETCH_STATE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store.fetch-state"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.FETCH_STATE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PURGE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PURGE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store.purge"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PURGE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.SINGLETON, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.SINGLETON, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store.singleton"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.SINGLETON, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PROPERTIES, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PROPERTIES, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".store.properties"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STORE+ModelKeys.SEPARATOR+ModelKeys.PROPERTIES, ModelDescriptionConstants.REQUIRED).set(false);
        // file-store (OBJECT)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.RELATIVE_TO, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.RELATIVE_TO, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.relative-to"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.RELATIVE_TO, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PATH, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PATH, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.path"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PATH, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.SHARED, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.SHARED, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.shared"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.SHARED, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PRELOAD, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PRELOAD, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.preload"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PRELOAD, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PASSIVATION, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PASSIVATION, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.passivation"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PASSIVATION, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.FETCH_STATE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.FETCH_STATE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.fetch-state"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.FETCH_STATE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PURGE, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PURGE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.purge"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PURGE, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.SINGLETON, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.SINGLETON, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.singleton"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.SINGLETON, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PROPERTIES, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PROPERTIES, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".file-store.properties"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.FILE_STORE+ModelKeys.SEPARATOR+ModelKeys.PROPERTIES, ModelDescriptionConstants.REQUIRED).set(false);

        return description ;
    }

    /*
     * Common cache attributes which qualify as REQUEST_PROPERTIES
     */
    static ModelNode addClusteredCacheRequestAttributes(ModelNode description, ResourceBundle resources, String keyPrefix) {

        // queue-size (int)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.QUEUE_SIZE, ModelDescriptionConstants.TYPE).set(ModelType.INT);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.QUEUE_SIZE, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".queue-size"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.QUEUE_SIZE, ModelDescriptionConstants.REQUIRED).set(false);
        // queue-flush-interval (long)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.QUEUE_FLUSH_INTERVAL, ModelDescriptionConstants.TYPE).set(ModelType.LONG);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.QUEUE_FLUSH_INTERVAL, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".queue-flush-interval"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.QUEUE_FLUSH_INTERVAL, ModelDescriptionConstants.REQUIRED).set(false);
        // remote-timeout (long)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REMOTE_TIMEOUT, ModelDescriptionConstants.TYPE).set(ModelType.LONG);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REMOTE_TIMEOUT, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".remote-timeout"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REMOTE_TIMEOUT, ModelDescriptionConstants.REQUIRED).set(false);

        return description ;
    }

    static ModelNode getInvalidationCacheDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        return createDescription(resources, "infinispan.invalidation-cache");
    }

    static ModelNode getInvalidationCacheAddDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createInvalidationCacheOperationDescription(ModelDescriptionConstants.ADD, resources);
        // need to add in the parameters!
        String keyPrefix = "infinispan.cache" ;
        addCacheRequestAttributes(description, resources, keyPrefix) ;
        addClusteredCacheRequestAttributes(description, resources, keyPrefix) ;

        return description;
    }
    static ModelNode getReplicatedCacheDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createDescription(resources, "infinispan.replicated-cache");

        // information about its child "component=rpc-manager"
        description.get(ModelDescriptionConstants.CHILDREN, "component", ModelDescriptionConstants.DESCRIPTION).set("A cache component");
        description.get(ModelDescriptionConstants.CHILDREN, "component", ModelDescriptionConstants.MIN_OCCURS).set(1);
        description.get(ModelDescriptionConstants.CHILDREN, "component", ModelDescriptionConstants.MAX_OCCURS).set(1);
        description.get(ModelDescriptionConstants.CHILDREN, "component", ModelDescriptionConstants.MODEL_DESCRIPTION);
        return description ;
    }

    static ModelNode getReplicatedCacheAddDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createReplicatedCacheOperationDescription(ModelDescriptionConstants.ADD, resources);
        // need to add in the parameters!

        String keyPrefix = "infinispan.cache" ;
        addCacheRequestAttributes(description, resources, keyPrefix) ;
        addClusteredCacheRequestAttributes(description, resources, keyPrefix) ;

        // add in the replicated parameters
        // state-transfer (OBJECT)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".state-transfer"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.ENABLED, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.ENABLED, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".state-transfer.enabled"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.ENABLED, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.TIMEOUT, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.TIMEOUT, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".state-transfer.timeout"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.TIMEOUT, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.FLUSH_TIMEOUT, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.FLUSH_TIMEOUT, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".state-transfer.flush-timeout"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.STATE_TRANSFER+ModelKeys.SEPARATOR+ModelKeys.FLUSH_TIMEOUT, ModelDescriptionConstants.REQUIRED).set(false);


        return description;
    }
    static ModelNode getDistributedCacheDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createDescription(resources, "infinispan.distributed-cache");

        // information about its child "component=rpc-manager"
        description.get(ModelDescriptionConstants.CHILDREN, "component", ModelDescriptionConstants.DESCRIPTION).set("A cache component");
        description.get(ModelDescriptionConstants.CHILDREN, "component", ModelDescriptionConstants.MIN_OCCURS).set(1);
        description.get(ModelDescriptionConstants.CHILDREN, "component", ModelDescriptionConstants.MAX_OCCURS).set(1);
        description.get(ModelDescriptionConstants.CHILDREN, "component", ModelDescriptionConstants.MODEL_DESCRIPTION);
        return description;
    }

    static ModelNode getDistributedCacheAddDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createDistributedCacheOperationDescription(ModelDescriptionConstants.ADD, resources);

        // need to add in the parameters!
        String keyPrefix = "infinispan.cache" ;
        addCacheRequestAttributes(description, resources, keyPrefix) ;
        addClusteredCacheRequestAttributes(description, resources, keyPrefix) ;

        // add in the distributed parameters

        // owners (int)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.OWNERS, ModelDescriptionConstants.TYPE).set(ModelType.INT);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.OWNERS, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".owners"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.OWNERS, ModelDescriptionConstants.REQUIRED).set(false);
        // virtual-nodes (int)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.VIRTUAL_NODES, ModelDescriptionConstants.TYPE).set(ModelType.INT);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.VIRTUAL_NODES, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".virtual-nodes"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.VIRTUAL_NODES, ModelDescriptionConstants.REQUIRED).set(false);
        // l1-lifespan (long)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.L1_LIFESPAN, ModelDescriptionConstants.TYPE).set(ModelType.INT);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.L1_LIFESPAN, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".l1-lifespan"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.L1_LIFESPAN, ModelDescriptionConstants.REQUIRED).set(false);
        // rehashing (OBJECT)
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING, ModelDescriptionConstants.TYPE).set(ModelType.BOOLEAN);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".rehashing"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING+ModelKeys.SEPARATOR+ModelKeys.ENABLED, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING+ModelKeys.SEPARATOR+ModelKeys.ENABLED, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".rehashing.enabled"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING+ModelKeys.SEPARATOR+ModelKeys.ENABLED, ModelDescriptionConstants.REQUIRED).set(false);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING+ModelKeys.SEPARATOR+ModelKeys.TIMEOUT, ModelDescriptionConstants.TYPE).set(ModelType.STRING);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING+ModelKeys.SEPARATOR+ModelKeys.TIMEOUT, ModelDescriptionConstants.DESCRIPTION).set(resources.getString(keyPrefix + ".rehashing.timeout"));
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES, ModelKeys.REHASHING+ModelKeys.SEPARATOR+ModelKeys.TIMEOUT, ModelDescriptionConstants.REQUIRED).set(false);

        return description;
    }

    // TODO update me
    static ModelNode getCacheRemoveDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createCacheContainerOperationDescription(ModelDescriptionConstants.REMOVE, resources);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES).setEmptyObject();
        return description;
    }

    static ModelNode getRpcManagerComponentDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode desc = createDescription(resources, "infinispan.component.rpc-manager");
        return desc;
    }

    static ModelNode getResetStatisticsDescription(Locale locale) {
        ResourceBundle resources = getResources(locale);
        ModelNode description = createRpcManagerComponentOperationDescription(RpcManagerHandler.RESET_STATISTICS, resources);
        description.get(ModelDescriptionConstants.REQUEST_PROPERTIES).setEmptyObject();
        return description;
    }


    private static ResourceBundle getResources(Locale locale) {
        return ResourceBundle.getBundle(RESOURCE_NAME, (locale == null) ? Locale.getDefault() : locale);
    }

    private static ModelNode createDescription(ResourceBundle resources, String key) {
        return createOperationDescription(null, resources, key);
    }

    private static ModelNode createOperationDescription(String operation, ResourceBundle resources, String key) {
        ModelNode description = new ModelNode();
        if (operation != null) {
            description.get(ModelDescriptionConstants.OPERATION_NAME).set(operation);
        }
        description.get(ModelDescriptionConstants.DESCRIPTION).set(resources.getString(key));
        return description;
    }

    private static ModelNode createSubsystemOperationDescription(String operation, ResourceBundle resources) {
        return createOperationDescription(operation, resources, "infinispan." + operation);
    }

    private static ModelNode createCacheContainerOperationDescription(String operation, ResourceBundle resources) {
        return createOperationDescription(operation, resources, "infinispan.container." + operation);
    }

    private static ModelNode createLocalCacheOperationDescription(String operation, ResourceBundle resources) {
        return createOperationDescription(operation, resources, "infinispan.local-cache." + operation);
    }

    private static ModelNode createInvalidationCacheOperationDescription(String operation, ResourceBundle resources) {
        return createOperationDescription(operation, resources, "infinispan.invalidation-cache." + operation);
    }

    private static ModelNode createReplicatedCacheOperationDescription(String operation, ResourceBundle resources) {
        return createOperationDescription(operation, resources, "infinispan.replicated-cache." + operation);
    }

    private static ModelNode createDistributedCacheOperationDescription(String operation, ResourceBundle resources) {
        return createOperationDescription(operation, resources, "infinispan.distributed-cache." + operation);
    }

    private static ModelNode createRpcManagerComponentOperationDescription(String operation, ResourceBundle resources) {
        return createOperationDescription(operation, resources, "infinispan.component.rpc-manager." + operation);
    }
}
