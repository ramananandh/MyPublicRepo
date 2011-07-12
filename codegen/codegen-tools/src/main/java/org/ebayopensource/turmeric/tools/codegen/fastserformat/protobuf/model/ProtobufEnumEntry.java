/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;

/**
 * @author rkulandaivel
 * 
 */
public class ProtobufEnumEntry {
	private String m_xsdEnumValue = null;
	private String m_enumValue = null;
	private int m_sequenceNumber = 0;
	private String m_fieldComments = null;

	public String getEnumValue() {
		return m_enumValue;
	}

	public void setEnumValue(String enumValue) {
		this.m_enumValue = enumValue;
	}

	public int getSequenceNumber() {
		return m_sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.m_sequenceNumber = sequenceNumber;
	}

	public String getFieldComments() {
		return m_fieldComments;
	}

	public void setFieldComments(String fieldComments) {
		this.m_fieldComments = fieldComments;
	}

	public String getXsdEnumValue() {
		return m_xsdEnumValue;
	}

	public void setXsdEnumValue(String xsdEnumValue) {
		this.m_xsdEnumValue = xsdEnumValue;
	}

	@Override
	public String toString() {
		return "ProtobufEnumEntry [m_enumValue=" + m_enumValue
				+ ", m_sequenceNumber=" + m_sequenceNumber + "]";
	}
	
}
