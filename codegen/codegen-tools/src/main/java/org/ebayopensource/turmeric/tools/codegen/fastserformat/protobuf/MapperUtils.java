package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import java.math.BigDecimal;
import java.util.Map;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.codegen.external.WSDLUtil;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroup;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleTypeRestriction;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufFieldModifier;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufFieldType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeName;

import com.sun.codemodel.JJavaName;

public class MapperUtils {

    public static final BigDecimal LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);
    public static final BigDecimal LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);
    public static final BigDecimal INT_MIN = BigDecimal.valueOf(Integer.MIN_VALUE);
    public static final BigDecimal INT_MAX = BigDecimal.valueOf(Integer.MAX_VALUE);

	/**
	 * This method is used by all types protobuf model mapper to derive the message name. 
	 * @param typeName
	 * @return
	 */
	public static String deriveMessageNameFromQName(QName typeName){
		return WSDLUtil.getXMLIdentifiersClassName(typeName.getLocalPart());
	}

	public static String deriveFieldName(String fieldName){
		//lets say the element names in WSDL are thisIsXYName and iNAME
		//Our aim is create proto field name same as field name in jaxb POJO.
		//JAXB first converts each element name into a java property name.( result: ThisIsXYName, INAME )
		//it then converts property name into getter method name, setter method name, field name.
		//( result of field name : thisIsXYName and iname )
		//hence here same util methods used by JAXB are called.

		String derivedFieldName = com.sun.xml.bind.api.impl.NameConverter.standard.toPropertyName(fieldName);

		derivedFieldName = com.sun.xml.bind.api.impl.NameConverter.standard.toVariableName( derivedFieldName );
		if(!JJavaName.isJavaIdentifier ( derivedFieldName )){
			derivedFieldName = '_'+derivedFieldName; // avoid colliding with the reserved names like 'abstract'.
		}
		return derivedFieldName;
	}

	/**
	 * This method uses the utility method used by jaxb to derive the enum constant name for the given enum name.
	 * This is done to achieve the enum name in dot proto file is same as the enum name in jaxb class.
	 * @param enumName
	 * @return
	 */
	public static String deriveEnumConstantName(String enumName){
		return com.sun.xml.bind.api.impl.NameConverter.standard.toConstantName(enumName);
	}
	/**
	 * This method finds the attribute group type based on the given attribute group ref.
	 * 
	 * @param schemaTypeMap
	 * @param attributeGroup
	 * @return
	 */
	public static AttributeGroupType findAttributeGroupType( Map<SchemaTypeName, SchemaType> schemaTypeMap, AttributeGroup attributeGroup ){
		QName ref = attributeGroup.getGroupRef();
		if( ref == null ){
			return null;
		}

		SchemaTypeName refName = new SchemaTypeName( ref );
		return (AttributeGroupType)schemaTypeMap.get( refName );
	}

	/**
	 * This method can only be used if it is a valid xsd type.
	 * This method returns the type of field type.
	 * @param type
	 * @return
	 */
	public static ProtobufFieldType getProtobufFieldType(QName type){
		ProtobufFieldType fieldType = null;
		if(InBuiltType2ProtobufTypeMap.isValidInBuiltType(type)){
			if( InBuiltType2ProtobufTypeMap.QNAME_TYPE.equals(type) ){
				fieldType = ProtobufFieldType.QNAME_TYPE;

			}else if( InBuiltType2ProtobufTypeMap.DURATION_TYPE.equals(type) ){
				fieldType = ProtobufFieldType.DURATION_TYPE;

			}else if( InBuiltType2ProtobufTypeMap.DATE_TYPE.equals(type) 
					|| InBuiltType2ProtobufTypeMap.DATETIME_TYPE.equals(type)
					|| InBuiltType2ProtobufTypeMap.TIME_TYPE.equals(type)
					|| InBuiltType2ProtobufTypeMap.GYEARMONTH_TYPE.equals(type)
					|| InBuiltType2ProtobufTypeMap.GYEAR_TYPE.equals(type)
					|| InBuiltType2ProtobufTypeMap.GMONTHDAY_TYPE.equals(type)
					|| InBuiltType2ProtobufTypeMap.GDAY_TYPE.equals(type)
					|| InBuiltType2ProtobufTypeMap.GMONTH_TYPE.equals(type)
																){
				fieldType = ProtobufFieldType.DATE_TYPE;

			}else{
				fieldType = ProtobufFieldType.INBUILT_TYPE;
			}
			
		}
		
		return fieldType;
	}

	/**
	 * This method decides the modifier for the given min occurs and max occurs combination.
	 * However, this is not final. because an element with minoccurs=1 and maxoccurs=1 is required if it
	 * is present inside sequence tag but is optional if it is present inside choice tag.
	 * 
	 *  So the complex type mapper, takes care of overwriting the field modifier.
	 *  
	 * @param minOccurs
	 * @param maxOccurs
	 * @return
	 */
	public static ProtobufFieldModifier getModifier(int minOccurs, int maxOccurs){
		if(maxOccurs == 1 && minOccurs == 0){
			return ProtobufFieldModifier.OPTIONAL;
		}
		if(maxOccurs == 1 && minOccurs == 1){
			return ProtobufFieldModifier.REQUIRED;
		}
		if(maxOccurs > 1 && minOccurs >= 0){
			return ProtobufFieldModifier.REPEATED;
		}
		return ProtobufFieldModifier.OPTIONAL;
	}

	/**
	 * Returns value only for special in-built types which are by default list like nmtokens.
	 * 
	 * @param type
	 * @return
	 */
	public static ProtobufFieldModifier getModifierForSpecialInBuiltType(QName type){
		if( InBuiltType2ProtobufTypeMap.NMTOKENS_TYPE.equals(type) ){
			return ProtobufFieldModifier.REPEATED;
		}
		if( InBuiltType2ProtobufTypeMap.ENTITIES_TYPE.equals(type) ){
			return ProtobufFieldModifier.REPEATED;
		}
		return null;
	}

	/**
	 * This method classifies the built in type especially types like Integer or Long.
	 * If the simple type is configured with facets like minExclusive, maxExclusive, minInclusive or maxInclusive
	 * then based on the values configured the types are determined. 
	 * 
	 * @param builtInType
	 * @param surroundingSimpleType
	 * @return
	 */
	public static QName classifyBuiltInType(QName builtInType, SimpleType surroundingSimpleType ){

		QName INTEGER_TYPE = InBuiltType2ProtobufTypeMap.INTEGER_TYPE;
		QName LONG_TYPE = InBuiltType2ProtobufTypeMap.LONG_TYPE;
		QName INT_TYPE = InBuiltType2ProtobufTypeMap.INT_TYPE;
		QName clasifiedType = builtInType;
		
		if( surroundingSimpleType == null ){
			//nothing to do
		}else if( INTEGER_TYPE.equals( builtInType ) || LONG_TYPE.equals( builtInType ) ){

			SimpleTypeRestriction sTypeRest = surroundingSimpleType.getRestriction();
			if( sTypeRest != null ){
				BigDecimal xe = null, xi = null;
				if( sTypeRest.hasMaxExclusive() ){
					xe = sTypeRest.getMaxExclusive().subtract( BigDecimal.ONE ) ;
				}
				if( sTypeRest.hasMaxInclusive() ){
					xi = sTypeRest.getMaxInclusive();
				}
	            BigDecimal max = min(xe,xi);    // most restrictive one takes precedence

	            if(max!=null) {
	            	BigDecimal ne = null, ni = null;
					if( sTypeRest.hasMinExclusive() ){
						ne = sTypeRest.getMinExclusive().add( BigDecimal.ONE ) ;
					}
					if( sTypeRest.hasMaxInclusive() ){
						ni = sTypeRest.getMinInclusive();
					}

	                BigDecimal min = max(ne,ni);

	                if(min!=null) {
	                    if(min.compareTo(INT_MIN )>=0 && max.compareTo(INT_MAX )<=0){
	                    	clasifiedType = INT_TYPE; //typeLocalName = "int";
	                    }else if(min.compareTo(LONG_MIN)>=0 && max.compareTo(LONG_MAX)<=0){
	                    	clasifiedType = LONG_TYPE; //typeLocalName = "long";
	                    }
	                }
	            }
				
			}
		}
		
		return clasifiedType;
	}

	/**
	 * Returns the minimum value between the two. 
	 * @param a
	 * @param b
	 * @return
	 */
    private static BigDecimal min(BigDecimal a, BigDecimal b) {
        if(a==null) return b;
        if(b==null) return a;
        return a.min(b);
    }

    /**
     * Returns the maximum value between the two.
     * @param a
     * @param b
     * @return
     */
    private static BigDecimal max(BigDecimal a, BigDecimal b) {
        if(a==null) return b;
        if(b==null) return a;
        return a.max(b);
    }
}
