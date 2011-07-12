package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Generate tag numbers for a protobuf message
 */
public interface ProtobufTagGenerator
{
    /**
     *  Obtain field names that belong to a type, along with their tag numbers 
     * @param name Name of the schema type in question
     * @return Given a Schema type's Qname, return the elements / attributes associated with that type and their corresponding tag number
     */
    public Map<String, Integer> getTagsForType(QName name);
    /**
     * Returns internal information maintained by this component to generate tag numbers. Ideally, this method is not needed. It should be deprecated if possible
     * @return - A map of internal data
     */
    public Map<String, Integer> getTagsToPersist();
}
