/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema;

import java.math.BigDecimal;
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
public class SimpleTypeRestriction extends Annotation {

    private QName base = null;
    List<Attribute> attributes = new ArrayList<Attribute>();
    List<AttributeGroup> attributeGroups = new ArrayList<AttributeGroup>();
    List<RestrictionEnumeration> enumerations = new ArrayList<RestrictionEnumeration>();
    
    private int fractionDigits = -1;
    private int length = -1;
    private BigDecimal maxExclusive = null;
    private BigDecimal minExclusive = null;
    private BigDecimal maxInclusive = null;
    private BigDecimal minInclusive = null;
    private int maxLength = -1;
    private int minLength = -1;
    private String pattern = null;
    private String whiteSpace = null;
    private int totalDigits = -1;

	public SimpleTypeRestriction(Element el, String tns) {
		super(el, tns);
		base = SchemaType.getAttributeQName(el, "base");
		 NodeList children = el.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            if (child.getNodeType() == Node.ELEMENT_NODE) {
	                Element subEl = (Element) child;
	                String elType = subEl.getLocalName();
	                QName value = SchemaType.getAttributeQName(subEl, "value");
	                if (elType.equals("fractionDigits") && (value != null) ) {
	                	fractionDigits = Integer.parseInt(value.getLocalPart());
	                }else if(elType.equals("length") && (value != null) ) {
	                	length = Integer.parseInt(value.getLocalPart());
	                }else if(elType.equals("maxExclusive") && (value != null) ) {
	                	maxExclusive = new BigDecimal(value.getLocalPart());
	                }else if(elType.equals("maxInclusive") && (value != null) ) {
	                	maxInclusive = new BigDecimal(value.getLocalPart());
	                }else if(elType.equals("maxLength") && (value != null) ) {
	                	maxLength = Integer.parseInt(value.getLocalPart());
	                }else if(elType.equals("minExclusive") && (value != null) ) {
	                	minExclusive = new BigDecimal(value.getLocalPart());
	                }else if(elType.equals("minInclusive") && (value != null) ) {
	                	minInclusive = new BigDecimal(value.getLocalPart());
	                }else if(elType.equals("minLength") && (value != null) ) {
	                	minLength = Integer.parseInt(value.getLocalPart());
	                }else if(elType.equals("pattern") && (value != null) ) {
	                	pattern = value.getLocalPart();
	                }else if(elType.equals("totalDigits") && (value != null) ) {
	                	totalDigits = Integer.parseInt(value.getLocalPart());
	                }else if(elType.equals("whiteSpace") && (value != null) ) {
	                	whiteSpace = value.getLocalPart();
	                } else if(elType.equals("attributeGroup")){
	                	attributeGroups.add(new AttributeGroup(subEl, tns));
	                } else if(elType.equals("attribute")){
	                	attributes.add(new Attribute(subEl, tns));
	                }else if(elType.equals("enumeration")){
	                	enumerations.add( new RestrictionEnumeration(subEl, tns) );
	                }
	                
	            }
	        }
	                
	               
	}

	
	public QName getBase() {
		return base;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public List<AttributeGroup> getAttributeGroups() {
		return attributeGroups;
	}

	public List<RestrictionEnumeration> getEnumerations() {
		return enumerations;
	}

	public boolean hasFractionDigits() {
		return fractionDigits != -1;
	}

	public int getFractionDigits() {
		return fractionDigits;
	}

	public boolean hasLength() {
		return length != -1;
	}

	public int getLength() {
		return length;
	}

	public boolean hasMaxExclusive() {
		return maxExclusive != null;
	}

	public BigDecimal getMaxExclusive() {
		return maxExclusive;
	}

	public boolean hasMinExclusive() {
		return minExclusive != null;
	}

	public BigDecimal getMinExclusive() {
		return minExclusive;
	}

	public boolean hasMaxInclusive() {
		return maxInclusive != null;
	}

	public BigDecimal getMaxInclusive() {
		return maxInclusive;
	}

	public boolean hasMinInclusive() {
		return minInclusive != null;
	}

	public BigDecimal getMinInclusive() {
		return minInclusive;
	}

	public boolean hasMaxLength() {
		return maxLength != -1;
	}

	public int getMaxLength() {
		return maxLength;
	}


	public boolean hasMinLength() {
		return minLength != -1;
	}
	public int getMinLength() {
		return minLength;
	}

	public boolean hasPattern() {
		return pattern !=null;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean hasWhiteSpace() {
		return whiteSpace != null;
	}

	public String getWhiteSpace() {
		return whiteSpace;
	}

	public boolean hasTotalDigits() {
		return totalDigits != -1;
	}

	public int getTotalDigits() {
		return totalDigits;
	}

	
}
