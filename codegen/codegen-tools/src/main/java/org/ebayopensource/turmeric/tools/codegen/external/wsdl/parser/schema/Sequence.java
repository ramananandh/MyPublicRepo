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
public class Sequence extends Annotation{

	
	private List<Choice> choices = new ArrayList<Choice>();
	private List<SequenceElement> elements = new ArrayList<SequenceElement>();
	private List<Group> groups = new ArrayList<Group>();
	private List<Sequence> sequences = new ArrayList<Sequence>();
	private List<SequenceEntry> entries = new ArrayList<SequenceEntry>();

    private int minOccurs = -1;
    private int maxOccurs = -1;

	Sequence(Element el, String tns) {
		super(el, tns);
		NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("element")) {
                	SequenceElement elem = new SequenceElement(subEl, tns); 
                    elements.add( elem );
                    entries.add( new SequenceEntry(SequenceEntryType.ELEMENTTYPE, elem) );
                }else if(elType.equals("choice")) {
                	Choice ch = new Choice(subEl, tns); 
                	choices.add( ch );
                    entries.add( new SequenceEntry(SequenceEntryType.CHOICE, ch) );
                }else if(elType.equals("group")) {
                	Group gp = new Group(subEl, tns); 
                	groups.add( gp );
                    entries.add( new SequenceEntry(SequenceEntryType.GROUP, gp) );
                }else if(elType.equals("sequence")) {
                	Sequence gp = new Sequence(subEl, tns); 
                	sequences.add( gp );
                    entries.add( new SequenceEntry(SequenceEntryType.SEQUENCE, gp) );
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

	public List<SequenceEntry> getEntries(){
		return entries;
	}
	
    public int getMinOccurs(){
    	return minOccurs;
    }
    public int getMaxOccurs(){
    	return maxOccurs;
    }

	public static enum SequenceEntryType{
		CHOICE("choice"),
		GROUP("group"),
		SEQUENCE("Sequence"),
		ELEMENTTYPE("ElementType");
		
		private String value = null;
		SequenceEntryType(String value){
			this.value = value;
		}
		
	    public String value() {
	        return value;
	    }

		public static SequenceEntryType fromValue(String value){
			for(SequenceEntryType use : values()){
				if(use.value.equals(value)){
					return use;
				}
			}
	        throw new IllegalArgumentException(value);
		}
	}

	public static class SequenceEntry{
		private SequenceEntryType type = null;
		private Object value;
		
		public SequenceEntry(SequenceEntryType type, Object value){
			this.value = value;
			this.type = type;
		}
		public boolean isChoice(){
			return SequenceEntryType.CHOICE == type;
		}
		
		public Choice getChoice(){
			return (Choice)value;
		}

		public boolean isGroup(){
			return SequenceEntryType.GROUP == type;
		}
		
		public Group getGroup(){
			return (Group)value;
		}

		public boolean isSequence(){
			return SequenceEntryType.SEQUENCE == type;
		}
		
		public Sequence getSequence(){
			return (Sequence)value;
		}
		
		public boolean isElement(){
			return SequenceEntryType.ELEMENTTYPE == type;
		}
		
		public SequenceElement getElement(){
			return (SequenceElement)value;
		}
	}
}
