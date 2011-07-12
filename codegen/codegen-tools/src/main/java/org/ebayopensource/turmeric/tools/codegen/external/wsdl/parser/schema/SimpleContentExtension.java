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
public class SimpleContentExtension extends Annotation{

	private QName base = null;
    List<Attribute> attributes = new ArrayList<Attribute>();
    List<AttributeGroup> attributeGroups = new ArrayList<AttributeGroup>();

	public SimpleContentExtension(Element el, String tns) {
		super(el, tns);
		base = SchemaType.getAttributeQName(el, "base");
		 NodeList children = el.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            if (child.getNodeType() == Node.ELEMENT_NODE) {
	                Element subEl = (Element) child;
	                String elType = subEl.getLocalName();
	                if(elType.equals("attributeGroup")){
	                	attributeGroups.add(new AttributeGroup(subEl, tns));
	                } else if(elType.equals("attribute")){
	                	attributes.add(new Attribute(subEl, tns));
	                }
	            }
	        }
	        
	}
	
	public QName getBase(){
		return base;
	}

	public List<Attribute> getAttributes(){
		return attributes;
	}

	public List<AttributeGroup> getAttributeGroups(){
		return attributeGroups;
	}
}
