/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ComplexType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ElementType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.exception.ProtobufModelGenerationFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufField;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufFieldModifier;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufFieldType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeMap;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeName;

/**
 * This class parses element tag of the complex type and creates a protobuf field.
 * The class understands the various possibilities of value type and creates field from it.
 * @author rkulandaivel
 *
 */
public class ElementTypeMapper extends BaseSchemaTypeMapper{
	public ElementTypeMapper(SchemaTypeMap schemaTypeMap, MapperInstanceProvider instProvider){
		super( schemaTypeMap, instProvider );
	}

	/**
	 * The method can return null value also.
	 * It returns null if both minoccurs and max occurs are zero.
	 * 
	 * @param complexType
	 * @param seqEl
	 * @return
	 * @throws ProtobufModelGenerationFailedException 
	 */
	public ProtobufField createFieldForElement(ComplexType complexType, ElementType seqEl) throws ProtobufModelGenerationFailedException{
		if( (seqEl.getMaxOccurs() == 0) && (seqEl.getMinOccurs() == 0) ){
			//both values zero is a valid combination
			return null;
		}

		
		
		if( seqEl.getRef() != null &&  seqEl.getTypeName() == null ){
			ElementType refElement = getElementType( new SchemaTypeName(seqEl.getRef() ) );

			ProtobufField field = createFieldForElement( complexType, (ElementType)refElement );
				
			//update with the modifier of the original element which refers.
			//the element which is being referred is definitely  "required" modifier, because
			//the element tag at root level is not applicable for attributes minoccurs and maxoccurs.
			//so blindly overwrite.
			field.setFieldModifier( getModifierForElement( seqEl ) );
			return field;

		}else if( seqEl.getRef() == null &&  seqEl.getElementType() == null ){
			if( seqEl.getSimpleType() == null && seqEl.getComplexType() == null ){
				throw new ProtobufModelGenerationFailedException("The type of field cannot be null");	
			}
		}

		ProtobufField field = new ProtobufField();
		field.setFieldName( seqEl.getTypeName().getLocalPart() );
		field.setConvertedFieldName( MapperUtils.deriveFieldName( field.getFieldName() ) );
		
		traceTypeAndPopulateTheField(seqEl, field);
		
		field.setNillable( seqEl.isNillable() );

		if( seqEl.getDocumentation() != null){
			field.setFieldComments( seqEl.getDocumentation().getContent() );
		}

		return field;
	}


	private void traceTypeAndPopulateTheField(ElementType seqEl, ProtobufField field){
		field.setFieldModifier( getModifierForElement( seqEl ) );
		QName typeName = seqEl.getElementType();
		
		//typeName passed can be an anonymous type.
		if( typeName == null && seqEl.hasSimpleType() ){
			populateFieldFromSimpleType(seqEl.getSimpleType(), field);

		}else if( typeName == null && seqEl.hasComplexType() ){
			populateFieldFromComplexType( seqEl.getComplexType(), field );

		}else if( typeName != null ){
			traceTypeAndPopulateTheField(typeName, field);
		}
	}

	public void populateFieldFromComplexType(ComplexType type, ProtobufField field){
		field.setXsdTypeName( type.getTypeName() );
		field.setTypeOfField( ProtobufFieldType.COMPLEX_TYPE );
	}
	
	/**
	 * This method traces a particular type and populates the field object with two values
	 * 1. xsd type, 2. type of field. and 3. FieldModifier 
	 * 
	 * Following algorithmn is used.
	 * An element might refer to an inbuilt type or an user defined type (simple type or complex type)
	 * If it is a inbuilt type,
	 * 		xsd type name --> in built type name
	 * 		type of field --> uses MapperUtils.getProtobufFieldType
	 * If it is a Complex type
	 * 		xsd type name --> type name
	 * 		type of field --> USER_DEFINED_TYPE
	 * If it is simple type and it is enum,
	 * 		xsd type name --> type name
	 * 		type of field --> ENUM_TYPE
	 * If it is simple type and it uses restriction but not enums
	 * 		xsd type name --> actual inbuilt type name
	 * 		type of field --> uses MapperUtils.getProtobufFieldType
	 * It it is simple type and it uses list base
	 * 		xsd type name --> actual inbuilt item base name
	 * 		type of field --> uses MapperUtils.getProtobufFieldType
	 * 
	 * 
	 * Field Modifier is derived from method getModifierForElement.
	 */
	public void traceTypeAndPopulateTheField(QName typeName, ProtobufField field){

		SchemaTypeName schemaTypeName = new SchemaTypeName( typeName );
		SchemaType schemaType =  getComplexOrSimpleType( schemaTypeName );
		
		//if not null it means either user defined simple type or complex type
		if( schemaType != null){
			if( schemaType instanceof SimpleType ){
				SimpleType simpleType = (SimpleType)schemaType;
				populateFieldFromSimpleType(simpleType, field);

				
			}else if( schemaType instanceof ComplexType ){
				populateFieldFromComplexType( (ComplexType)schemaType, field );

			}

			
		}else{
			populateFieldFromSimpleType( typeName, field, null );
			
		}
	}

	
	public void populateFieldFromSimpleType(QName simpleType, ProtobufField field, SimpleType surroundingSimpleType){
		SchemaTypeName schemaTypeName = new SchemaTypeName( simpleType );
		SimpleType schemaType = (SimpleType) getComplexOrSimpleType( schemaTypeName );
		if(schemaType != null){
			populateFieldFromSimpleType(schemaType, field);

		}else{
			field.setXsdTypeName( MapperUtils.classifyBuiltInType( simpleType, surroundingSimpleType ) );
			field.setTypeOfField( MapperUtils.getProtobufFieldType(simpleType) );

			//update the modifier only if it is a special case
			ProtobufFieldModifier modifier = MapperUtils.getModifierForSpecialInBuiltType(simpleType);
			if( modifier != null){
				field.setFieldModifier(modifier);
			}
		}
	}
	public void populateFieldFromSimpleType(SimpleType simpleType, ProtobufField field){
		//if enumerations defined, then it is a enum type
		if( simpleType.getRestriction() != null && simpleType.getRestriction().getEnumerations().size() > 0 ){
			field.setTypeOfField( ProtobufFieldType.ENUM_TYPE );
			field.setXsdTypeName( simpleType.getTypeName() );

		}else if( simpleType.getRestriction() != null){
			//get the base type which is gonna be the actual type
			//but base type can be either a built-in simple type or a user defined simple type
			QName baseType = simpleType.getRestriction().getBase();
			populateFieldFromSimpleType( baseType,  field, simpleType);

		}else if( simpleType.getList() != null ){
			//get the item type which is gonna be the actual type
			//also if list is used, the element type is repeated

			QName itemType = simpleType.getList().getItemType();
			populateFieldFromSimpleType( itemType,  field, simpleType);

			field.setFieldModifier( ProtobufFieldModifier.REPEATED );
		}
	}

	private ProtobufFieldModifier getModifierForElement(ElementType seqEl){
		return MapperUtils.getModifier(seqEl.getMinOccurs(), seqEl.getMaxOccurs());
	}
}
