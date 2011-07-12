/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author rkulandaivel
 *
 */
public  class Annotation  {

	private boolean containsAnnotation = false;
	private Documentation documentation = null;
	public Annotation(Element el, String tns) {
		
        NodeList children = el.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("annotation")) {
                	containsAnnotation = true;
                	NodeList annotationChildren = subEl.getChildNodes();
                    for (int j=0; j<annotationChildren.getLength(); j++) {
                        Node annotationChild = annotationChildren.item(j);
                        if (annotationChild.getNodeType() == Node.ELEMENT_NODE) {
                            Element annotationSubEl = (Element) annotationChild;
                            String subElType = annotationSubEl.getLocalName();
                            if (subElType.equals("documentation")) {
                            	documentation = new Documentation(annotationSubEl, tns);
                            }
                        }
                    }
                }
            }
        }
	}

	public boolean hasAnnotation() {
		return containsAnnotation ;
	}
	public Documentation getDocumentation() {
		return documentation;
	}
	public boolean hasDocumentation() {
		return documentation != null;
	}

}
