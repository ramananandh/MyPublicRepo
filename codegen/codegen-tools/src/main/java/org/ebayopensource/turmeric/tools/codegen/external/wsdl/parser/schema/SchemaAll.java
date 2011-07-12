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
public class SchemaAll extends Annotation {
	private List<SequenceElement> elements = new ArrayList<SequenceElement>();

    private int minOccurs = -1;
    private int maxOccurs = -1;

	public SchemaAll(Element el, String tns) {
		super(el, tns);
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("element")) {
                	elements.add(new SequenceElement(subEl, tns));
                }
            }
        }
        
        
        QName minOccurs = SchemaType.getAttributeQName(el, "minOccurs");
        if(minOccurs == null){
        	this.minOccurs = 1;
        }else{
        	this.minOccurs = Integer.parseInt( minOccurs.getLocalPart() );
        }

        QName maxOccurs = SchemaType.getAttributeQName(el, "maxOccurs");
        if(maxOccurs == null){
        	this.maxOccurs = 1;
        }else{
        	if("unbounded".equals( maxOccurs.getLocalPart() ) ){
        		this.maxOccurs = 999999999;
        	}else{
        		this.maxOccurs = Integer.parseInt( maxOccurs.getLocalPart() );
        	}
        }
	}

	public List<SequenceElement> getElements(){
		return elements;
	}

    public int getMinOccurs(){
    	return minOccurs;
    }
    public int getMaxOccurs(){
    	return maxOccurs;
    }
}
