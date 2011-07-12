/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeMap;

/**
 * @author rkulandaivel
 *
 */
public class MapperInstanceProvider {
	private ComplexTypeMapper m_complexTypeMapper = null;
	private AttributesMapper m_attributesMapper = null;
	private ElementTypeMapper m_elementTypeMapper = null;
	private SimpleTypeMapper m_simpleTypeMapper = null;

	private MapperInstanceProvider(SchemaTypeMap schemaTypeMap){
		m_complexTypeMapper = new ComplexTypeMapper( schemaTypeMap, this );
		m_attributesMapper =  new AttributesMapper( schemaTypeMap, this );
		m_elementTypeMapper =  new ElementTypeMapper( schemaTypeMap, this );
		m_simpleTypeMapper =  new SimpleTypeMapper( schemaTypeMap, this );
	}
	
	
	public static MapperInstanceProvider createMapperInstanceProvider(SchemaTypeMap schemaTypeMap){
		return new MapperInstanceProvider( schemaTypeMap );
	}


	public ComplexTypeMapper getComplexTypeMapper() {
		return m_complexTypeMapper;
	}
	public SimpleTypeMapper getSimpleTypeMapper() {
		return m_simpleTypeMapper;
	}

	public AttributesMapper getAttributesMapper() {
		return m_attributesMapper;
	}

	public ElementTypeMapper getElementTypeMapper() {
		return m_elementTypeMapper;
	}
}
