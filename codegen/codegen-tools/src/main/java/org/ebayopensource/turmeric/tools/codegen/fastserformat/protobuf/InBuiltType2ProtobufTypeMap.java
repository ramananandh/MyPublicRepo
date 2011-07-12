/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.WSDLParserConstants;

/**
 * @author rkulandaivel
 *
 */
public class InBuiltType2ProtobufTypeMap {

	private static final Map<QName, String> s_map = new HashMap<QName, String>();
	private static QName qName( String primitiveTypeName){
		return new QName( WSDLParserConstants.NS_URI_2001_SCHEMA_XSD, primitiveTypeName );
	}
	public static final QName POSITIVEINTEGER_TYPE = qName("positiveInteger");
	public static final QName INT_TYPE = qName("int");
	public static final QName STRING_TYPE = qName("string");
	public static final QName INTEGER_TYPE = qName("integer");
	public static final QName LONG_TYPE = qName("long");
	public static final QName DECIMAL_TYPE = qName("decimal");
	public static final QName ID_TYPE = qName("ID");
	public static final QName NMTOKEN_TYPE = qName("NMTOKEN");
	public static final QName NMTOKENS_TYPE = qName("NMTOKENS");
	public static final QName ANYURI_TYPE = qName("anyURI");
	public static final QName BOOLEAN_TYPE = qName("boolean");
	public static final QName BYTE_TYPE = qName("byte");
	public static final QName BASE64BINARY_TYPE = qName("base64Binary");
	public static final QName DATE_TYPE = qName("date");
	public static final QName FLOAT_TYPE = qName("float");
	public static final QName DOUBLE_TYPE = qName("double");
	public static final QName DURATION_TYPE = qName("duration");
	public static final QName DATETIME_TYPE = qName("dateTime");
	public static final QName TIME_TYPE = qName("time");
	public static final QName GYEARMONTH_TYPE = qName("gYearMonth");
	public static final QName GYEAR_TYPE = qName("gYear");
	public static final QName GMONTHDAY_TYPE = qName("gMonthDay");
	public static final QName GDAY_TYPE = qName("gDay");
	public static final QName GMONTH_TYPE = qName("gMonth");
	public static final QName HEXBINARY_TYPE = qName("hexBinary");
	public static final QName QNAME_TYPE = qName("QName");
	public static final QName TOKEN_TYPE = qName("token");
	public static final QName ENTITY_TYPE = qName("ENTITY");
	public static final QName ENTITIES_TYPE = qName("ENTITIES");
	public static final QName LANGUAGE_TYPE = qName("language");
	public static final QName NAME_TYPE = qName("Name");
	public static final QName NC_NAME_TYPE = qName("NCName");
	public static final QName NEGATIVEINTEGER_TYPE = qName("negativeInteger");
	public static final QName NONNEGATIVEINTEGER_TYPE = qName("nonNegativeInteger");
	public static final QName NONPOSITIVEINTEGER_TYPE = qName("nonPositiveInteger");
	public static final QName NOMALIZED_TYPE = qName("normalizedString");
	public static final QName USIGNED_BYTE_TYPE = qName("unsignedByte");
	public static final QName USIGNED_INT = qName("unsignedInt");
	public static final QName USIGNED_LONG = qName("unsignedLong");
	public static final QName USIGNED_SHORT = qName("unsignedShort");
	public static final QName SHORT = qName("short");

	static{
		s_map.put(POSITIVEINTEGER_TYPE, 	"string");
		s_map.put(INT_TYPE, 				"sint32");
		s_map.put(STRING_TYPE, 				"string");
		s_map.put(INTEGER_TYPE, 			"string");
		s_map.put(LONG_TYPE, 				"sint64");
		s_map.put(DECIMAL_TYPE, 			"string");
		s_map.put(ID_TYPE, 					"string");
		s_map.put(NMTOKEN_TYPE, 			"string");
		s_map.put(NMTOKENS_TYPE, 			"string");
		s_map.put(ANYURI_TYPE,				"string");
		s_map.put(BOOLEAN_TYPE,				"bool");
		s_map.put(BYTE_TYPE,				"sint32");
		s_map.put(BASE64BINARY_TYPE,		"bytes");
		s_map.put(DATE_TYPE,				"sint64");
		s_map.put(FLOAT_TYPE,				"float");
		s_map.put(DOUBLE_TYPE,				"double");
		s_map.put(DURATION_TYPE,			"sint64");
		s_map.put(DATETIME_TYPE,			"sint64");
		s_map.put(TIME_TYPE,				"sint64");
		s_map.put(GYEARMONTH_TYPE,			"sint64");
		s_map.put(GYEAR_TYPE,				"sint64");
		s_map.put(GMONTHDAY_TYPE,			"sint64");
		s_map.put(GDAY_TYPE,				"sint64");
		s_map.put(GMONTH_TYPE,				"sint64");
		s_map.put(HEXBINARY_TYPE,			"bytes");
		s_map.put(QNAME_TYPE,				"string");
		s_map.put(TOKEN_TYPE,				"string");
		s_map.put(ENTITY_TYPE,				"string");
		s_map.put(ENTITIES_TYPE,			"string");
		s_map.put(LANGUAGE_TYPE,			"string");
		s_map.put(NAME_TYPE,				"string");
		s_map.put(NC_NAME_TYPE,				"string");
		s_map.put(NEGATIVEINTEGER_TYPE,		"string");
		s_map.put(NONNEGATIVEINTEGER_TYPE,	"string");
		s_map.put(NONPOSITIVEINTEGER_TYPE,	"string");
		s_map.put(NOMALIZED_TYPE,			"string");
		s_map.put(USIGNED_BYTE_TYPE,		"sint32");
		s_map.put(USIGNED_INT,				"sint64");
		s_map.put(USIGNED_LONG,				"string");
		s_map.put(USIGNED_SHORT,			"sint32");
		s_map.put(SHORT,					"sint32");
	}

	public static String getProtoType(QName xsdType){
		return s_map.get(xsdType);
	}

	public static String getProtoType(String xsdType){
		return s_map.get( qName(xsdType) );
	}
	
	public static boolean isValidInBuiltType(QName xsdType){
		if( xsdType == null ){
			return false;
		}
		return s_map.get(xsdType) != null;
	}
}
