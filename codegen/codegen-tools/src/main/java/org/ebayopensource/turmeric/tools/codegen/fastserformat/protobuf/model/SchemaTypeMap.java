/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Attribute;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ComplexType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ElementType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.GroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleType;

/**
 * @author rkulandaivel
 *
 * This class is holds various schema type map.
 * The types needs to be holded different Map for each type
 * because a complex type name can be same as element name 
 * which again can be same for group type name which again can be same for attribute.
 * 
 *  This is the reason separate Map needs to be maintained.
 */
public class SchemaTypeMap {

	private Map<SchemaTypeName, SchemaType> m_allComplexAndSimpleTypesMap = new HashMap<SchemaTypeName, SchemaType>();
	private Map<SchemaTypeName, SchemaType> m_allAttributeTypesMap = new HashMap<SchemaTypeName, SchemaType>();
	private Map<SchemaTypeName, SchemaType> m_allAttributeGroupTypesMap = new HashMap<SchemaTypeName, SchemaType>();
	private Map<SchemaTypeName, SchemaType> m_allGroupTypesMap = new HashMap<SchemaTypeName, SchemaType>();
	private Map<SchemaTypeName, SchemaType> m_allElementTypesMap = new HashMap<SchemaTypeName, SchemaType>();
	
	private SchemaTypeMap(){
		
	}
	public static SchemaTypeMap createSchemaTypeMapFromList(List<SchemaType> schemaTypes){
		
		SchemaTypeMap map = new SchemaTypeMap();
		
		for(SchemaType schemaType : schemaTypes){
			SchemaTypeName typeName = new SchemaTypeName(schemaType.getTypeName());
			if( schemaType instanceof ComplexType ){
				map.m_allComplexAndSimpleTypesMap.put(typeName, schemaType);

			}else if( schemaType instanceof SimpleType ){
				map.m_allComplexAndSimpleTypesMap.put(typeName, schemaType);

			}else if( schemaType instanceof AttributeGroupType ){
				map.m_allAttributeGroupTypesMap.put(typeName, schemaType);

			}else if( schemaType instanceof ElementType ){
				map.m_allElementTypesMap.put(typeName, schemaType);

			}else if( schemaType instanceof Attribute ){
				map.m_allAttributeTypesMap.put(typeName, schemaType);

			}else if( schemaType instanceof GroupType ){
				map.m_allGroupTypesMap.put(typeName, schemaType);

			}

		}

		return map;
	}

	public Map<SchemaTypeName, SchemaType> getAllComplexAndSimpleTypes(){
		return m_allComplexAndSimpleTypesMap;
	}

	public SchemaType getComplexOrSimpleType(SchemaTypeName typeName){
		return m_allComplexAndSimpleTypesMap.get(typeName);
	}

	public Map<SchemaTypeName, SchemaType> getAllElementTypes(){
		return m_allElementTypesMap;
	}
	
	public SchemaType getElementType(SchemaTypeName typeName){
		return m_allElementTypesMap.get(typeName);
	}
	
	public SchemaType getArrtibuteGroupType(SchemaTypeName typeName){
		return m_allAttributeGroupTypesMap.get(typeName);
	}
	
	public SchemaType getArrtibuteType(SchemaTypeName typeName){
		return m_allAttributeTypesMap.get(typeName);
	}
	public SchemaType getGroupType(SchemaTypeName typeName){
		return m_allGroupTypesMap.get(typeName);
	}
}
