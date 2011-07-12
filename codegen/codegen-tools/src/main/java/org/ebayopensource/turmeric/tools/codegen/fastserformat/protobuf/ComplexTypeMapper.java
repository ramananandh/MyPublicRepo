/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Attribute;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroup;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Choice;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ComplexContent;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ComplexType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Extension;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Group;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.GroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Restriction;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaAll;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Sequence;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SequenceElement;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleContent;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleTypeRestriction;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.exception.ProtobufModelGenerationFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufField;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufFieldModifier;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeMap;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeName;

/**
 * The class parses the complex type schema object and creates protobuf message object.
 * The classes understands combinations of various child elements like simple content,
 * complex content, sequence, choice, group and attributes.
 *  
 * @author rkulandaivel
 *
 */
public class ComplexTypeMapper extends BaseSchemaTypeMapper{

	private static final String FIELD_NAME_FOR_SIMPLE_CONTENT_EXTENSION_TYPE = "value";

	public ComplexTypeMapper(SchemaTypeMap schemaTypeMap, MapperInstanceProvider instProvider){
		super( schemaTypeMap, instProvider );
	}

	/**
	 * The method creates protobuf message from the given complex type.
	 * This method understands various combinations of simple content, complex content,
	 * restriction, extension, group, choice and sequence at all level.
	 * Based on the combinations used, the elements are created.
	 * 
	 * In case of attributes, all the attributes in the entire hierarchy  are collected into single map.
	 * So that two attributes defined with same name are resolved as single attribute.
	 * 
	 * If the simple content uses extension, then there might be a chance that two attributes defined with same.
	 * 
	 * Finally, the method validates the uniqueness in the field names.
	 * The validation is required because if both super type and sub type defines field with same name
	 * then proto model cannot differentiate it. Hence this validation is required.
	 *   
	 * @param complexType
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	public ProtobufMessage createProtobufMessage(ComplexType complexType) throws ProtobufModelGenerationFailedException{
		
		ProtobufMessage protoMessage = new ProtobufMessage();
		
		String messageName = MapperUtils.deriveMessageNameFromQName( complexType.getTypeName() );
		protoMessage.setMessageName( messageName );
		protoMessage.setSchemaTypeName( new SchemaTypeName( complexType.getTypeName() ) );

		List<ProtobufField> fields = new ArrayList<ProtobufField>();

		//a map if created here, because when this class goes down to each leaf of the type,
		//all the attributes would be collected into map.
		//finally when all fields are identified, attributes would be converted into fields.
		//the reason for doing this is a simple content can define an attribute with the same name its base type defines.
		Map<QName, Attribute> mapOfAttributes = new HashMap<QName, Attribute>();

		fields.addAll( handleComplexTypeForFields(complexType, mapOfAttributes) );
		
		List<Attribute> attributes = new ArrayList<Attribute>(mapOfAttributes.values());
		List<ProtobufField> attrs = getInstanceProvider().getAttributesMapper().createProtobufFields(complexType, attributes);
		fields.addAll( attrs );
		
		validateUniquenessInFields( complexType, fields );

		protoMessage.getFields().addAll( fields );
		if( complexType.getDocumentation() != null ){
			protoMessage.setMessageComments( complexType.getDocumentation().getContent() );
		}
		return protoMessage;
	}

	/**
	 * This validation is required because if a complex type extends another type.
	 * Both type defines fields with same name irrespective of field type.
	 * This case is not supported in protobuf. Hence exception would be thrown here. 
	 * @param fields
	 * @throws ProtobufModelGenerationFailedException 
	 */
	private void validateUniquenessInFields(ComplexType complexType, List<ProtobufField> fields) throws ProtobufModelGenerationFailedException{
		Map<String, ProtobufField> mapOfFields = new HashMap<String, ProtobufField>();
		for(ProtobufField field : fields){
			String fieldName = field.getFieldName();
			if(mapOfFields.get( fieldName ) != null){
				String message = "Identified duplicate field name '"+ fieldName+"' in type '"+complexType.getTypeName()+"'.";
				message = message + " This could be due to fields declared with same name in base type and sub type";
				throw new ProtobufModelGenerationFailedException(message);
			}else{
				mapOfFields.put(fieldName, field);
			}
		}
	}

	/**
	 * The method understands the possible child elements the complex type has like group, sequence etc
	 * and handles it.
	 * 
	 * @param complexType
	 * @param mapOfAttributes
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	private List<ProtobufField> handleComplexTypeForFields(ComplexType complexType, Map<QName, Attribute> mapOfAttributes) throws ProtobufModelGenerationFailedException{
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		
		if(complexType.hasSimpleContent()){
			fields.addAll( handleSimpleContent(complexType, mapOfAttributes) );
		}

		if(complexType.hasComplexContent() ){
			fields.addAll( handleComplexContent(complexType, mapOfAttributes) );
		}

		if(complexType.hasGroup()){
			fields.addAll( handleGroup(complexType, complexType.getGroup()) );
		}

		if( complexType.hasChoice() ){
			fields.addAll( handleChoice(complexType, complexType.getChoice()) );
		}
		if( complexType.hasSequence() ){
			fields.addAll( handleSequence(complexType, complexType.getSequence()) );
		}

		if( complexType.hasAll() ){
			fields.addAll( handleAll(complexType, complexType.getAll()) );
		}
		List<Attribute> attrs = complexType.getAttributes();
		populateAttributesInMap(mapOfAttributes, attrs);

		List<AttributeGroup> attrGps = complexType.getAttributeGroup();
		populateAttributeGroupsInMap(mapOfAttributes, attrGps);
		return fields;
	}

	/**
	 * This method handles the complex content of the complex type.
	 * 
	 * @param complexType
	 * @param mapOfAttributes
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	private List<ProtobufField> handleComplexContent(ComplexType complexType, Map<QName, Attribute> mapOfAttributes) throws ProtobufModelGenerationFailedException{
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		ComplexContent complexContent = complexType.getComplexContent();
		
		if( complexContent.getExtension() != null ){
			fields.addAll( handleComplexContentExtension(complexType, mapOfAttributes) );
		}
		if( complexContent.getRestriction() != null ){
			fields.addAll( handleComplexContentRestriction(complexType, mapOfAttributes) );
		}
		return fields;
	}

	/**
	 * This method handles the element node.
	 * It uses ElementTypeMapper to create the field.
	 * 
	 * @param complexType
	 * @param seqEl
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	private ProtobufField handleElement(ComplexType complexType, SequenceElement seqEl) throws ProtobufModelGenerationFailedException{
		return getInstanceProvider().getElementTypeMapper().createFieldForElement(complexType, seqEl);
	}

	/**
	 * This method handles the choice node.
	 * For each element the choice has, protobuf Field is created and then
	 * the field modifier is overwritten.
	 * Because, a field can never be required inside the choice tag.
	 * See method compareAndUpdateModifier for algorithmn of overwriting the modifier.
	 * 
	 * @param complexType
	 * @param choiceEl
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	private List<ProtobufField> handleChoice(ComplexType complexType, Choice choiceEl) throws ProtobufModelGenerationFailedException{
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		
		for( SequenceElement element : choiceEl.getElements() ){
			ProtobufField field = handleElement(complexType, element);
			if(field != null){
				//compareAndUpdateModifier(choiceEl, field);
				fields.add( field );
			}
		}

		for ( Choice choice : choiceEl.getChoices() ){
			fields.addAll( handleChoice(complexType, choice) );
		}
		for ( Sequence sequence : choiceEl.getSequences() ){
			fields.addAll( handleSequence(complexType, sequence) );
		}
		for ( Group gp : choiceEl.getGroups() ){
			fields.addAll( handleGroup(complexType, gp) );
		}
		for(ProtobufField field : fields){
			compareAndUpdateModifier(choiceEl, field);
		}
		return fields;
	}

	/**
	 * This method handles the sequence node.
	 * For each element the sequence has, protobuf Field is created and then
	 * the field modifier is overwritten.
	 * Because, the sequence node can also define min occurs and max occurs attribute.
	 * 
	 * See method compareAndUpdateModifier for algorithmn of overwriting the modifier.
	 * It assigns lesser modifier. For example if the element is required and sequence is optional, then field modifier is optional.
	 * 
	 * @param complexType
	 * @param seqEl
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	private List<ProtobufField> handleSequence(ComplexType complexType, Sequence seqEl) throws ProtobufModelGenerationFailedException{
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		for( Sequence.SequenceEntry entry : seqEl.getEntries() ){
			if(entry.isChoice()){
				fields.addAll( handleChoice(complexType, entry.getChoice()) );
			}
			if(entry.isElement()){
				ProtobufField field = handleElement(complexType, entry.getElement());
				if(field != null){
					compareAndUpdateModifier(seqEl, field);
					fields.add( field );
				}
			}
			if(entry.isGroup()){
				fields.addAll( handleGroup(complexType, entry.getGroup()) );
			}
			if(entry.isSequence()){
				fields.addAll( handleSequence(complexType, entry.getSequence()) );
			}
		}
		return fields;
	}

	/**
	 * This method identifies the group ref type and reads the elements from the corresponding group type.
	 * 
	 * @param complexType
	 * @param groupEl
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	private List<ProtobufField> handleGroup(ComplexType complexType, Group groupEl) throws ProtobufModelGenerationFailedException{
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		
		QName groupRef = groupEl.getGroupRef();
		SchemaTypeName groupRefName = new SchemaTypeName( groupRef );
		
		GroupType groupType = getGroupType( groupRefName );
		if(groupType != null){
			if( groupType.getChoice() != null ){
				fields.addAll( handleChoice(complexType, groupType.getChoice()) );
			}
			if( groupType.getSequence() != null ){
				fields.addAll( handleSequence(complexType, groupType.getSequence()) );
			}
			if( groupType.getAll() != null ){
				fields.addAll( handleAll(complexType, groupType.getAll()) );
			}
		}
		return fields;
	}

	private List<ProtobufField> handleAll(ComplexType complexType, SchemaAll allEl)  throws ProtobufModelGenerationFailedException{
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		for( SequenceElement element : allEl.getElements() ){
			ProtobufField field = handleElement(complexType, element);
			if(field != null){
				fields.add( field );
			}
		}
		for(ProtobufField field : fields){
			compareAndUpdateModifier(allEl, field);
		}
		return fields;
	}
	/**
	 * This method handles the complex content restriction.
	 * If restriction is used at complex content, then the base type would be another complex type.
	 * So all the fields from the base complex type would be flattened to the current complex type.
	 * It also handles if sequence, group are used.
	 * 
	 * @param complexType
	 * @param mapOfAttributes
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	private List<ProtobufField> handleComplexContentRestriction(ComplexType complexType, Map<QName, Attribute> mapOfAttributes) throws ProtobufModelGenerationFailedException{
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		Restriction restriction = complexType.getComplexContent().getRestriction();

		//handle base here
		QName complexTypeBase = restriction.getBase();

		//complex content restriction base would definitely be a complex type which can have simple or complex content
		SchemaTypeName schemaTypeName = new SchemaTypeName( complexTypeBase );
		ComplexType baseComplexType = (ComplexType)getComplexOrSimpleType( schemaTypeName );
		fields.addAll( handleComplexTypeForFields(baseComplexType,mapOfAttributes) );


//		if(restriction.getChoice() != null ){
//			fields.addAll( handleChoice(complexType, restriction.getChoice()) );
//		}
//		if( restriction.getGroup() != null){
//			fields.addAll( handleGroup(complexType, restriction.getGroup()) );
//		}
//		if( restriction.getSequence() != null){
//			fields.addAll( handleSequence(complexType, restriction.getSequence()) );
//		}
		
//		if( restriction.getAll() != null ){
//			fields.addAll( handleAll(complexType, restriction.getAll()) );
//		}

		
		
		//no need to populate attributes from a complex content restriction
		//because the jaxb does not generate fields corresponding to attributes present in complex content restriction
		//jaxb generates fields corresponding to attributes only in base types.
		//consider the below example.
		//jaxb generated for baseType1 has fields corresponding to attributes
		//whereas jaxb generated for restrictedType1 does not have fields corresponding to attributes

		
		/*
		 * e.g.
		 * 
		    <xs:complexType name="baseType1">
				<xs:attributeGroup ref="tns:attgroup1"></xs:attributeGroup>
			</xs:complexType>
		
			<xs:complexType name="restrictedType1">
				<xs:complexContent>
					<xs:restriction base ="tns:baseType1">
						<xs:attribute name = "att1" type ="xs:string" use = "required"/>
						<xs:attribute name = "att2" type ="xs:string" use = "prohibited"/>
						<xs:attribute name = "att3" type ="xs:string" use = "optional"/>
					</xs:restriction>
				</xs:complexContent>
			</xs:complexType>
			<xs:attributeGroup name="attgroup1">
				<xs:attribute name = "att1" type ="xs:string" use = "optional"/>
				<xs:attribute name = "att2" type ="xs:string" use = "optional"/>
				<xs:attribute name = "att3" type ="xs:string" use = "optional"/>
				<xs:attribute name = "att4" type ="xs:string" use = "optional"/>
			</xs:attributeGroup>
		 */
		
		
		
//		List<Attribute> attrs = restriction.getAttributeList();
//		populateAttributesInMap(mapOfAttributes, attrs);
//
//		List<AttributeGroup> attrGps = restriction.getAttributeGroup();
//		populateAttributeGroupsInMap(mapOfAttributes, attrGps);

		return fields;
	}

	/**
	 * This method handles the complex content extension.
	 * If extension is used at complex content, then the base type would be another complex type.
	 * So all the fields from the base complex type would be flattened to the current complex type.
	 * It also handles if sequence, group are used.
	 * 
	 * @param complexType
	 * @param mapOfAttributes
	 * @return
	 * @throws ProtobufModelGenerationFailedException
	 */
	private List<ProtobufField> handleComplexContentExtension(ComplexType complexType, Map<QName, Attribute> mapOfAttributes) throws ProtobufModelGenerationFailedException{
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		
		Extension extension = complexType.getComplexContent().getExtension();
		
		//handle base here
		QName complexTypeBase = extension.getBase();

		//complex content extension base would definitely be a complex type which can have simple or complex content
		SchemaTypeName schemaTypeName = new SchemaTypeName( complexTypeBase );
		ComplexType baseComplexType = (ComplexType) getComplexOrSimpleType( schemaTypeName );
		fields.addAll( handleComplexTypeForFields(baseComplexType,mapOfAttributes) );
		
		if(extension.getChoice() != null ){
			fields.addAll( handleChoice(complexType, extension.getChoice()) );
		}
		if( extension.getGroup() != null){
			fields.addAll( handleGroup(complexType, extension.getGroup()) );
		}
		if( extension.getSequence() != null){
			fields.addAll( handleSequence(complexType, extension.getSequence()) );
		}

		if( extension.getAll() != null ){
			fields.addAll( handleAll(complexType, extension.getAll()) );
		}
		List<Attribute> attrs = extension.getAttributeList();
		populateAttributesInMap(mapOfAttributes, attrs);

		List<AttributeGroup> attrGps = extension.getAttributeGroup();
		populateAttributeGroupsInMap(mapOfAttributes, attrGps);

		return fields;
	}

	/**
	 * This method handles the simple content.
	 * It checks the simple content uses restriction or extension.
	 * Accordingly it handles restriction and extension.
	 * 
	 * @param complexType
	 * @param mapOfAttributes
	 * @return
	 * @throws ProtobufModelGenerationFailedException 
	 */
	private List<ProtobufField> handleSimpleContent(ComplexType complexType, Map<QName, Attribute> mapOfAttributes) throws ProtobufModelGenerationFailedException{
		SimpleContent simpleContent = complexType.getSimpleContent();
		List<ProtobufField> fields = new ArrayList<ProtobufField>();

		if( simpleContent.getExtension() != null ){
			fields.addAll( handleSimpleContentExtension(complexType, mapOfAttributes) );
		}
		if( simpleContent.getRestriction() != null ){
			fields.addAll( handleSimpleContentRestriction(complexType, mapOfAttributes) );
		}
		return fields;
	}

	/**
	 * This method handles Simple Content restriction.
	 * The restriction base would definitely be a complex type with simple content
	 * 
	 * @param complexType
	 * @param mapOfAttributes
	 * @return
	 * @throws ProtobufModelGenerationFailedException 
	 */
	private List<ProtobufField> handleSimpleContentRestriction(ComplexType complexType, Map<QName, Attribute> mapOfAttributes) throws ProtobufModelGenerationFailedException{

		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		SimpleContent simpleContent = complexType.getSimpleContent();
		SimpleTypeRestriction restriction = simpleContent.getRestriction();
		
		QName complexTypeBase = restriction.getBase();

		//simple content restriction base would definitely be a complex type which has simple content
		SchemaTypeName schemaTypeName = new SchemaTypeName( complexTypeBase );
		ComplexType baseComplexType = (ComplexType)getComplexOrSimpleType( schemaTypeName );

		if( baseComplexType != null ){
			fields.addAll( handleComplexTypeForFields( baseComplexType, mapOfAttributes) );
		}
		
		//no need to populate attributes from a simple content restriction
		//because the jaxb does not generate fields corresponding to attributes present in simple content restriction
		//jaxb generates fields corresponding to attributes only in base types.
		//consider the below example.
		//jaxb generated for baseComplex has fields corresponding to attributes
		//whereas jaxb generated for ActualComplex does not have fields corresponding to attributes

		
		/*
		 * e.g.
		 * 
			<xs:complexType name="baseComplex">
		    	<xs:simpleContent>
		    		<xs:extension base="tns:baseSimple">
					    	<xs:attribute name = "att1" type ="xs:string" use = "required"/>
							<xs:attribute name = "att2" type ="xs:string" use = "optional"/>
							<xs:attribute name = "att3" type ="xs:string" use = "optional"/>
							<xs:attribute name = "att4" type ="xs:string" use = "optional"/>
		    		</xs:extension>
		    	</xs:simpleContent>
		    </xs:complexType>
		
		    <xs:complexType name="ActualComplex">
		    	<xs:simpleContent>
		    		<xs:restriction base="tns:baseComplex">
		    			<xs:attribute name = "att1" type ="xs:string" use = "required"/>
						<xs:attribute name = "att2" type ="xs:string" use = "prohibited"/>
						<xs:attribute name = "att3" type ="xs:string" use = "optional"/>
		    		</xs:restriction>
		    	</xs:simpleContent>
		    </xs:complexType>
		 */
		
		
		
		
//		List<Attribute> attrs = simpleContent.getRestriction().getAttributes();
//		populateAttributesInMap(mapOfAttributes, attrs);
//
//		List<AttributeGroup> attrGps = simpleContent.getRestriction().getAttributeGroups();
//		populateAttributeGroupsInMap(mapOfAttributes, attrGps);

		return fields;
	}

	/**
	 * This method handles simple content extension.
	 * The extension base would either be a complex type or an inbuilt xsd type.
	 * 
	 * @param complexType
	 * @param mapOfAttributes
	 * @return
	 * @throws ProtobufModelGenerationFailedException 
	 */
	private List<ProtobufField> handleSimpleContentExtension(ComplexType complexType, Map<QName, Attribute> mapOfAttributes) throws ProtobufModelGenerationFailedException{

		List<ProtobufField> fields = new ArrayList<ProtobufField>(); 
		SimpleContent simpleContent = complexType.getSimpleContent();

		QName base = complexType.getSimpleContent().getExtension().getBase();
		
		//the base type can either be a complex type with simple content or a primitive type
		if(InBuiltType2ProtobufTypeMap.isValidInBuiltType(base)){
			ProtobufField baseField = new ProtobufField();
			baseField.setFieldModifier( ProtobufFieldModifier.OPTIONAL );
			baseField.setFieldName( FIELD_NAME_FOR_SIMPLE_CONTENT_EXTENSION_TYPE );
			baseField.setConvertedFieldName( MapperUtils.deriveFieldName( FIELD_NAME_FOR_SIMPLE_CONTENT_EXTENSION_TYPE ) );
			
			baseField.setXsdTypeName( base );
			baseField.setTypeOfField( MapperUtils.getProtobufFieldType( base  ) );
			
			fields.add( baseField );			
		}else{
			SchemaTypeName baseType = new SchemaTypeName( base );
			SchemaType baseSchemaType = getComplexOrSimpleType(baseType);
			
			if( baseSchemaType instanceof ComplexType){
				ComplexType baseComplexType = (ComplexType)baseSchemaType;
				fields.addAll( handleComplexTypeForFields(baseComplexType, mapOfAttributes) );

			}else if( baseSchemaType instanceof SimpleType ){
				SimpleType baseSimpleType = (SimpleType)baseSchemaType;
				fields.addAll( handleSimpleType( baseSimpleType ) );

			}
			
			
			
		}

		
		List<Attribute> attrs = simpleContent.getExtension().getAttributes();
		populateAttributesInMap(mapOfAttributes, attrs);

		List<AttributeGroup> attrGps = simpleContent.getExtension().getAttributeGroups();
		populateAttributeGroupsInMap(mapOfAttributes, attrGps);


		return fields;
	}
	
	private List<ProtobufField> handleSimpleType(SimpleType simpleType){
		List<ProtobufField> fields = new ArrayList<ProtobufField>();
		
		ProtobufField field = new ProtobufField();
		
		field.setFieldModifier( ProtobufFieldModifier.OPTIONAL );
		field.setFieldName( FIELD_NAME_FOR_SIMPLE_CONTENT_EXTENSION_TYPE );
		field.setConvertedFieldName( MapperUtils.deriveFieldName( FIELD_NAME_FOR_SIMPLE_CONTENT_EXTENSION_TYPE ) );

		getInstanceProvider().getElementTypeMapper().populateFieldFromSimpleType(simpleType, field);
		
		fields.add(field);
		return fields;
	}


	private void populateAttributeGroupsInMap(Map<QName, Attribute> mapOfAttributes, List<AttributeGroup> attributeGps){
		for( AttributeGroup attr : attributeGps){
			AttributeGroupType gpType = getArrtibuteGroupType( attr );
			
			populateAttributesInMap(mapOfAttributes, gpType.getAttributes());
		}
	}
	private void populateAttributesInMap(Map<QName, Attribute> mapOfAttributes, List<Attribute> attributes){
		for( Attribute attr : attributes){
			if( attr.getUse() == Attribute.AttributeUse.PROHIBHITED ){
				continue;
			}
			if( attr.getAttributeQName() != null ){
				mapOfAttributes.put(attr.getAttributeQName(), attr);
			}else{
				mapOfAttributes.put( attr.getAttributeRef() , attr);
			}
		}
	}

	/**
	 * Following method compares choice tag and element tag for attributes like min occurs and maxoccurs
	 * and decides the modifier for field. 
	 * If an element is defined inside choice tag, then the modifier can only be either repeated or optional.
	 * It can never be Required.
	 * 
	 * Uses following logic to compare.
	 * If either element tag or choice tag is repeated then the final value taken is repeated.
	 * If none of them is repeated, then value taken is optional. 
	 * @param choiceEl
	 * @param field
	 */
	private void compareAndUpdateModifier(Choice choiceEl, ProtobufField field){
		
		ProtobufFieldModifier oldValue = field.getFieldModifier();
		ProtobufFieldModifier newValue = MapperUtils.getModifier(choiceEl.getMinOccurs(), choiceEl.getMaxOccurs());
		
		if ( (ProtobufFieldModifier.REPEATED == newValue) || (ProtobufFieldModifier.REPEATED == oldValue) ) {
			field.setFieldModifier( ProtobufFieldModifier.REPEATED );

		}else {
			field.setFieldModifier( ProtobufFieldModifier.OPTIONAL );
		}
	}

	/**
	 * Following method compares sequence tag and element tag for attributes like min occurs and maxoccurs
	 * and decides the modifier for the field. 
	 * 
	 * Uses following logic to compare.
	 * If either element tag or choice tag is repeated then the final value taken is repeated.
	 * If none of them is repeated and if either of them is optional then final value is optional.
	 * Final value would be required only if both of them is required.
	 *  
	 * @param seqEl
	 * @param field
	 */
	private void compareAndUpdateModifier(Sequence seqEl, ProtobufField field){
		
		ProtobufFieldModifier oldValue = field.getFieldModifier();
		ProtobufFieldModifier newValue = MapperUtils.getModifier(seqEl.getMinOccurs(), seqEl.getMaxOccurs());
		
		if ( (ProtobufFieldModifier.REPEATED == newValue) || (ProtobufFieldModifier.REPEATED == oldValue) ) {
			field.setFieldModifier( ProtobufFieldModifier.REPEATED );
		}else if( (ProtobufFieldModifier.OPTIONAL == newValue) || (ProtobufFieldModifier.OPTIONAL == oldValue) ){
			field.setFieldModifier( ProtobufFieldModifier.OPTIONAL );
		}else if( (ProtobufFieldModifier.REQUIRED == newValue) && (ProtobufFieldModifier.REQUIRED == oldValue) ){
			field.setFieldModifier( ProtobufFieldModifier.REQUIRED );
		}
	}
	
	/**
	 * Updates the modifier for an element present inside all.
	 * Schema definition says, an element which is inside all, cannot have maxoccurs > 1.
	 * So the modifier of an element can either be Required or OPTIONAL.
	 * If it is required, all has minoccurs=0, then make the modifier as OPTIONAL
	 * 
	 * @param all
	 * @param field
	 */
	private void compareAndUpdateModifier(SchemaAll all, ProtobufField field){
		
		ProtobufFieldModifier oldValue = field.getFieldModifier();
		ProtobufFieldModifier newValue = MapperUtils.getModifier(all.getMinOccurs(), all.getMaxOccurs());
		
		if ( (ProtobufFieldModifier.OPTIONAL == newValue) && (ProtobufFieldModifier.REQUIRED == oldValue) ) {
			field.setFieldModifier( ProtobufFieldModifier.OPTIONAL );

		}
	}
}
