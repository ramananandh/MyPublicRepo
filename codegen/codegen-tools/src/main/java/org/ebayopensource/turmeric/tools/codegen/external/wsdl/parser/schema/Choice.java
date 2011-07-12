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
public class Choice  extends Annotation {
	


	List<SequenceElement> elements = new ArrayList<SequenceElement>();
	private List<Choice> choices = new ArrayList<Choice>();
	private List<Group> groups = new ArrayList<Group>();
	private List<Sequence> sequences = new ArrayList<Sequence>();

    private int minOccurs = -1;
    private int maxOccurs = -1;

	Choice(Element el, String tns) {
		super(el, tns);
		NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("element")) {
                    elements.add(new SequenceElement(subEl, tns));
                }else if(elType.equals("choice")) {
                	Choice ch = new Choice(subEl, tns); 
                	choices.add( ch );
                }else if(elType.equals("group")) {
                	Group gp = new Group(subEl, tns); 
                	groups.add( gp );
                }else if(elType.equals("sequence")) {
                	Sequence gp = new Sequence(subEl, tns); 
                	sequences.add( gp );
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
    
	public List<Choice> getChoices(){
		return choices;
	}

	public List<Sequence> getSequences(){
		return sequences;
	}

	public List<Group> getGroups(){
		return groups;
	}
}
