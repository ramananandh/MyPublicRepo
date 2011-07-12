/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author rkulandaivel
 *
 */
public class SimpleTypeList extends Annotation {

	QName itemType = null;
	SimpleType anonymousSimpleType = null;

	public SimpleTypeList(Element el, String tns) {
		super(el, tns);
		itemType = SchemaType.getAttributeQName(el, "itemType");
		  NodeList children = el.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            if (child.getNodeType() == Node.ELEMENT_NODE) {
	                Element subEl = (Element) child;
	                String elType = subEl.getLocalName();
	                if (elType.equals("simpleType")) {
	                	anonymousSimpleType = new SimpleType(subEl, tns);
	                	break;
	                }
	            }
	        }
	}
	
	public QName getItemType(){
		return itemType;
	}
	
	public boolean hasSimpleType(){
		return anonymousSimpleType != null;
	}
	
	public SimpleType getSimpleType(){
		return anonymousSimpleType;
	}
}
