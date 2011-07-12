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

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.WSDLParserConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent a &lt;complexType&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class ComplexType extends SchemaType implements Serializable {

	static final long serialVersionUID = 1L;

    private boolean isAnArray = false;
    private QName typeName = null;
    private int arrayDim = 0;
    private ComplexContent complexContent = null;
    Sequence sequence = null; 
    Choice choice = null;
    Group group = null;
    private SchemaAll all = null;
    List<AttributeGroup> attributeGroups = new ArrayList<AttributeGroup>();
    List<Attribute> attributes = new ArrayList<Attribute>();
    SimpleContent simpleContent = null;
    boolean typeAbstract = false;

    private static final QName soapEncArray =
        new QName(WSDLParserConstants.NS_URI_SOAP_ENC, "Array");
    private static final QName soapEncArrayType =
        new QName(WSDLParserConstants.NS_URI_SOAP_ENC, "arrayType");
    private static final QName wsdlArrayType =
        new QName(WSDLParserConstants.NS_URI_WSDL, "arrayType");
    ArrayList sequenceElements = new ArrayList();        

	/**
	 * Constructor
	 * @param el The dom element for this complexType
	 */
    ComplexType(Element el, String tns) {
    	super(el, tns);
        typeName = getAttributeQName(el, "name", tns);
        process(el, tns);
       
    }

    ComplexType(Element el, String tns, QName typeName) {
    	super(el, tns);
        this.typeName = typeName;        
        process(el, tns);

    }


	/**
	 * @see SchemaType#isComplex()
	 */ 
    public boolean isComplex() {
        return true;
    }

	/**
	 * @see SchemaType#isArray()
	 */ 
    public boolean isArray() {
        return isAnArray;
    }

	/**
	 * @see SchemaType#getArrayDimension()
	 */ 
    public int getArrayDimension() {
        return arrayDim;
    }

	/**
	 * @see SchemaType#getTypeName()
	 */ 
    public QName getTypeName() {
        return typeName;
    }

    private void process(Element el, String tns) {
    	QName abstractAttribute = getAttributeQName(el, "abstract", tns);
    	if( abstractAttribute != null ){
    		typeAbstract = Boolean.parseBoolean( abstractAttribute.getLocalPart() );
    	}
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("attribute")) {
                    attributes.add(new Attribute(subEl, tns));
                }else if (elType.equals("complexContent")) {
                    complexContent = new ComplexContent(subEl, tns);
                } else if (elType.equals("sequence")) {
                	sequence = new Sequence(subEl, tns);
                } else if(elType.equals("choice")) {
                	choice = new Choice(subEl, tns);
                } else if(elType.equals("group")) {
                	group = new Group(subEl, tns);
                } else if(elType.equals("attributeGroup")){
                	attributeGroups.add(new AttributeGroup(subEl, tns));
                } else if(elType.equals("simpleContent")){
                	simpleContent = new SimpleContent(subEl, tns);
                }else if(elType.equals("all")){
                	all = new SchemaAll(subEl, tns);
                }
            }
        }
    }
      
    
    /**
     * @author arajmony
     */
    public ComplexContent getComplexContent(){
    	return complexContent;
    }
    public boolean hasComplexContent(){
    	return complexContent != null;
    }
    public List<Attribute> getAttributes(){
    	return attributes;
    }
    
    public List<AttributeGroup> getAttributeGroup(){
    	return attributeGroups;
    }

    public boolean hasChoice(){
    	return choice != null;
    }
    public Choice getChoice(){
    	return choice;
    }

    public boolean hasGroup(){
    	return group != null;
    }
    public Group getGroup(){
    	return group;
    }
    
    public boolean hasSequence(){
    	return sequence != null;
    }
    public Sequence getSequence(){
    	return sequence;
    }
    
    public boolean hasSimpleContent(){
    	return simpleContent != null;
    }
    public SimpleContent getSimpleContent(){
    	return simpleContent;
    }
    
    public boolean isAbstract(){
    	return typeAbstract;
    }
    
	public boolean hasAll(){
		return all != null;
	}

	public SchemaAll getAll(){
		return all;
	}

}
