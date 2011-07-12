/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Attribute;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroup;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ElementType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.GroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeMap;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeName;

/**
 * @author rkulandaivel
 * 
 * This is base abstract class for all Mapper types.
 * This class has util methods to access SchemaTypeMap.
 *  
 */
public abstract class BaseSchemaTypeMapper {
	private SchemaTypeMap m_schemaTypeMap = null;
	private MapperInstanceProvider m_instProvider = null;

	public BaseSchemaTypeMapper(SchemaTypeMap schemaTypeMap, MapperInstanceProvider instProvider){
		this.m_schemaTypeMap = schemaTypeMap;
		this.m_instProvider = instProvider;
	}

	public MapperInstanceProvider getInstanceProvider(){
		return m_instProvider;
	}

	public SchemaTypeMap getSchemaTypeMap(){
		return this.m_schemaTypeMap;
	}
	
	/**
	 * Return the complex type or simple type
	 * 
	 * @param typeName
	 * @return
	 */
	public SchemaType getComplexOrSimpleType(SchemaTypeName typeName){
		return getSchemaTypeMap().getComplexOrSimpleType(typeName);
	}

	/**
	 * Return the element type.
	 * @param typeName
	 * @return
	 */
	public ElementType getElementType(SchemaTypeName typeName){
		return (ElementType)getSchemaTypeMap().getElementType(typeName);
	}
	
	/**
	 * Return the attribute group type
	 * 
	 * @param typeName
	 * @return
	 */
	public AttributeGroupType getArrtibuteGroupType(SchemaTypeName typeName){
		return (AttributeGroupType)getSchemaTypeMap().getArrtibuteGroupType(typeName);
	}
	
	/**
	 * Return the group type.
	 * 
	 * @param typeName
	 * @return
	 */
	public GroupType getGroupType(SchemaTypeName typeName){
		return (GroupType)getSchemaTypeMap().getGroupType(typeName);
	}
	
	/**
	 * Return the attribute type.
	 * 
	 * @param typeName
	 * @return
	 */
	public Attribute getArrtibuteType(SchemaTypeName typeName){
		return (Attribute)getSchemaTypeMap().getArrtibuteType(typeName);
	}
	/**
	 * This method finds the attribute group type based on the given attribute group ref.
	 * 
	 * @param schemaTypeMap
	 * @param attributeGroup
	 * @return
	 */
	public AttributeGroupType getArrtibuteGroupType( AttributeGroup attributeGroup ){
		QName ref = attributeGroup.getGroupRef();
		if( ref == null ){
			return null;
		}

		SchemaTypeName refName = new SchemaTypeName( ref );
		return getArrtibuteGroupType( refName );
	}
}
