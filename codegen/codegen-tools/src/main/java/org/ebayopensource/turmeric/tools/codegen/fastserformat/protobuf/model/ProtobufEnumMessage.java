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
public class ProtobufEnumMessage extends ProtobufMessage {
	private String m_enumMessageName = null;
	private List<ProtobufEnumEntry> m_enumEntries = null;
	public String getEnumMessageName() {
		return m_enumMessageName;
	}
	public void setEnumMessageName(String enumMessageName) {
		this.m_enumMessageName = enumMessageName;
	}
	public List<ProtobufEnumEntry> getEnumEntries() {
		if( m_enumEntries == null ){
			m_enumEntries = new ArrayList<ProtobufEnumEntry>();
		}
		return m_enumEntries;
	}
	@Override
	public String toString() {
		return "ProtobufEnumMessage [m_enumEntries=" + m_enumEntries
				+ ", m_enumMessageName=" + m_enumMessageName
				+ ", getEprotoClassName()=" + getEprotoClassName()
				+ ", getFields()=" + getFields() + ", getJaxbClassName()="
				+ getJaxbClassName() + ", getJprotoClassName()="
				+ getJprotoClassName() + ", getMessageComments()="
				+ getMessageComments() + ", getMessageName()="
				+ getMessageName() + ", getSchemaTypeName()="
				+ getSchemaTypeName() + ", isEnumType()=" + isEnumType()
				+ ", isRootType()=" + isRootType() + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((m_enumEntries == null) ? 0 : m_enumEntries.hashCode());
		result = prime
				* result
				+ ((m_enumMessageName == null) ? 0 : m_enumMessageName
						.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ProtobufEnumMessage other = (ProtobufEnumMessage) obj;
		if (m_enumEntries == null) {
			if (other.m_enumEntries != null)
				return false;
		} else if (!m_enumEntries.equals(other.m_enumEntries))
			return false;
		if (m_enumMessageName == null) {
			if (other.m_enumMessageName != null)
				return false;
		} else if (!m_enumMessageName.equals(other.m_enumMessageName))
			return false;
		return true;
	}

	
	
}
