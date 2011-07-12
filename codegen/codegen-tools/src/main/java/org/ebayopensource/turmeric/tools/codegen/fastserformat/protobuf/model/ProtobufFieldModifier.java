/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;


/**
 * @author rkulandaivel
 *
 */
public enum ProtobufFieldModifier {
	OPTIONAL("optional"),
	REQUIRED("required"),
	REPEATED("repeated");
	
	
	private String value = null;
	ProtobufFieldModifier(String value){
		this.value = value;
	}
	
    public String value() {
        return value;
    }

	public static ProtobufFieldModifier fromValue(String value){
		for(ProtobufFieldModifier use : values()){
			if(use.value.equals(value)){
				return use;
			}
		}
        throw new IllegalArgumentException(value);
	}

}
