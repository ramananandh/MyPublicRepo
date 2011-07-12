/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema;

import org.w3c.dom.Element;


/**
 * @author rkulandaivel
 *
 */
public class Documentation {
    protected String content;

	public Documentation(Element el, String tns) {
		content = el.getTextContent();
	}

	public String getContent() {
		return content;
	}

}
