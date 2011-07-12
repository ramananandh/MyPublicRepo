/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;

import javax.xml.namespace.QName;

/**
 * @author rkulandaivel
 * 
 */
public class ProtobufField {
	private String m_fieldName = null;
	private String m_convertedFieldName = null;
	private ProtobufFieldType m_typeOfField = null;
	private QName m_xsdTypeName = null;
	private String m_protobufTypeName = null;
	private ProtobufFieldModifier m_fieldModifier = null;
	private int m_sequenceTagNumber = 0;
	private boolean m_nillable = false;
	private String m_fieldComments = null;
	
	public String getFieldName() {
		return m_fieldName;
	}

	public void setFieldName(String fieldName) {
		this.m_fieldName = fieldName;
	}

	public ProtobufFieldType getTypeOfField() {
		return m_typeOfField;
	}

	public void setTypeOfField(ProtobufFieldType typeOfField) {
		this.m_typeOfField = typeOfField;
	}

	public ProtobufFieldModifier getFieldModifier() {
		return m_fieldModifier;
	}

	public void setFieldModifier(ProtobufFieldModifier fieldModifier) {
		this.m_fieldModifier = fieldModifier;
	}

	public int getSequenceTagNumber() {
		return m_sequenceTagNumber;
	}

	public void setSequenceTagNumber(int sequenceTagNumber) {
		this.m_sequenceTagNumber = sequenceTagNumber;
	}


	public QName getXsdTypeName() {
		return m_xsdTypeName;
	}

	public void setXsdTypeName(QName xsdTypeName) {
		this.m_xsdTypeName = xsdTypeName;
	}

	public String getProtobufTypeName() {
		return m_protobufTypeName;
	}

	public void setProtobufTypeName(String protobufTypeName) {
		this.m_protobufTypeName = protobufTypeName;
	}

	/**
	 * @return the nillable
	 */
	public boolean isNillable() {
		return m_nillable;
	}

	/**
	 * @param nillable the nillable to set
	 */
	public void setNillable(boolean nillable) {
		this.m_nillable = nillable;
	}

	public String getFieldComments() {
		return m_fieldComments;
	}

	public void setFieldComments(String fieldComments) {
		this.m_fieldComments = fieldComments;
	}

	
	public String getConvertedFieldName() {
		return m_convertedFieldName;
	}

	public void setConvertedFieldName(String convertedFieldName) {
		this.m_convertedFieldName = convertedFieldName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProtobufField [m_fieldModifier=" + m_fieldModifier
				+ ", m_convertedFieldName=" + m_convertedFieldName + ", m_nillable=" + m_nillable
				+ ", m_protobufTypeName=" + m_protobufTypeName
				+ ", m_sequenceTagNumber=" + m_sequenceTagNumber
				+ ", m_typeOfField=" + m_typeOfField 
				//+ ", m_xsdTypeName=" + m_xsdTypeName 
				+ "]";
	}

	
}
