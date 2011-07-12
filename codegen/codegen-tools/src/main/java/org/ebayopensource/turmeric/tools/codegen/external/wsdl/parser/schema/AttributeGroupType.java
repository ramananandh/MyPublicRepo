/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author rkulandaivel
 *
 */
public class AttributeGroupType extends SchemaType {


	private static final long serialVersionUID = 1L;
    private QName groupName = null;
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private List<AttributeGroup> attributeGroups = new ArrayList<AttributeGroup>();

	public AttributeGroupType(Element el, String tns) {
		super(el, tns);
        groupName = getAttributeQName(el, "name", tns);
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("attribute")) {
                	attributes.add( new Attribute(subEl, tns) );
                }else if(elType.equals("attributeGroup")){
                	attributeGroups.add( new AttributeGroup(subEl, tns) );
                }
            }
        }
	}

	public QName getTypeName(){
		return groupName;
	}

	public List<AttributeGroup> getAttributeGroups(){
		return attributeGroups;
	}
	
	public List<Attribute> getAttributes(){
		return attributes;
	}
}
