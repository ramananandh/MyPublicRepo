/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * @author rkulandaivel
 *
 */
public class AttributeGroup extends Annotation {

	

	QName groupType = null;

	public AttributeGroup(Element el, String tns) {
		super(el, tns);
		groupType = SchemaType.getAttributeQName(el, "ref");
	}

	public QName getGroupRef(){
		return groupType;
	}
}
