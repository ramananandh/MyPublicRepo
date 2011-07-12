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
 * A class to represent a &lt;restriction&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class Restriction extends Annotation implements Serializable {
	
	static final long serialVersionUID = 1L;
		
    QName base = null;
    List<Attribute> attributes = new ArrayList<Attribute>();
    List<AttributeGroup> attributeGroups = new ArrayList<AttributeGroup>();
    Sequence sequence = null; 
    Choice choice = null;
    Group group = null;
	private SchemaAll all = null;


	/**
	 * Constructor
	 * @param el The dom element for this restriction
	 */
    Restriction(Element el, String tns) {
    	super(el, tns);

        base = SchemaType.getAttributeQName(el, "base");
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("attribute")) {
                    attributes.add(new Attribute(subEl, tns));
                } else if (elType.equals("sequence")) {
                	sequence = new Sequence(subEl, tns);
                } else if(elType.equals("choice")) {
                	choice = new Choice(subEl, tns);
                } else if(elType.equals("group")) {
                	group = new Group(subEl, tns);
                } else if(elType.equals("attributeGroup")){
                	attributeGroups.add(new AttributeGroup(subEl, tns));
                }else if(elType.equals("all")){
                	all = new SchemaAll(subEl, tns);
                }
            }
        }
    }

	/**
	 * Get the "base" attribute for this restriction
	 * @return The "base" attribute
	 */
    public QName getBase() {
        return base;
    }

	/**
	 * Get all the &lt;attribute&gt; elements within this restriction
	 * @return The &lt;attribute&gt; elements
	 */
	public Attribute[] getAttributes() {
        return (Attribute[]) attributes.toArray(
            new Attribute[attributes.size()]);
    }



    
    public List<Attribute> getAttributeList(){
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
	public boolean hasAll(){
		return all != null;
	}

	public SchemaAll getAll(){
		return all;
	}

}
