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

/**
 * A class to represent an &lt;element&gt; element defined within a &lt;sequence&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class SequenceElement extends ElementType implements Serializable {
	
	static final long serialVersionUID = 1L;
		
    /**
     * Constructor
     * @param el The dom element for this element within a sequence
     */
    SequenceElement(Element el, String tns) {
        super(el, tns);
    }
}
