/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rkulandaivel
 * 
 */
public class ProtobufMessage {
	private String m_messageName = null;
	private List<ProtobufField> m_fields = null;
	private String m_jprotoClassName = null;
	private String m_eprotoClassName = null;
	private String m_jaxbClassName = null;
	private boolean m_enumType = false;
	private SchemaTypeName m_schemaTypeName = null;
	private boolean m_rootType = false;
	private String m_messageComments = null;

	public String getMessageName() {
		return m_messageName;
	}

	public void setMessageName(String messageName) {
		this.m_messageName = messageName;
	}

	public List<ProtobufField> getFields() {
		if( m_fields == null ){
			m_fields = new ArrayList<ProtobufField>();
		}
		return m_fields;
	}


	public String getJprotoClassName() {
		return m_jprotoClassName;
	}

	public void setJprotoClassName(String jprotoClassName) {
		this.m_jprotoClassName = jprotoClassName;
	}

	public String getEprotoClassName() {
		return m_eprotoClassName;
	}

	public void setEprotoClassName(String eprotoClassName) {
		this.m_eprotoClassName = eprotoClassName;
	}

	public boolean isEnumType() {
		return m_enumType;
	}

	public void setEnumType(boolean enumType) {
		this.m_enumType = enumType;
	}

	public String getJaxbClassName() {
		return m_jaxbClassName;
	}

	public void setJaxbClassName(String jaxbClassName) {
		this.m_jaxbClassName = jaxbClassName;
	}

	public SchemaTypeName getSchemaTypeName() {
		return m_schemaTypeName;
	}

	public void setSchemaTypeName(SchemaTypeName schemaTypeName) {
		this.m_schemaTypeName = schemaTypeName;
	}

	public boolean isRootType() {
		return m_rootType;
	}

	public void setRootType(boolean rootType) {
		this.m_rootType = rootType;
	}

	public String getMessageComments() {
		return m_messageComments;
	}

	public void setMessageComments(String messageComments) {
		this.m_messageComments = messageComments;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((m_schemaTypeName == null) ? 0 : m_schemaTypeName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ProtobufMessage)) {
			return false;
		}
		ProtobufMessage other = (ProtobufMessage) obj;
		if (m_schemaTypeName == null) {
			if (other.m_schemaTypeName != null) {
				return false;
			}
		} else if (!m_schemaTypeName.equals(other.m_schemaTypeName)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProtobufMessage [m_enumType=" + m_enumType
				+ ", m_eprotoClassName=" + m_eprotoClassName + ", m_fields="
				+ m_fields + ", m_jaxbClassName=" + m_jaxbClassName
				+ ", m_jprotoClassName=" + m_jprotoClassName
				+ ", m_messageName=" + m_messageName + ", m_rootType="
				+ m_rootType + ", m_schemaTypeName=" + m_schemaTypeName + "]";
	}
	
	
}
