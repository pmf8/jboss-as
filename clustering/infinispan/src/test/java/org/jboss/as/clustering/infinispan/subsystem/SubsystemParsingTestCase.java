package org.jboss.as.clustering.infinispan.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

/**
 *  Subsystem parsing test case
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class SubsystemParsingTestCase extends AbstractSubsystemTest {

    static final String SUBSYSTEM_XML_FILE = "subsystem-infinispan.xml" ;

    public SubsystemParsingTestCase() {
        super(InfinispanExtension.SUBSYSTEM_NAME, new InfinispanExtension());
    }
    /**
     * Tests that the xml is parsed into the correct operations
     */
    @Test
    public void testParseSubsystem() throws Exception {

        //Parse the subsystem xml into operations
        String subsystemXml = getSubsystemXml() ;
        List<ModelNode> operations = super.parse(subsystemXml);

        // Check that we have the expected number of operations
        // one operation for adding subsystem; one operation for adding deployment-type
        Assert.assertEquals(8, operations.size());

        //Check that each operation has the correct content

    }

    private String getSubsystemXml() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(SUBSYSTEM_XML_FILE);
        if (url == null) {
            throw new IllegalStateException(String.format("Failed to locate %s", SUBSYSTEM_XML_FILE));
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(url.toURI())));
            StringWriter writer = new StringWriter();
            try {
                String line = reader.readLine();
                while (line != null) {
                    writer.write(line);
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
            return writer.toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }


}
