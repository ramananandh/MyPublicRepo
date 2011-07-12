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
public class GroupType extends SchemaType {
	private QName groupName = null;
	private Choice choice = null;
	private Sequence sequence = null;
	private SchemaAll all = null;
	
	public GroupType(Element el, String tns) {
		super(el, tns);
		groupName = getAttributeQName(el, "name", tns);

		NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("choice")) {
                	choice = new Choice(subEl, tns);
                	break;
                }else if(elType.equals("sequence")){
                	sequence = new Sequence(subEl, tns);
                	break;
                }else if(elType.equals("all")){
                	all = new SchemaAll(subEl, tns);
                	break;
                }
            }
        }
	}
	public QName getTypeName(){
		return groupName;
	}
	public boolean hasChoice(){
		return choice != null;
	}

	public Choice getChoice(){
		return choice;
	}

	public boolean hasSequence(){
		return sequence != null;
	}

	public Sequence getSequence(){
		return sequence;
	}
	
	public boolean hasAll(){
		return all != null;
	}

	public SchemaAll getAll(){
		return all;
	}
}
