package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Attribute;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ComplexType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ElementType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.GroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Parser;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Schema;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleType;

/**
 * Builds schema type information from WSDL and classifies them into different maps   
 *
 */
public class QnameMapBuilder
{
    private Map<QName, SchemaType> elementMap = new HashMap<QName, SchemaType>();
    private Map<QName, SchemaType> groupMap = new HashMap<QName, SchemaType>();
    private Map<QName, SchemaType> attributeMap = new HashMap<QName, SchemaType>();
    private Map<QName, SchemaType> attributeGroupMap = new HashMap<QName, SchemaType>();
    private Map<QName, SchemaType> schemaMap = new HashMap<QName, SchemaType>();
    
    public QnameMapBuilder(Definition definition)
    {
        buildQnameMap(definition);
    }
    
    private Map<QName, SchemaType> buildQnameMap(Definition definition)
    {
        Map<QName, SchemaType> result = new HashMap<QName, SchemaType>();
        try
        {
            List<?> schemaList = new ArrayList<Object>();
            Parser.getTypesSchemas(definition, schemaList, null);
            Parser.getAllSchemaTypes(definition, schemaList, null);
            
            for (Object object : schemaList)
            {
                if (object instanceof Schema)
                {
                    Schema schema = (Schema) object;
                    List<SchemaType> schemaTypes = schema.getTypes();
                    for (SchemaType schemaType : schemaTypes)
                    {
                        QName typeName = schemaType.getTypeName();
                        if((schemaType instanceof SimpleType) || (schemaType instanceof ComplexType))
                        {
                            schemaMap.put(typeName, schemaType);    
                        }
                        else if(schemaType instanceof Attribute)
                        {
                            attributeMap.put(typeName, schemaType);
                        }
                        else if(schemaType instanceof AttributeGroupType)
                        {
                            attributeGroupMap.put(typeName, schemaType);
                        }
                        else if (schemaType instanceof GroupType)
                        {
                            groupMap.put(typeName, schemaType);
                        }
                        else if (schemaType instanceof ElementType)
                        {
                            elementMap.put(typeName, schemaType);
                        }
                    }
                }
            }
            return result;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public Map<QName, SchemaType> getElementMap()
    {
        return Collections.unmodifiableMap(elementMap) ;
    }


    public Map<QName, SchemaType> getGroupMap()
    {
        return Collections.unmodifiableMap(groupMap);
    }


    public Map<QName, SchemaType> getSchemaMap()
    {
        return Collections.unmodifiableMap(schemaMap);
    }

    public Map<QName, SchemaType> getAttributeMap()
    {
        return Collections.unmodifiableMap(attributeMap);
    }

    public Map<QName, SchemaType> getAttributeGroupMap()
    {
        return Collections.unmodifiableMap(attributeGroupMap);
    }
    
}