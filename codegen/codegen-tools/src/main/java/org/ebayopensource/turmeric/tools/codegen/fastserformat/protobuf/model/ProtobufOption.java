/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;

/**
 * @author rkulandaivel
 * 
 */
public class ProtobufOption {
	private ProtobufOptionType m_optionType = null;
	private String m_optionValue = null;

	public ProtobufOptionType getOptionType() {
		return m_optionType;
	}

	public void setOptionType(ProtobufOptionType optionType) {
		this.m_optionType = optionType;
	}

	public String getOptionValue() {
		return m_optionValue;
	}

	public void setOptionValue(String optionValue) {
		this.m_optionValue = optionValue;
	}

}
