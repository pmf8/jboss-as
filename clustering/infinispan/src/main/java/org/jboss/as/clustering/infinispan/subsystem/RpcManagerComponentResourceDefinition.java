package org.jboss.as.clustering.infinispan.subsystem;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class RpcManagerComponentResourceDefinition extends SimpleResourceDefinition {

    public static final String RPC_MANAGER = "rpc-manager" ;

    // StandardResourceDescriptionResolver(keyPrefix, bundleBaseName, bundleLoader, reuseAttributesForAdd, useUnprefixedChildTypes)
    // resource descriptions are relative to "rpc-manager"
    // need to be in specific format for attributes, operations and the like, with keyPrefix "infinispan.container.rpc-manager"
    public static ResourceDescriptionResolver getOurResolver() {
       return new StandardResourceDescriptionResolver(RPC_MANAGER, InfinispanExtension.RESOURCE_NAME, InfinispanExtension.class.getClassLoader(), true, true);
    }

    private static final PathElement RPC_MANAGER_PATH = PathElement.pathElement("component", RPC_MANAGER);
    public static final RpcManagerComponentResourceDefinition INSTANCE = new RpcManagerComponentResourceDefinition() ;

    // SimpleResourceDefinition(PathElement, ResourceDescriptionResolver)
    private RpcManagerComponentResourceDefinition() {
        // super(RPC_MANAGER_PATH, InfinispanExtension.getResourceDescriptionResolver(RPC_MANAGER), null, null) ;
        super(RPC_MANAGER_PATH, getOurResolver()) ;
    }

    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        RpcManagerHandler.INSTANCE.registerOperations(resourceRegistration);
    }

    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        RpcManagerHandler.INSTANCE.registerAttributes(resourceRegistration);
    }

    // TODO - check this out
    /*
    public DescriptionProvider getDescriptionProvider(ImmutableManagementResourceRegistration resourceRegistration) {
        return new DefaultResourceDescriptionProvider(resourceRegistration, getResourceDescriptionResolver());
    }
    */
}
