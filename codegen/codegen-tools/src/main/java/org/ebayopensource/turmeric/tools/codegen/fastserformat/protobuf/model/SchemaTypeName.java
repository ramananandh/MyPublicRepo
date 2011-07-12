/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;

import javax.xml.namespace.QName;

/**
 * @author rkulandaivel
 *
 */
public class SchemaTypeName {
	private QName m_typeName = null;

	public SchemaTypeName(QName typeName){
		m_typeName = typeName;
	}
	public QName getTypeName() {
		return m_typeName;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof SchemaTypeName){
			return m_typeName.equals( ((SchemaTypeName)o).getTypeName() );			
		}
		return false;
	}

	@Override
	public int hashCode() {
		return m_typeName.hashCode();
	}

	@Override
	public String toString() {
		return "SchemaTypeName [m_typeName=" + m_typeName + "]";
	}
	
	
}
