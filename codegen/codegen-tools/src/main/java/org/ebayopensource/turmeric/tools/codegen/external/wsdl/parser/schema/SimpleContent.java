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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent a &lt;simpleContent&gt; element in a schema
 * 
 * @author arajmony
 */
public class SimpleContent extends Annotation implements Serializable {
	
	static final long serialVersionUID = 1L;
	
	private SimpleTypeRestriction restriction = null;
	private SimpleContentExtension extention = null;	


	/**
	 * Constructor
	 * @param el The dom element for this simpleContent
	 */	
	SimpleContent(Element el, String tns) {
		super(el, tns);
		NodeList children = el.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element subEl = (Element) child;
				String elType = subEl.getLocalName();
				if (elType.equals("restriction")) {
					restriction = new SimpleTypeRestriction(subEl, tns);
					break;
				} else if (elType.equals("extension")) {
					extention = new SimpleContentExtension(subEl,tns);
					break;
				}
			}			
		}	
	}
	
	/**
	 * Get the restriction element for this simpleContent
	 * @return A Restriction object representing the restriction
	 */
	public SimpleTypeRestriction getRestriction() {
		return restriction;
	}

	/**
	 * Get the extension element for this simpleContent
	 * @return An Extension object representing the restriction
	 */	
	public SimpleContentExtension getExtension() {
		return extention;
	}	
}
