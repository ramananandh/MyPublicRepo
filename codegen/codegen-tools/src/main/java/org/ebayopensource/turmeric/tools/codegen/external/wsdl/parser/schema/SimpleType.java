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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent a &lt;simpleType&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class SimpleType extends SchemaType implements Serializable {

	static final long serialVersionUID = 1L;
	
	private String name = "";
	private QName typeName = null;
	private SimpleTypeRestriction restriction = null;
	private SimpleTypeList list = null;


	/**
	 * Constructor
	 * @param el The dom element for this simpleType
	 */	
    SimpleType(Element el, String tns) {
    	super(el, tns);
        typeName = getAttributeQName(el, "name", tns);
        processOtherElements(el, tns);
        // If the element has no name, we cannot map it. Don't do any more processing
        // of this type
        if (typeName == null) return;        
        
        name = typeName.getLocalPart();
    }
    
    SimpleType(Element el, String tns, QName typeName) {
    	super(el, tns);
        this.typeName = typeName;
        processOtherElements(el, tns);

        // If the element has no name, we cannot map it. Don't do any more processing
        // of this type
        if (typeName == null) return;        
        
        name = typeName.getLocalPart();    
    }
    
    public void processOtherElements(Element el, String tns){
		 NodeList children = el.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            if (child.getNodeType() == Node.ELEMENT_NODE) {
	                Element subEl = (Element) child;
	                String elType = subEl.getLocalName();
	                if (elType.equals("list")  ) {
	                	list = new SimpleTypeList(subEl, tns);
	                }else if(elType.equals("restriction")  ) {
	                	restriction = new SimpleTypeRestriction(subEl, tns);
	                }
	            }
	        }

    }

    
    public String getName() {
    	return name;
    }
    

	/**
	 * @see SchemaType#isComplex()
	 */    
	public boolean isComplex() {
		return false;
	}

	/**
	 * @see SchemaType#getTypeName()
	 */	
	public QName getTypeName() {
		return typeName;
	}
	
	/**
	 * @see SchemaType#isSimple()
	 */ 
    public boolean isSimple() {
        return true;
    }
    
    public SimpleTypeList getList(){
    	return list;
    }
    
    public SimpleTypeRestriction getRestriction(){
    	return restriction;
    }

}
