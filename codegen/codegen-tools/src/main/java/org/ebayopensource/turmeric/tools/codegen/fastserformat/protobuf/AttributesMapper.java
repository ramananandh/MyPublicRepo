/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Attribute;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroup;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.exception.ProtobufModelGenerationFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufField;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufFieldModifier;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeMap;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeName;

/**
 * This class parses the attribute schema object and creates protobuf field. 
 * @author rkulandaivel
 *
 */
public class AttributesMapper extends BaseSchemaTypeMapper{

	public AttributesMapper(SchemaTypeMap schemaTypeMap, MapperInstanceProvider instProvider){
		super( schemaTypeMap, instProvider );
	}

	/**
	 * The method creates list of protobuf fields for the given list of attributes.
	 * 
	 * @param enclosingType
	 * @param attributes
	 * @return
	 * @throws ProtobufModelGenerationFailedException 
	 */
	public List<ProtobufField> createProtobufFields(SchemaType enclosingType, List<Attribute> attributes) throws ProtobufModelGenerationFailedException{
		
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		for( Attribute attr : attributes){
			ProtobufField attrField = createProtobufField( enclosingType, attr );
			if( attrField != null){
				fields.add( attrField );
			}
		}
		return fields;
	}

	/**
	 * The method creates list of protobuf fields for the given list of attribute groups.
	 * 
	 * @param enclosingType
	 * @param attributeGroups
	 * @return
	 * @throws ProtobufModelGenerationFailedException 
	 */
	public List<ProtobufField> createProtobufFieldsForAttributeGroup(SchemaType enclosingType, List<AttributeGroup> attributeGroups) throws ProtobufModelGenerationFailedException{
		
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		for( AttributeGroup attrGp : attributeGroups){
			
			AttributeGroupType groupType = getArrtibuteGroupType( attrGp );
			if(groupType == null){
				continue;
			}
			
			fields.addAll( createProtobufFields( enclosingType, groupType.getAttributes() ) );

		}
		return fields;
	}

	/**
	 * The method creates list of protobuf field for the given attribute.
	 * 
	 * @param enclosingType
	 * @param attribute
	 * @return
	 * @throws ProtobufModelGenerationFailedException 
	 */
	public ProtobufField createProtobufField(SchemaType enclosingType, Attribute attribute) throws ProtobufModelGenerationFailedException{
		if( attribute.getUse() ==  Attribute.AttributeUse.PROHIBHITED ){
			return null;
		}
		if( attribute.getAttributeRef() != null &&  attribute.getAttributeQName() == null ){
			SchemaTypeName ref = new SchemaTypeName( attribute.getAttributeRef() );
			Attribute refType = getArrtibuteType(ref);
			if( refType== null ){
				return null;
			}
			ProtobufField field = createProtobufField( enclosingType, (Attribute)refType );
			
			//update with the modifier of the original attribute which refers.
			//the attribute which is being referred is definitely  "optional" modifier, because
			//the attribute tag at root level is not applicable for attribute 'use'.
			//so overwrite with the modifier of referring attribute.
			field.setFieldModifier( getProtobufFieldModifier( attribute ) );
			return field;
		}
		ProtobufField attrField = new ProtobufField();
		attrField.setFieldModifier( getProtobufFieldModifier( attribute ) );

		String fieldName = getFieldNameForAttribute( enclosingType, attribute );
		attrField.setFieldName( fieldName );
		attrField.setConvertedFieldName( MapperUtils.deriveFieldName( attribute.getAttributeQName().getLocalPart() ) );
		
		if(attribute.getValueType() == null){
			throw new ProtobufModelGenerationFailedException("The type of attribute cannot be null");
		}

		getInstanceProvider().getElementTypeMapper().traceTypeAndPopulateTheField( attribute.getValueType() , attrField);
		
		return attrField;
	}


	/**
	 * Decide the field modifier based on attribute use.
	 * First check whether it is a special type, if so check method getModifierForSpecialInBuiltType.
	 * else derive based on AttributeUse.
	 * 
	 * @param attribute
	 * @return
	 */
	private ProtobufFieldModifier getProtobufFieldModifier(Attribute attribute){
		ProtobufFieldModifier modifier = MapperUtils.getModifierForSpecialInBuiltType( attribute.getValueType() );
		if( modifier != null){
			return modifier;
		}

		if( attribute.getUse() == Attribute.AttributeUse.REQUIRED ){
			return ProtobufFieldModifier.REQUIRED;
		}
		if( attribute.getUse() == Attribute.AttributeUse.OPTIONAL ){
			return ProtobufFieldModifier.OPTIONAL;
		}
		return null;
	}

	private String getFieldNameForAttribute(SchemaType enclosingType, Attribute attribute){
		String fieldName = attribute.getAttributeQName().getLocalPart();
		
		fieldName = enclosingType.getTypeName().getLocalPart() + "_" + fieldName;
		
		return fieldName;
	}
}
