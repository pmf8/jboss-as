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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.transaction.LockingMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry.EntryType;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import static org.jboss.as.clustering.infinispan.InfinispanLogger.ROOT_LOGGER;

/**
 * @author Paul Ferraro
 */
public class InfinispanExtension implements Extension, DescriptionProvider {

    static final String SUBSYSTEM_NAME = "infinispan";

    private static final InfinispanSubsystemParser_1_0 infinispanSubsystemParser_1_0 = new InfinispanSubsystemParser_1_0();
    private static final InfinispanSubsystemParser_1_1 infinispanSubsystemParser_1_1 = new InfinispanSubsystemParser_1_1();

    private static final PathElement containerPath = PathElement.pathElement(ModelKeys.CACHE_CONTAINER);
    private static final PathElement localCachePath = PathElement.pathElement(ModelKeys.LOCAL_CACHE);
    private static final PathElement invalidationCachePath = PathElement.pathElement(ModelKeys.INVALIDATION_CACHE);
    private static final PathElement replicatedCachePath = PathElement.pathElement(ModelKeys.REPLICATED_CACHE);
    private static final PathElement distributedCachePath = PathElement.pathElement(ModelKeys.DISTRIBUTED_CACHE);

    private static final DescriptionProvider containerDescription = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return LocalDescriptions.getCacheContainerDescription(locale);
        }
    };
    private static final DescriptionProvider localCacheDescription = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return LocalDescriptions.getLocalCacheDescription(locale);
        }
    };
    private static final DescriptionProvider invalidationCacheDescription = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return LocalDescriptions.getInvalidationCacheDescription(locale);
        }
    };
    private static final DescriptionProvider replicatedCacheDescription = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return LocalDescriptions.getReplicatedCacheDescription(locale);
        }
    };
    private static final DescriptionProvider distributedCacheDescription = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return LocalDescriptions.getDistributedCacheDescription(locale);
        }
    };

    /**
     * {@inheritDoc}
     * @see org.jboss.as.controller.Extension#initialize(org.jboss.as.controller.ExtensionContext)
     */
    @Override
    public void initialize(ExtensionContext context) {
        SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME);
        subsystem.registerXMLElementWriter(infinispanSubsystemParser_1_0);

        // add /subsystem=infinispan
        ManagementResourceRegistration registration = subsystem.registerSubsystemModel(this);
        registration.registerOperationHandler(ModelDescriptionConstants.ADD, InfinispanSubsystemAdd.INSTANCE, InfinispanSubsystemAdd.INSTANCE, false);
        registration.registerOperationHandler(ModelDescriptionConstants.DESCRIBE, InfinispanSubsystemDescribe.INSTANCE, InfinispanSubsystemDescribe.INSTANCE, false, EntryType.PRIVATE);

        // add /subsystem=infinispan/cache-container=*
        ManagementResourceRegistration container = registration.registerSubModel(containerPath, containerDescription);
        container.registerOperationHandler(ModelDescriptionConstants.ADD, CacheContainerAdd.INSTANCE, CacheContainerAdd.INSTANCE, false);
        container.registerOperationHandler(ModelDescriptionConstants.REMOVE, CacheContainerRemove.INSTANCE, CacheContainerRemove.INSTANCE, false);

        // add /subsystem=infinispan/cache-container=*/local-cache=*
        ManagementResourceRegistration local = container.registerSubModel(localCachePath, localCacheDescription);
        local.registerOperationHandler(ModelDescriptionConstants.ADD, LocalCacheAdd.INSTANCE, LocalCacheAdd.INSTANCE, false);
        local.registerOperationHandler(ModelDescriptionConstants.REMOVE, CacheRemove.INSTANCE, CacheRemove.INSTANCE, false);

        // add /subsystem=infinispan/cache-container=*/invalidation-cache=*
        ManagementResourceRegistration inv = container.registerSubModel(invalidationCachePath, invalidationCacheDescription);
        inv.registerOperationHandler(ModelDescriptionConstants.ADD, InvalidationCacheAdd.INSTANCE, InvalidationCacheAdd.INSTANCE, false);
        inv.registerOperationHandler(ModelDescriptionConstants.REMOVE, CacheRemove.INSTANCE, CacheRemove.INSTANCE, false);

        // add /subsystem=infinispan/cache-container=*/replicated-cache=*
        ManagementResourceRegistration repl = container.registerSubModel(replicatedCachePath, replicatedCacheDescription);
        repl.registerOperationHandler(ModelDescriptionConstants.ADD, ReplicatedCacheAdd.INSTANCE, ReplicatedCacheAdd.INSTANCE, false);
        repl.registerOperationHandler(ModelDescriptionConstants.REMOVE, CacheRemove.INSTANCE, CacheRemove.INSTANCE, false);

        // add /subsystem=infinispan/cache-container=*/distributed-cache=*
        ManagementResourceRegistration dist = container.registerSubModel(distributedCachePath, distributedCacheDescription);
        dist.registerOperationHandler(ModelDescriptionConstants.ADD, DistributedCacheAdd.INSTANCE, DistributedCacheAdd.INSTANCE, false);
        dist.registerOperationHandler(ModelDescriptionConstants.REMOVE, CacheRemove.INSTANCE, CacheRemove.INSTANCE, false);

    }

    /**
     * {@inheritDoc}
     * @see org.jboss.as.controller.Extension#initializeParsers(org.jboss.as.controller.parsing.ExtensionParsingContext)
     */
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(Namespace.INFINISPAN_1_0.getUri(), infinispanSubsystemParser_1_0);
        context.setSubsystemXmlMapping(Namespace.INFINISPAN_1_1.getUri(), infinispanSubsystemParser_1_1);
    }

    /**
     * {@inheritDoc}
     * @see org.jboss.as.controller.descriptions.DescriptionProvider#getModelDescription(java.util.Locale)
     */
    @Override
    public ModelNode getModelDescription(Locale locale) {
        return LocalDescriptions.getSubsystemDescription(locale);
    }


}
