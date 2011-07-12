/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import java.util.List;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.RestrictionEnumeration;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufEnumEntry;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufEnumMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeMap;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeName;

/**
 * @author rkulandaivel
 *
 */
public class SimpleTypeMapper extends BaseSchemaTypeMapper{
	private static final String ENUM_MESSAGE_NAME_SUFFIX = "Enum";
	public SimpleTypeMapper(SchemaTypeMap schemaTypeMap, MapperInstanceProvider instProvider){
		super( schemaTypeMap, instProvider );
	}

	public ProtobufEnumMessage createEnumProtoMessage(SchemaTypeName schemaTypeName, SimpleType simpleType){
		ProtobufEnumMessage enumMessage = new ProtobufEnumMessage();
		
		String messageName = MapperUtils.deriveMessageNameFromQName( simpleType.getTypeName() );
		enumMessage.setMessageName( messageName );
		enumMessage.setEnumMessageName( messageName + ENUM_MESSAGE_NAME_SUFFIX );
		enumMessage.setEnumType(true);
		enumMessage.setSchemaTypeName(schemaTypeName);
		
		List<RestrictionEnumeration> enumerations = simpleType.getRestriction().getEnumerations();
		for(RestrictionEnumeration enumeration : enumerations){
			ProtobufEnumEntry enumEntry = new ProtobufEnumEntry();

			//uses the utility method used by jaxb to derive the enum constant name for the given enum name.
			//This is done to achieve the enum name in dot proto file is same as the enum name in jaxb class.
			enumEntry.setEnumValue( MapperUtils.deriveEnumConstantName( enumeration.getEnumValue() ) );
			enumEntry.setXsdEnumValue( enumeration.getEnumValue() );

			if(enumeration.getDocumentation() != null){
				enumEntry.setFieldComments( enumeration.getDocumentation().getContent() );
			}
			
			enumMessage.getEnumEntries().add(enumEntry);
		}
		
		
		return enumMessage;
	}

}
