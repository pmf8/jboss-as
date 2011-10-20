package org.jboss.as.clustering.infinispan.subsystem;

import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamException;
import java.util.List;

/**
 * Parser for Infinispan subsystem.
 *
 * User: rachmato@redhat.com
 */
public class InfinispanSubsystemParser implements XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    /**
     * (@inheritDoc)
     */
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {

    }

    /**
     * (@inheritDoc)
     */
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> operation) throws XMLStreamException {

    }

}
