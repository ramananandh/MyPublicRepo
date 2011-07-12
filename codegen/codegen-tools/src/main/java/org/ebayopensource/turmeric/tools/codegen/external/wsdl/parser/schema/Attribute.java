/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema;

import java.io.Serializable;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent an &lt;attribute&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class Attribute extends SchemaType implements Serializable {
	
	static final long serialVersionUID = 1L;
	
	Hashtable<QName, QName> attributes = new Hashtable<QName, QName>();
	SimpleType anonymousSimpleType = null;
	

	/**
	 * Constructor
	 * @param el The dom element for this attribute
	 */	
	Attribute(Element el, String tns) {
		super(el, tns);
		SchemaType.getAllAttributes(el, tns, attributes);
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
	
	/**
	 * Get the value of a specified attribute on this element
	 * @param The name of the attribute
	 * @return The value of the attribute or null if the attribute does not exist
	 */	
	public QName getXMLAttribute(String name) {
        return (QName) attributes.get(new QName(name));
    }

	/**
	 * Get the value of a specified attribute on this element when the attribute name is
	 * a QName
	 * @param The name of the attribute
	 * @return The value of the attribute or null if the attribute does not exist
	 */		
	public QName getXMLAttribute(QName name) {
        return (QName) attributes.get(name);
    }

	/**
	 * Returns the value of attribute name
	 * @return
	 */
	public String getAttributeName(){
		QName nameAttribute = getXMLAttribute("name");
		if(nameAttribute != null){
			return nameAttribute.getLocalPart();
		}
		return null;
	}
	
	public QName getAttributeQName(){
		return getXMLAttribute("name");
	}

	public QName getAttributeRef(){
		return getXMLAttribute("ref");
	}
	/**
	 * Returns the type of the attribute
	 * @return
	 */
	public QName getValueType(){
		return getXMLAttribute("type");
	}

	/**
	 * Returns the value attribute use as a enum type
	 * @return
	 */
	public AttributeUse getUse(){
		QName useAttribute = getXMLAttribute("use");
		if(useAttribute == null){
			return AttributeUse.OPTIONAL; 
		}
		return AttributeUse.fromValue( useAttribute.getLocalPart() );
	}

	public boolean hasSimpleType(){
		return anonymousSimpleType != null;
	}
	
	public SimpleType getSimpleType(){
		return anonymousSimpleType;
	}
	/**
	 * The enum type which represents the Attribute use options.
	 * @author rkulandaivel
	 *
	 */
	public static enum AttributeUse{
		OPTIONAL("optional"),
		REQUIRED("required"),
		PROHIBHITED("prohibited");
		
		private String value = null;
		AttributeUse(String value){
			this.value = value;
		}
		
	    public String value() {
	        return value;
	    }

		public static AttributeUse fromValue(String value){
			for(AttributeUse use : values()){
				if(use.value.equals(value)){
					return use;
				}
			}
	        throw new IllegalArgumentException(value);
		}
	}
	@Override
	public QName getTypeName() {
		return getXMLAttribute("name");
	}
}
