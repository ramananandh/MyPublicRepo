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
public class RestrictionEnumeration extends Annotation {

	private String value = null;
	
	public RestrictionEnumeration(Element el, String tns) {
		super(el, tns);
		QName value = SchemaType.getAttributeQName(el, "value");
		if(value == null){
			this.value = "";
		}else{
			this.value = value.getLocalPart();
		}
	}

	public String getEnumValue(){
		return this.value;
	}
}
