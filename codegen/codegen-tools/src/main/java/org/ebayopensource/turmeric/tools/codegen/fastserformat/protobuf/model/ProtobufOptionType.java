/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;

/**
 * @author rkulandaivel
 *
 */
public enum ProtobufOptionType {
	JAVA_PACKAGE_NAME("java_package"),
	JAVA_OUTER_CLASS_NAME("java_outer_classname"),
	OPTIMIZE_FOR("optimize_for");
	
	
	
	private String value = null;
	ProtobufOptionType(String value){
		this.value = value;
	}
	
    public String value() {
        return value;
    }

	public static ProtobufOptionType fromValue(String value){
		for(ProtobufOptionType type : values()){
			if(type.value.equals(value)){
				return type;
			}
		}
        throw new IllegalArgumentException(value);
	}
}
