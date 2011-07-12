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
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent a top level &lt;element&gt; element in a schema
 *
 * @author Owen Burroughs <owenb@apache.org>
 */
public class ElementType extends SchemaType implements Serializable {
    
    static final long serialVersionUID = 1L;
    
    private String name = "";
    private QName typeName = null;
    private QName elementType = null;
    private List<SchemaType> childTypes = new ArrayList<SchemaType>();
    private boolean nillable = false;
    
    private ElementFormChoice formAttribute = null;
    private int minOccurs = -1;
    private int maxOccurs = -1;
    private QName ref = null;

    private SimpleType anonymousSimpleType = null;
    private ComplexType anonymousComplexType = null;

    
    /**
     * Constructor
     * @param el The dom element for this element
     */
	ElementType(Element el, String tns) {
		super(el, tns);
        // jgreif Webalo, Inc. -- incorrect to use tns as default namespace
        // for type or ref attribute value !
        //elementType = getAttributeQName(el, "type", tns);
        elementType = getAttributeQName(el, "type");
        typeName = getAttributeQName(el, "name", tns);
        // jgreif Webalo, Inc. -- ref attr may appear rather than name attr
        if (typeName == null) {
        	ref = getAttributeQName(el, "ref");
        }
        
        QName nillableAttr = getAttributeQName(el, "nillable", null);
        String stTrue = "true";
        if (nillableAttr != null && stTrue.equals(nillableAttr.getLocalPart())) {
            nillable = true;
        }
        

        parseOtherAttributes(el, tns);

        if (typeName != null){
            name = typeName.getLocalPart();
        }

        NodeList children = el.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("complexType")) {
                	anonymousComplexType = new ComplexType(subEl, tns, typeName);
                    childTypes.add( anonymousComplexType );
                } else if (elType.equals("simpleType")) {
                	anonymousSimpleType = new SimpleType(subEl, tns, typeName); 
                    childTypes.add( anonymousSimpleType );
                } else if (elType.equals("element")) {
                    childTypes.add(new ElementType(subEl, tns));
                } else {
                    //ignore all other types
                }
            }
        }
    }

    private void parseOtherAttributes(Element el, String tns){
        
        QName formAttribute = getAttributeQName(el, "form");
        if(formAttribute == null){
        	this.formAttribute = ElementFormChoice.QUALIFIED;
        }else{
        	this.formAttribute = ElementFormChoice.fromValue( formAttribute.getLocalPart() );
        }
        
        QName minOccurs = getAttributeQName(el, "minOccurs");
        if(minOccurs == null){
        	this.minOccurs = 1;
        }else{
        	this.minOccurs = Integer.parseInt( minOccurs.getLocalPart() );
        }

        QName maxOccurs = getAttributeQName(el, "maxOccurs");
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
    
    public String getName() {
    	return name;
    }
    
    /**
     * @see SchemaType#isElement()
     */
    public boolean isElement() {
        return true;
    }
    
    /**
     * @see SchemaType#getTypeName()
     */
    public QName getTypeName() {
        return typeName;
    }
    
    public QName getElementType() {
        return elementType;
    }
    
    public boolean isNillable() {
        return nillable;
    }
    
    /**
     * @see SchemaType#getChildren()
     */

    @SuppressWarnings("unchecked")
	public List getChildren() {
        return childTypes;
    }
    
	/**
	 * Returns the form choice of the element.
	 * @return
	 */
    public ElementFormChoice getForm(){
    	return formAttribute;
    }


    public int getMinOccurs(){
    	return minOccurs;
    }
    public int getMaxOccurs(){
    	return maxOccurs;
    }

    public QName getRef(){
    	return ref;
    }

    public boolean hasSimpleType(){
    	return anonymousSimpleType != null;
    }
   
    public SimpleType getSimpleType(){
    	return anonymousSimpleType;
    }

    public boolean hasComplexType(){
    	return anonymousComplexType != null;
    }

    public ComplexType getComplexType(){
    	return anonymousComplexType;
    }
	/**
	 * The enum type which represents the element Form options.
	 * @author rkulandaivel
	 *
	 */
	public static enum ElementFormChoice{
		QUALIFIED("qualified"),
		UNQUALIFIED("unqualified");
		
		private String value = null;
		ElementFormChoice(String value){
			this.value = value;
		}
		
	    public String value() {
	        return value;
	    }

		public static ElementFormChoice fromValue(String value){
			for(ElementFormChoice use : values()){
				if(use.value.equals(value)){
					return use;
				}
			}
			throw new IllegalArgumentException(value);
		}
	}

}
