package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatType;
import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatValidationError;
import org.ebayopensource.turmeric.runtime.codegen.common.SchemaNode;
import org.ebayopensource.turmeric.runtime.codegen.common.SchemaNodeAttribute;
import org.ebayopensource.turmeric.runtime.codegen.common.ValidationRule;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.InputOptions;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.WSDLParserConstants;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class does the validations defined in ValidationRule.
 * The metadata file ValidationData.xml is used to perform the validation.
 * 
 * A particular validation rule can be applicable for one or more fast ser formats.
 * So this class executes validation rules which are applicable for supported fast ser formats.
 * 
 * Lets say the validation rule A is applicable for protobuf and rule B is applicable for avro.
 * If service supportes protobuf, only rule A is executed.
 * If service supports avro, only rule B is executed.
 * 
 * @author rkulandaivel
 *
 */
public class FastSerFormatValidationHandler implements SchemaConstuctConstants{
	

	private Set<ValidationRule> m_rulesTobeValidated = new HashSet<ValidationRule>();
	private List<FastSerFormatValidationError> m_errors = new ArrayList<FastSerFormatValidationError>();
	private SchemaNode m_rootNode = new SchemaNode();
	private List<FastSerFormatType> m_formatsWhichRequireValidation = null;
	private SchemaNodeRepresentationByType m_typesMap = new SchemaNodeRepresentationByType();

	private static Logger s_logger = LogManager
	.getInstance(FastSerFormatValidationHandler.class);

	private static final String MARKET_PLACE_NAMESPACE = "http://www.ebay.com/marketplace/services";

	private static Logger getLogger() {
		return s_logger;
	}
	private static final String EMPTY_STRING = "";
	private static final String REPLACE_ARG_0 = "{0}";
	private static final String REPLACE_ARG_1 = "{1}";
	private static final String REPLACE_ARG_2 = "{2}";
	private static final String REPLACE_ARG_3 = "{3}";

	public static final String EMPTY_WSDL_PATH = "The wsdl path is empty. Could not perform validation.";

	private FastSerFormatValidationHandler(List<FastSerFormatType> formatsWhichRequireValidation){
		//Consolidate only the list of rules that this service or type library supports
		FastSerFormatValidationData data = FastSerFormatValidationData.getInstance();
		for(FastSerFormatType format : formatsWhichRequireValidation){
			m_rulesTobeValidated.addAll( data.getRulesForFormat( format ) );
		}
		this.m_formatsWhichRequireValidation = formatsWhichRequireValidation;
		
	}

	/**
	 * Returns the template error object.
	 * @param rule
	 * @return
	 */
	private FastSerFormatValidationError getErrorTemplate( ValidationRule rule ){
		return FastSerFormatValidationData.getInstance().getTemplateData( rule );
	}
	/**
	 * Creates Validation Error object by mapping from the template object and from the schema node.
	 * The Description is taken from template object.
	 * All other info like line no, columnno, file name are taken from the schema node.
	 * 
	 * @param currentNode
	 * @param rule
	 * @return
	 */
	private FastSerFormatValidationError createError(SchemaNode currentNode, ValidationRule rule){
		FastSerFormatValidationError template = FastSerFormatValidationData.getInstance().getTemplateData( rule );
		
		FastSerFormatValidationError error = new FastSerFormatValidationError();
		error.setError(rule);
		error.setLineNumber( currentNode.getLineNumber() );
		error.setColumnNumber( currentNode.getColumnNumber() );
		error.setFileName( currentNode.getFileName() );
		error.setDescription( template.getDescription() );
		
		return error;
	}

	/**
	 * This method returns the string representation of supported types string
	 * for the specified validation rule.
	 * The output format is [protobuf, avro] 
	 * @param rule
	 * @return
	 */
	private String getSupportedTypesString(ValidationRule rule){
		List<FastSerFormatType> formats = FastSerFormatValidationData.getInstance().getApplicableFormats(rule);
		
		StringBuilder buff = new StringBuilder( "[ " );
		int i = 0;
		for( FastSerFormatType format : formats ){
			if( m_formatsWhichRequireValidation.contains(format) ){
				if(i > 0){
					buff.append( ", " );
				}
				buff.append( format.value() );
				
				
			}
		}
		buff.append( " ]" );
		return buff.toString();
		
	}

	/**
	 * This method logs the details of the schema node like 
	 * name, line no, column no, file name.
	 * 
	 * @param currentNode
	 * @param methodName
	 */
	private void log(SchemaNode currentNode, String methodName){
		if( !getLogger().isLoggable(Level.INFO) ){
			return;
		}
		String message = "Scanning Schema Node inside " + methodName;
		message = message + ". Node Name : " + currentNode.getNodeName();
		for( SchemaNodeAttribute attr : currentNode.getAttributes() ){
			message = message + "; " + attr.getAttributeName() + " : ";
			message = message + attr.getAttributeValue();
		}
		message = message + "; Line : " + currentNode.getLineNumber();
		message = message + "; Column : " + currentNode.getColumnNumber();
		message = message + "; FileName : " + currentNode.getFileName();
		message = message + ".";
		getLogger().info( message );
	}

	/**
	 * This method logs the error details of the schema node.
	 * 
	 * @param error
	 * @param currentNode
	 */
	private void log(FastSerFormatValidationError error, SchemaNode currentNode){
		if( !getLogger().isLoggable(Level.INFO) ){
			return;
		}
		String message = "The schema node contains validation error.";
		message = message + " Node Name : " + currentNode.getNodeName();
		message = message + "; Rule : " + error.getError();
		message = message + "; Description : " + error.getDescription();
		message = message + "; Line : " + error.getLineNumber();
		message = message + "; Column : " + error.getColumnNumber();
		message = message + "; FileName : " + error.getFileName();
		getLogger().info( message );
	}

	/**
	 * This method performs the anonymous type validations.
	 * It captures usage of anonymous types at second level or deeper.
	 * If the root element node which is directly under schema tag uses anonymous type 
	 * then it is allowed.
	 * If the element which is not root uses  anonymous type then it is captured.
	 * 
	 * @param currentNode
	 */
	private void doAnonymousCheck( SchemaNode currentNode ){
		boolean rootNode = Util.isRootNodeInSchema( currentNode ); 


		boolean isElementNode = Util.isInValidNodeName( currentNode, ELEMENT );
		boolean isAttributeNode = Util.isInValidNodeName( currentNode, ATTRIBUTE );
		if( isElementNode || isAttributeNode){
			if( !currentNode.isTypeAttrExists() &&  !currentNode.isRefAttrExists() ){
				boolean usesAnonymousType = false;
				for( SchemaNode child : currentNode.getChildNodes() ){
					if( COMPLEXTYPE.equals(child.getNodeName()) ||
							SIMPLETYPE.equals(child.getNodeName()) ){
						
						if( !rootNode ){
							FastSerFormatValidationError error = createError(currentNode, ValidationRule.ANONYMOUS_TYPE_NOT_SUPPORTED );
							String desc = error.getDescription();
							
							desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_0, currentNode );
							desc = desc.replace(REPLACE_ARG_1, isElementNode ? ELEMENT : ATTRIBUTE);
							desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.ANONYMOUS_TYPE_NOT_SUPPORTED  ) );
							error.setDescription(desc);
							
							log(error, currentNode);
							m_errors.add( error );
						}

						usesAnonymousType = true;
					}
				}
				
				//The element is not having type attribute and also does not use anonymous type 
				if( !usesAnonymousType  ){
					ValidationRule rule = isElementNode ? ValidationRule.ELEMENT_WITHOUT_TYPE_NOT_SUPPORTED : ValidationRule.ATTRIBUTES_WITHOUT_TYPE_NOT_SUPPORTED;
					FastSerFormatValidationError error = createError(currentNode, rule );
					String desc = error.getDescription();
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( rule  ) );
					error.setDescription(desc);
					
					log(error, currentNode);
					m_errors.add( error );
				}
			}

		}
	}

	/**
	 * Only the schemas which follow http://www.w3.org/2001/XMLSchema
	 * are supported. The schemas which follow older schema definitions are not supported.
	 * 
	 * This method throws immediate exception, instead of adding to error collections.
	 * The reason is that all the validations are written with assumption  that schema will follow
	 * http://www.w3.org/2001/XMLSchema.
	 * So if namespace is incorrect then no point in validation remaining schema.
	 * 
	 * @param uri
	 * @throws CodeGenFailedException
	 */
	public void doNamespaceValidation(String uri, String fileName) throws CodeGenFailedException{
		if( m_rulesTobeValidated.contains( ValidationRule.OLD_SCHEMAS_NOT_SUPPORTED ) ){

			if( WSDLParserConstants.NS_URI_1999_SCHEMA_XSD.equals( uri ) 
					|| WSDLParserConstants.NS_URI_2000_SCHEMA_XSD.equals( uri ) ){

				FastSerFormatValidationError template = getErrorTemplate( ValidationRule.OLD_SCHEMAS_NOT_SUPPORTED );
				String desc = template.getDescription();
				
				desc = desc.replace(REPLACE_ARG_0, fileName );
				desc = desc.replace(REPLACE_ARG_1, uri );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.OLD_SCHEMAS_NOT_SUPPORTED  ) );

				throw new CodeGenFailedException( desc );
			}
		}
	}

	/**
	 * The wsdl should not have more than one namespace.
	 * If wsdl has more than one namespce then add a validation rule.
	 * 
	 * This method is called during end tag validation of definitions.
	 * Since xsd does not have definitions
	 * The code should not fail for xsd validation.
	 * 
	 * 
	 * @param currentNode
	 */
	private void checkForMultipleNamespace( SchemaNode currentNode ){
		//xpath for schema node in wsdl is 
		//definitions/types/xs:schema

		//get the xpath "definitions/types" 
		
		SchemaNode typesNode = null;
		List<SchemaNode> typesNodes =  m_typesMap.getSchemaNodesList(TYPES);
		for( SchemaNode node : typesNodes ){
			//the 'definitions' would have lots of child nodes like 'message','portType'
			//so instead of traversing through all child nodes of 'definitions'
			//traverse through available 'types' node and check the parent
			if( node.getParentNode() != null && node.getParentNode() == currentNode ){
				typesNode = node;
				break;
			}
		}
		
		if( typesNode == null ){
			String message = "Types node is null which is not possible on a valid wsdl. The reason could be due to inconsistent codechange on SchemaNodeRepresentationByType";
			getLogger().log(Level.SEVERE, message );
			//throw as runtime as this could be possible only because of inconsistent code change related to 'SchemaNodeRepresentationByType'
			throw new RuntimeException( message );
		}

		//i.e. xpath = definitions/types
		if( typesNode.getChildNodes().size() > 1 ){
			//multiple namespace
			
			FastSerFormatValidationError error = createError(currentNode, ValidationRule.MULTIPLE_NAMESPACE_WSDL_IS_NOT_SUPPORTED );

			String desc = error.getDescription();

			desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.ANY_NOT_SUPPORTED  ) );
			error.setDescription(desc);

			log(error, currentNode);
			m_errors.add( error );
		}
	}
	/**
	 * This is the call back method of SAX parser event "endElement"
	 * This method does validations which are applicable on endElement like checking anonymous types.
	 * 
	 * @param currentNode
	 */
	public void doEndElementValidation(SchemaNode currentNode){
		log(currentNode, "doStartElementValidation");
		if( m_rulesTobeValidated.contains( ValidationRule.ANONYMOUS_TYPE_NOT_SUPPORTED ) ){
			doAnonymousCheck(currentNode );
		}
		
		if( m_rulesTobeValidated.contains( ValidationRule.ANY_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, DEFINITIONS ) ){
				checkForMultipleNamespace( currentNode );
				return ;
			}
		}
	}

	/**
	 * this method escapes validation of ANY_NOT_SUPPORTED if
	 * the type is part of namespace http://www.ebay.com/marketplace/services.
	 * 
	 * This method does not do validation for node name.
	 * It will just check for namespace.
	 * 
	 * @param currentNode
	 * @return
	 */
	private boolean escapeValidationForAny( SchemaNode currentNode ){
		
		SchemaNode surroundingType = Util.getSurroundingType(currentNode);

		if( surroundingType == null ){
			return false;
		}
		if( MARKET_PLACE_NAMESPACE.equals(surroundingType.getTargetNamespace() ) ){
			return true;
		}
		if( surroundingType.getLibraryInfo() == null ){
			return false;
		}
		if( MARKET_PLACE_NAMESPACE.equals( surroundingType.getLibraryInfo().getNamespace() ) ){
			return true;
		}
		
		return false;
	}
	/**
	 * This is the call back method of SAX parser event startElement.
	 * This method does all validations applicable on start element.
	 *  
	 * @param currentNode
	 */
	public void doStartElementValidation(SchemaNode currentNode){
		FastSerFormatValidationError error = null;

		log(currentNode, "doStartElementValidation");

		if( m_rulesTobeValidated.contains( ValidationRule.ANY_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, ANY ) && !escapeValidationForAny( currentNode )){
				error = createError(currentNode, ValidationRule.ANY_NOT_SUPPORTED );

				String desc = error.getDescription();
				SchemaNode surroundingType = Util.getSurroundingType( currentNode );

				desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.ANY_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				return ; 
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.ANY_ATTRIBUTE_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, ANY_ATTRIBUTE ) ){
				error = createError(currentNode, ValidationRule.ANY_ATTRIBUTE_NOT_SUPPORTED );

				String desc = error.getDescription();
				SchemaNode surroundingType = Util.getSurroundingType( currentNode );

				desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.ANY_ATTRIBUTE_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				
				return ; 
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.FIELD_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, FIELD ) ){
				error = createError(currentNode, ValidationRule.FIELD_NOT_SUPPORTED );

				String desc = error.getDescription();
				SchemaNode surroundingType = Util.getSurroundingElement( currentNode );

				desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.FIELD_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				
				return ;
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.KEY_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, KEY ) ){
				error = createError(currentNode, ValidationRule.KEY_NOT_SUPPORTED );

				String desc = error.getDescription();
				SchemaNode surroundingType = Util.getSurroundingElement( currentNode );

				desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.KEY_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				
				return ;
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.KEY_REF_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, KEY_REF ) ){
				error = createError(currentNode, ValidationRule.KEY_REF_NOT_SUPPORTED );

				String desc = error.getDescription();
				SchemaNode surroundingType = Util.getSurroundingElement( currentNode );

				desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.KEY_REF_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );

				return ;
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.NOTATION_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, NOTATION ) ){
				error = createError(currentNode, ValidationRule.NOTATION_NOT_SUPPORTED );

				String desc = error.getDescription();
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.NOTATION_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				return ;
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.REDEFINE_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, REDEFINE ) ){
				error = createError(currentNode, ValidationRule.REDEFINE_NOT_SUPPORTED );

				String desc = error.getDescription();
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.REDEFINE_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				
				return ;
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.SELECTOR_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, SELECTOR ) ){
				error = createError(currentNode, ValidationRule.SELECTOR_NOT_SUPPORTED );

				String desc = error.getDescription();
				SchemaNode surroundingType = Util.getSurroundingElement( currentNode );

				desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.SELECTOR_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				
				return ;
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.UNION_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, UNION ) ){
				error = createError(currentNode, ValidationRule.UNION_NOT_SUPPORTED );

				String desc = error.getDescription();
				SchemaNode surroundingType = Util.getSurroundingType( currentNode );

				desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.UNION_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				
				return ;
			}

		}
		if( m_rulesTobeValidated.contains( ValidationRule.UNIQUE_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, UNIQUE ) ){
				error = createError(currentNode, ValidationRule.UNIQUE_NOT_SUPPORTED );

				String desc = error.getDescription();
				SchemaNode surroundingType = Util.getSurroundingElement( currentNode );

				desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.UNIQUE_NOT_SUPPORTED  ) );
				error.setDescription(desc);

				log(error, currentNode);
				m_errors.add( error );
				
				return ;
			}
		}
		if( m_rulesTobeValidated.contains( ValidationRule.MIN_AND_MAX_OCCURS_ARE_NOT_SUPPORTED_IN_CHOICE ) ){
			if( Util.isInValidNodeName( currentNode, CHOICE ) ){

				if( currentNode.isMaxoccursAttrExists() || Util.getAttributeValue(currentNode, MINOCCURS ) != null ){

					error = createError(currentNode, ValidationRule.MIN_AND_MAX_OCCURS_ARE_NOT_SUPPORTED_IN_CHOICE );

					String desc = error.getDescription();
					SchemaNode surroundingType = Util.getSurroundingType( currentNode );

					desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
					desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.MIN_AND_MAX_OCCURS_ARE_NOT_SUPPORTED_IN_CHOICE  ) );
					error.setDescription(desc);

					log(error, currentNode);
					m_errors.add( error );
					
					return ;
				}
			}
		}
		if( m_rulesTobeValidated.contains( ValidationRule.MIN_AND_MAX_OCCURS_ARE_NOT_SUPPORTED_IN_SEQUENCE ) ){
			if( Util.isInValidNodeName( currentNode, SEQUENCE ) ){
				if( currentNode.isMaxoccursAttrExists() || Util.getAttributeValue(currentNode, MINOCCURS ) != null ){
					error = createError(currentNode, ValidationRule.MIN_AND_MAX_OCCURS_ARE_NOT_SUPPORTED_IN_SEQUENCE );

					String desc = error.getDescription();
					SchemaNode surroundingType = Util.getSurroundingType( currentNode );

					desc = Util.replaceWithNodeName( desc, REPLACE_ARG_0, surroundingType );
					desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.MIN_AND_MAX_OCCURS_ARE_NOT_SUPPORTED_IN_SEQUENCE  ) );
					error.setDescription(desc);

					log(error, currentNode);
					m_errors.add( error );
					
					return ;
				}
			}

		}

		if( m_rulesTobeValidated.contains( ValidationRule.ATTRIBUTE_NILLABLE_IS_NOT_SUPPORTED ) ){
			if( Util.isInValidNodeName( currentNode, ELEMENT ) && !Util.isRootNodeInSchema( currentNode ) ){
				String nillable = Util.getAttributeValue(currentNode, NILLABLE );

				if( nillable != null && (Boolean.parseBoolean(nillable) ) ){
					error = createError(currentNode, ValidationRule.ATTRIBUTE_NILLABLE_IS_NOT_SUPPORTED );

					String desc = error.getDescription();
					desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, currentNode );
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.ATTRIBUTE_NILLABLE_IS_NOT_SUPPORTED  ) );
					error.setDescription(desc);

					log(error, currentNode);
					m_errors.add( error );
				}
			}
		}

		if( m_rulesTobeValidated.contains( ValidationRule.IDREF_AND_IDRE_FS_ARE_NOT_SUPPORTED ) ){
			QName typeQName = null;
			if( Util.isInValidNodeName( currentNode, ELEMENT ) || Util.isInValidNodeName( currentNode, ATTRIBUTE )  ){
				typeQName = currentNode.getTypeAttrValue();

			}else if( Util.isInValidNodeName( currentNode, RESTRICTION ) || Util.isInValidNodeName( currentNode, EXTENSION ) ){
				typeQName = currentNode.getBaseAttrValue();

			}else if( Util.isInValidNodeName( currentNode, LIST ) ){
				typeQName = currentNode.getItemTypeAttrValue();

			}
			
			if( typeQName != null ){
				String typeNameLocal = typeQName.getLocalPart();
				if ( isValidInBuiltType( typeQName ) && (IDREF.equals(typeNameLocal) || IDREFS
								.equals(typeNameLocal))){
					error = createError(currentNode, ValidationRule.IDREF_AND_IDRE_FS_ARE_NOT_SUPPORTED );

					String desc = error.getDescription();
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.IDREF_AND_IDRE_FS_ARE_NOT_SUPPORTED  ) );
					error.setDescription(desc);

					log(error, currentNode);
					m_errors.add( error );
				}
			}
		}

		if( m_rulesTobeValidated.contains( ValidationRule.ANY_TYPE_AND_ANY_SIMPLE_TYPE_NOT_SUPPORTED ) ){
			QName typeQName = null;
			if( Util.isInValidNodeName( currentNode, ELEMENT ) || Util.isInValidNodeName( currentNode, ATTRIBUTE )  ){
				typeQName = currentNode.getTypeAttrValue();

			}else if( Util.isInValidNodeName( currentNode, RESTRICTION ) || Util.isInValidNodeName( currentNode, EXTENSION ) ){
				typeQName = currentNode.getBaseAttrValue();

			}else if( Util.isInValidNodeName( currentNode, LIST ) ){
				typeQName = currentNode.getItemTypeAttrValue();

			}

			if( typeQName != null ){
				String typeNameLocal = typeQName.getLocalPart();
				if ( isValidInBuiltType( typeQName ) && 
						( ANYTYPE.equals(typeNameLocal) || ANYSIMPLETYPE.equals(typeNameLocal) ) ){
					error = createError(currentNode, ValidationRule.ANY_TYPE_AND_ANY_SIMPLE_TYPE_NOT_SUPPORTED );

					String desc = error.getDescription();
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.ANY_TYPE_AND_ANY_SIMPLE_TYPE_NOT_SUPPORTED  ) );
					error.setDescription(desc);

					log(error, currentNode);
					m_errors.add( error );
				}
			}
		}
		
		if( m_rulesTobeValidated.contains( ValidationRule.MIXED_ATTRIBUTE_NOT_SUPPORTED ) ){
			String typeName = null;
			if( Util.isInValidNodeName( currentNode, COMPLEXTYPE ) ){
				typeName = Util.getAttributeValue(currentNode, MIXED);
				if( typeName != null && Boolean.parseBoolean(typeName)){
					error = createError(currentNode, ValidationRule.MIXED_ATTRIBUTE_NOT_SUPPORTED );

					String desc = error.getDescription();
					desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_0, currentNode );
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.MIXED_ATTRIBUTE_NOT_SUPPORTED  ) );
					error.setDescription(desc);

					log(error, currentNode);
					m_errors.add( error );
				}

			}
		}

		if( m_rulesTobeValidated.contains( ValidationRule.ATTRIBUTE_SUBSTITUTIONGROUP_IS_NOT_SUPPORTED ) ){
			String typeName = null;
			if( Util.isInValidNodeName( currentNode, ELEMENT ) ){
				typeName = Util.getAttributeValue(currentNode, SUBSTITUTIONGROUP);
				if( typeName != null ){
					error = createError(currentNode, ValidationRule.ATTRIBUTE_SUBSTITUTIONGROUP_IS_NOT_SUPPORTED );

					String desc = error.getDescription();
					desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_0, currentNode );
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.ATTRIBUTE_SUBSTITUTIONGROUP_IS_NOT_SUPPORTED  ) );
					error.setDescription(desc);

					log(error, currentNode);
					m_errors.add( error );
				}

			}
		}

		if( m_rulesTobeValidated.contains( ValidationRule.UNBOUNDED_ELEMENT_WITH_TYPE_ID_IS_NOT_SUPPORTED ) ){

			if( Util.isInValidNodeName( currentNode, ELEMENT ) && currentNode.isTypeAttrExists() ){
				QName typeQName = currentNode.getTypeAttrValue();
				if ( isValidInBuiltType( typeQName ) &&  ID.equals(typeQName.getLocalPart()) ){
					
					String maxOcurs = currentNode.getMaxoccursAttrValue();
					if( currentNode.isMaxoccursAttrExists() &&  ( UNBOUNDED.equals(maxOcurs) || Integer.valueOf(maxOcurs) > 1 )){
						error = createError(currentNode, ValidationRule.UNBOUNDED_ELEMENT_WITH_TYPE_ID_IS_NOT_SUPPORTED );

						String desc = error.getDescription();
						desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_0, currentNode );
						desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.UNBOUNDED_ELEMENT_WITH_TYPE_ID_IS_NOT_SUPPORTED  ) );
						error.setDescription(desc);

						log(error, currentNode);
						m_errors.add( error );	
					}
				}

			}
		}
	}

	private boolean isValidInBuiltType(QName xsdType){
		if( xsdType == null ){
			return false;
		}
		return xsdType.getNamespaceURI().equals( WSDLParserConstants.NS_URI_2001_SCHEMA_XSD);
	}

	private void validatePolymorphism(SchemaNodeRepresentationByType typesMap){
		//Test Polymorphism

		List<SchemaNode> complexTypeNodes  = typesMap.getSchemaNodesList(COMPLEXTYPE);
		List<SchemaNode> elementNodes = typesMap.getSchemaNodesList( ELEMENT );
		Map<QName, SchemaNode> abstractTypeMap = new HashMap<QName, SchemaNode>();
		for( SchemaNode node : complexTypeNodes){
			if( node.isAbstractAttrExists() && node.isAbstractAttrValue() && node.isNameAttrExists() ){
				abstractTypeMap.put( new QName( node.getTargetNamespace(), node.getNameAttrValue() ), node );
			}
		}

		for( SchemaNode node : elementNodes){
			if( node.isTypeAttrExists() ){
				QName typeQName = node.getTypeAttrValue();
				SchemaNode abstractNode = abstractTypeMap.get(typeQName);
				if(abstractNode != null){
					FastSerFormatValidationError error = createError(node, ValidationRule.POLYMORPHISM_NOT_SUPPORTED );

					String desc = error.getDescription();
					desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_0, node );
					desc = desc.replace(REPLACE_ARG_1, typeQName.getLocalPart() );
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.POLYMORPHISM_NOT_SUPPORTED  ) );
					error.setDescription(desc);
					log(error, node);
					m_errors.add( error );
				}
			}
		}
	}


	/**
	 * This method does validations for three rules.
	 * ATTRIBUTE_NAME_CANNOT_BE_SAME_AS_FIELD_NAME
	 * TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME
	 * NAME_OF_ATTRIBUTE_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE
	 * 
	 * 
	 * @param currentNode
	 * @param mapOfAllTypes
	 */
	private void validateAttributeAndElementName(SchemaNode currentNode, SchemaNodeRepresentationByType typesMap ){

		//This map collects all the attribute nodes defined in the complex type.
		//If two attributes have same name then it is overwritten.
		Map<String, SchemaNode> mapOfAttributes = new HashMap<String, SchemaNode>();

		//This map collects all the element nodes defined in the complex type.
		//The validation for duplicate element name (TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME)
		//is aggressive i.e. while traversing validation takes place
		Map<String, SchemaNode> mapOfElements = new HashMap<String, SchemaNode>();

		//If any of the complex type extends a used defined simple type or inbuilt xsd type
		//it is invalid NAME_OF_ELEMENT_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE
		Map<QName, SchemaNode> baseType2ComplexTypeMap = new HashMap<QName, SchemaNode>();

		recursivelyPopulateAndvalidateName( currentNode, typesMap, mapOfAttributes, mapOfElements, baseType2ComplexTypeMap);

		//validate for NAME_OF_ATTRIBUTE_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE
		//size cannot be more than one
		if( m_rulesTobeValidated.contains( ValidationRule.NAME_OF_ATTRIBUTE_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE ) ){
			if( baseType2ComplexTypeMap.size() == 1 && mapOfAttributes.containsKey( VALUE ) ){
				FastSerFormatValidationError error = createError(currentNode, ValidationRule.NAME_OF_ATTRIBUTE_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE );
				String desc = error.getDescription();
	
				desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, currentNode );
	
				desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.NAME_OF_ATTRIBUTE_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE  ) );
				error.setDescription(desc);
				log(error, currentNode);
				m_errors.add( error );
			}
		}
		
		//validate attribute names
		//If any of the attribute name is same as the element name then throw error ATTRIBUTE_NAME_CANNOT_BE_SAME_AS_FIELD_NAME
		if( m_rulesTobeValidated.contains( ValidationRule.ATTRIBUTE_NAME_CANNOT_BE_SAME_AS_FIELD_NAME ) ){
			for( Map.Entry<String, SchemaNode> entry : mapOfAttributes.entrySet() ){
				String attributeName = entry.getKey();
				SchemaNode attr = entry.getValue();
				
				//same name but different case is also not supported
				SchemaNode elemen = mapOfElements.get(attributeName.toLowerCase());
	
				if( elemen != null ){
					FastSerFormatValidationError error = createError(attr, ValidationRule.ATTRIBUTE_NAME_CANNOT_BE_SAME_AS_FIELD_NAME );
					String desc = error.getDescription();
	
					desc = desc.replace(REPLACE_ARG_0, attributeName);
	
					SchemaNode surroundingType = Util.getSurroundingType( elemen );
					desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.ATTRIBUTE_NAME_CANNOT_BE_SAME_AS_FIELD_NAME  ) );
					error.setDescription(desc);
					log(error, surroundingType);
					m_errors.add( error );
				}
			}
		}
	}

	/**
	 * This method is called if two element have same name in the same hierarchy.
	 * Creates error ValidationRule.TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME
	 * 
	 * @param oldElement
	 * @param newElement
	 */
	private void handleDuplicateElementName(SchemaNode oldElement, SchemaNode newElement, String name){
		if( !m_rulesTobeValidated.contains( ValidationRule.TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME ) ){
			return ;
		}
		FastSerFormatValidationError error = createError(newElement, ValidationRule.TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME );
		String desc = error.getDescription();

		SchemaNode surroundingType = Util.getSurroundingType( oldElement );
		desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_0, surroundingType );
		
		surroundingType = Util.getSurroundingType( newElement );
		desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_1, surroundingType );
		desc = desc.replace(REPLACE_ARG_3, name );

		desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME  ) );
		error.setDescription(desc);
		log(error, surroundingType);
		m_errors.add( error );
	}

	/**
	 * This method is called if the current node is restriction or extension as restriction and extension would have base attribute.
	 * If the base type is user defined simple type or inbuilt type then argument baseType2ComplexTypeMap is filled.
	 * This map would be used later for rule NAME_OF_ATTRIBUTE_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE
	 * 
	 * If the base type is complex type, recursivelyPopulateAndvalidateName is called.
	 * 
	 * @param currentNode
	 * @param mapOfAllTypes
	 * @param mapOfAttributes
	 * @param mapOfElements
	 * @param baseType2ComplexTypeMap
	 */
	private void handleBaseTypes(SchemaNode currentNode,
			SchemaNodeRepresentationByType typesMap,
			Map<String, SchemaNode> mapOfAttributes,
			Map<String, SchemaNode> mapOfElements,
			Map<QName, SchemaNode> baseType2ComplexTypeMap) {

		if( currentNode.isBaseAttrExists() ){
			QName typeQName = currentNode.getBaseAttrValue();
			
			SchemaNode baseType = typesMap.getType(typeQName);// mapOfAllTypes.get(typeQName);
			if (baseType != null
					&& baseType.getNodeName().equals(COMPLEXTYPE)) {
				recursivelyPopulateAndvalidateName(baseType, typesMap,
						mapOfAttributes, mapOfElements,
						baseType2ComplexTypeMap);
			} else {
				boolean userDefinedSimpleType = false;
				boolean xsdType = typeQName.getNamespaceURI().equals(
						WSDLParserConstants.NS_URI_2001_SCHEMA_XSD);
				if (!xsdType && baseType != null) {
					userDefinedSimpleType = baseType.getNodeName().equals(
							SIMPLETYPE);
				}

				if (xsdType || userDefinedSimpleType) {
					baseType2ComplexTypeMap.put(typeQName,
							Util.getSurroundingType(currentNode));
				}
			}
		}
	}

	/**
	 * If the node passed is of element type, the method updates map mapOfElements.
	 * If the node passed is of attribute type, the method updates map mapOfAttributes.
	 * If the node passed is of restriction or extension, the method calls handleBaseTypes to populate
	 * mapOfElements and mapOfAttributes 
	 * 
	 * For each child node, it calls recursively the same method to populate the names.  
	 * If duplicate field names are encountered, error TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME is thrown.
	 * 
	 * @param currentNode
	 * @param mapOfAllTypes
	 * @param mapOfAttributes
	 * @param mapOfElements
	 * @param baseType2ComplexTypeMap
	 */
	private void recursivelyPopulateAndvalidateName(SchemaNode currentNode,
			SchemaNodeRepresentationByType typesMap,
			Map<String, SchemaNode> mapOfAttributes,
			Map<String, SchemaNode> mapOfElements,
			Map<QName, SchemaNode> baseType2ComplexTypeMap) {

		if( Util.isInValidNodeName( currentNode, RESTRICTION ) ){
			handleBaseTypes( currentNode, typesMap, mapOfAttributes, mapOfElements, baseType2ComplexTypeMap );
			//if it is a restriction, then return immediately after handling base type.
			//since it is a restricted complex type, the element name defined inside restriction would be same 
			//as element names defined in base type.
			return ;

		}else if( Util.isInValidNodeName( currentNode, EXTENSION ) ){
			handleBaseTypes( currentNode, typesMap, mapOfAttributes, mapOfElements, baseType2ComplexTypeMap );

		}else if( Util.isInValidNodeName( currentNode, ELEMENT ) ){
			SchemaNode elementNode = currentNode;

			if( !elementNode.isNameAttrExists() && elementNode.isRefAttrExists() ){
				QName refQName = currentNode.getRefAttrValue();
				elementNode = typesMap.getRootElementNode(refQName);
			}
			if(elementNode != null ){
				String nameAttribute = elementNode.getNameAttrValue();
				//same name but different case is also not supported
				SchemaNode oldElement = mapOfElements.put(nameAttribute.toLowerCase(), elementNode);
				//if it is not null, then there are two elements with same name
				if( oldElement != null ){
					handleDuplicateElementName(oldElement, elementNode, nameAttribute);
				}				
			}

		}else if( Util.isInValidNodeName( currentNode, ATTRIBUTE ) ){
			if( currentNode.isNameAttrExists() ){
				//same name but different case is also not supported
				mapOfAttributes.put(currentNode.getNameAttrValue().toLowerCase() , currentNode);	
			}

		}else if( Util.isInValidNodeName( currentNode, ATTRIBUTE_GROUP ) || Util.isInValidNodeName( currentNode, GROUP ) ){
			boolean isAttrGroup = Util.isInValidNodeName( currentNode, ATTRIBUTE_GROUP );

			if( currentNode.isRefAttrExists() ){
				QName refQName = currentNode.getRefAttrValue();
				SchemaNode refNode = null;
				if(isAttrGroup){
					refNode = typesMap.getRootAttributeGroupNode(refQName);
				}else{
					refNode = typesMap.getRootGroupNode(refQName);
				}
				if( refNode != null ){
					recursivelyPopulateAndvalidateName(refNode, typesMap, mapOfAttributes, mapOfElements, baseType2ComplexTypeMap);
				}
			}

		}

		List<SchemaNode> childNodes = currentNode.getChildNodes();
		for( SchemaNode child : childNodes ){
			recursivelyPopulateAndvalidateName( child, typesMap, mapOfAttributes, mapOfElements, baseType2ComplexTypeMap);
		}
	}

	/**
	 * This method does validations for threee rules.
	 * ATTRIBUTE_NAME_CANNOT_BE_SAME_AS_FIELD_NAME
	 * TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME
	 * NAME_OF_ATTRIBUTE_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE
	 * 
	 */
	private void validateDuplicateFieldNames(SchemaNodeRepresentationByType typesMap ){
		List<SchemaNode> complexTypeNodes = typesMap.getComplexTypeNodes();
		for( SchemaNode node : complexTypeNodes ){
			validateAttributeAndElementName( node, typesMap );			
		}

	}

	/**
	 * This method validates following structure.
	 * 
	 *     	<xs:simpleType name="MySimpleTypeListA">
     *			<xs:restriction base="tns:MySimpleTypeList"></xs:restriction>
     *		</xs:simpleType>
     *		<xs:simpleType name="MySimpleTypeList">
     *			<xs:list itemType="xs:double" />
     * 		</xs:simpleType>
     * 
     *       <xs:element minOccurs="0" maxOccurs="unbounded" name="param5"  
     *       type="tns:MySimpleTypeListA" />
     *       
     *       The jaxb generates type as List<JAXBElement<List<Double>>> param5
     *       
     * This is not supported by protobuf
     * 
	 * @param allSimpleTypeNodes
	 * @param allElementNodes
	 */
	private void validateListOfList( SchemaNodeRepresentationByType typesMap ){

		List<SchemaNode> allElementNodes = typesMap.getSchemaNodesList( ELEMENT );
		List<SchemaNode> listNodes = typesMap.getSchemaNodesList(LIST);
		List<SchemaNode> restrctionNodes = typesMap.getSchemaNodesList(RESTRICTION);

		//contains all the simple types that has list nodes 
		Map<QName,SchemaNode> listSimpleTypeNodesMap = new HashMap<QName,SchemaNode>();
		
		//maps the restriction base type name to its simple type name
		//e.g. 
		/**
		 *      <xs:simpleType name="MySimpleTypeListA">
    	 *			<xs:restriction base="tns:MySimpleTypeList"></xs:restriction>
    	 *	 	</xs:simpleType>
		 */
		Map<QName, List<QName> > restrictionBase2NameMap = new HashMap<QName, List<QName> >();
		
		//contains the final set of simple types which element should not use as unbounded 
		Set<QName> derivedListUsers = new HashSet<QName>();


		populateListSimpleTypeNodesMap(listNodes, listSimpleTypeNodesMap);
		if( listSimpleTypeNodesMap.size() == 0 ){
			//if no list tag just return
			return ;
		}

		derivedListUsers.addAll( listSimpleTypeNodesMap.keySet() );
		populateRestrictionBase2NameMap(restrctionNodes, restrictionBase2NameMap);

		if( restrictionBase2NameMap.size() > 0 ){
			for( Map.Entry<QName, SchemaNode> entry :  listSimpleTypeNodesMap.entrySet() ){
				QName baseType = entry.getKey();
				
				//find out recursively consider the example given in comments
				//a simple type can have another simple type as restriction.
				//this can go n level deep
				recursivelyFindOutAllSimpleTypesThatUsesList( baseType, restrictionBase2NameMap, derivedListUsers );

			}
		}
		
		for( SchemaNode elementNode : allElementNodes ){
			String maxOcurs = elementNode.getMaxoccursAttrValue();
			if( elementNode.isMaxoccursAttrExists() &&  ( UNBOUNDED.equals(maxOcurs) || Integer.valueOf(maxOcurs) > 1 )){
				if( elementNode.isTypeAttrExists() && derivedListUsers.contains(elementNode.getTypeAttrValue() ) ){
					FastSerFormatValidationError error = createError(elementNode, ValidationRule.UNBOUNDED_SIMPLE_TYPE_WITH_LIST_NOT_SUPPORTED );
					String desc = error.getDescription();

					desc = Util.replaceWithNameAttribute( desc, REPLACE_ARG_0, elementNode );

					desc = desc.replace(REPLACE_ARG_2, getSupportedTypesString( ValidationRule.UNBOUNDED_SIMPLE_TYPE_WITH_LIST_NOT_SUPPORTED  ) );
					error.setDescription(desc);
					log(error, elementNode);
					m_errors.add( error );
					
				}
			}
		}
	}

	private void recursivelyFindOutAllSimpleTypesThatUsesList(QName baseType,
			Map<QName, List<QName>> restrictionBase2NameMap,
			Set<QName> derivedListUsers) {
		List<QName> simpleTypeNames = restrictionBase2NameMap.get( baseType );
		if( simpleTypeNames == null ){
			simpleTypeNames = new ArrayList<QName>();
		}
		
		derivedListUsers.addAll(simpleTypeNames);
		for( QName simpleTypeName : simpleTypeNames ){
			recursivelyFindOutAllSimpleTypesThatUsesList(simpleTypeName, restrictionBase2NameMap, derivedListUsers);
		}

		
	}
	private void populateListSimpleTypeNodesMap(List<SchemaNode> listNodes, Map<QName,SchemaNode> listSimpleTypeNodesMap){
		for( SchemaNode node : listNodes ){
			SchemaNode surroundingNode = Util.getSurroundingType(node);
			if( Util.isInValidNodeName(surroundingNode, SIMPLETYPE)){
				//may be an anonymous type
				if( surroundingNode.isNameAttrExists() ){
					QName typeName = new QName( surroundingNode.getTargetNamespace(), surroundingNode.getNameAttrValue() );
					listSimpleTypeNodesMap.put(typeName, surroundingNode);
				}
				
				
			}
		}
	}
	private void populateRestrictionBase2NameMap(List<SchemaNode> restrctionNodes, Map<QName, List<QName> > restrictionBase2NameMap){
		for( SchemaNode node : restrctionNodes ){
			SchemaNode surroundingNode = Util.getSurroundingType(node);
			if( Util.isInValidNodeName(surroundingNode, SIMPLETYPE)){
				
				//may be an anonymous type
				if( surroundingNode.isNameAttrExists()  &&  node.isBaseAttrExists() ){
					QName baseTypeName = node.getBaseAttrValue();
					//we are interested in user defined types
					if( isValidInBuiltType(baseTypeName) ){
						continue;
					}

					QName typeName = new QName( surroundingNode.getTargetNamespace(), surroundingNode.getNameAttrValue() );
					List<QName> listOfTypes = restrictionBase2NameMap.get( baseTypeName );

					if( listOfTypes == null ){
						listOfTypes = new ArrayList<QName>();
						restrictionBase2NameMap.put( baseTypeName, listOfTypes );
					}
					
					listOfTypes.add(typeName);
				}
				
			}
		}
	}
	private void doOtherValidations(){

		if( m_rulesTobeValidated.contains( ValidationRule.POLYMORPHISM_NOT_SUPPORTED ) ){
			validatePolymorphism( m_typesMap );
		}

		if( m_rulesTobeValidated.contains( ValidationRule.ATTRIBUTE_NAME_CANNOT_BE_SAME_AS_FIELD_NAME ) 
				|| m_rulesTobeValidated.contains( ValidationRule.TWO_ELEMENTS_CANNOT_HAVE_SAME_NAME ) 
				|| m_rulesTobeValidated.contains( ValidationRule.NAME_OF_ATTRIBUTE_CANNOT_BE_VALUE_WHEN_EXTENDING_SIMPLE_TYPE ) ){
			validateDuplicateFieldNames( m_typesMap);
		}
		
		if( m_rulesTobeValidated.contains( ValidationRule.UNBOUNDED_SIMPLE_TYPE_WITH_LIST_NOT_SUPPORTED ) ){
			validateListOfList( m_typesMap );
		}
	}

	private boolean isValidURL( String filePath ){
		try {
			new URL( filePath );
			return true;
		} catch (MalformedURLException e) {
			getLogger().log(Level.SEVERE, "The file path passed is not a valid URL."+filePath);
			getLogger().log(Level.SEVERE, "The file path passed is not a valid URL."+e.getMessage());
			return false;
		}
	}
	/**
	 * This method validates one or more files.
	 * The file can be wsdl file or xsd file.
	 * 
	 * @param files
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public List<FastSerFormatValidationError> validateFile(String... files) throws CodeGenFailedException{
		long startTime = System.currentTimeMillis();

		LocatorImpl sourceLocator = new LocatorImpl();

		SchemaParserEventHandler handler = new SchemaParserEventHandler( this, m_rootNode, m_typesMap );
		handler.setDocumentLocator(sourceLocator);

		for( int i=0; i<files.length; i++ ){
			String filePath = files[i];
			File file = new File( filePath );
			InputStream stream = null;
			

			getLogger().log(Level.INFO, "Start loading file " + filePath);
			try{
				if( file.exists() ){
					stream = new FileInputStream( file );
					getLogger().log(Level.INFO, "File path exists");

				}else if( isValidURL( filePath ) ){
					//path of a jar. expected format is 'jar:file:\C:....\xyz.jar!\...\abc.xsd'
					URL filePathUrl = new URL( filePath );
					stream = filePathUrl.openStream();
				}else{
					getLogger().log(Level.INFO, "File path does not exists. Try to load file using getResourceAsStream");
					
					stream = CodeGenUtil.getInputStreamForAFileFromClasspath(filePath, getClass().getClassLoader() );

					if( stream == null ){
						getLogger().log(Level.SEVERE, "File not found. Path = " + filePath);
						throw new CodeGenFailedException("File not found. Path = " + filePath );
					}

				}
				InputSource is = new InputSource( stream );
				XMLReader reader = XMLReaderFactory.createXMLReader();

				reader.setContentHandler(handler);
				reader.setErrorHandler(handler);

				handler.setFileName( file.getAbsolutePath()  );
				reader.parse( is );

			}catch(CodeGenFailedException e){
				throw e;
			}catch(Exception e){
				throw new CodeGenFailedException("File Parsing Failed." + e.getMessage(), e );
			}finally{
				if( stream != null){
					try {
						stream.close();
					} catch (Exception e) {
						getLogger().log(Level.WARNING, "Unable to close the wsdl file");
					}
				}
			}
		}

		doOtherValidations();

		long endTime = System.currentTimeMillis();
		getLogger().log(Level.INFO, "The time take to do parsing  and perform all validations "+ (endTime-startTime) +" msecs");
		return m_errors;


	}

	/**
	 * This method validates one single wsdl file.
	 * 
	 * @param wsdlFile
	 * @param supportedFormats
	 * @throws CodeGenFailedException
	 */
	public static void validateWSDL(String wsdlFileLoc, List<FastSerFormatType> supportedFormats) throws CodeGenFailedException{
		FastSerFormatValidationHandler handler = new FastSerFormatValidationHandler( supportedFormats );
		
		try {
			List<FastSerFormatValidationError> errors = handler.validateFile( wsdlFileLoc );
			if( errors.size() > 0 ){
				String msg = "The WSDL is invalid to support Fast Ser Formats like " + supportedFormats.toString();
				throw new FastSerFormatNotSupportedException(msg, errors );
			}
		}catch (FastSerFormatNotSupportedException e ){
			throw e;
		}catch (CodeGenFailedException e){
			throw e;
		}catch (Exception e) {
			throw new CodeGenFailedException("Validation of the WSDL failed.", e);
		} 

	}

	/**
	 * This method validates the wsdl against unsupported constructs for fast ser format.
	 * 
	 * @param context
	 * @param supportedFormats
	 * @throws CodeGenFailedException
	 */
	public static void validateServiceForFastSerFormatSupport(CodeGenContext context, List<FastSerFormatType> supportedFormats) throws CodeGenFailedException{
		InputOptions inputOptions = context.getInputOptions();
		String wsdlFileLoc = inputOptions.getInputFile();
		if( wsdlFileLoc == null ){
			getLogger().warning("Validation Not done. The path of the wsdl is empty.");
			throw new CodeGenFailedException( EMPTY_WSDL_PATH );
		}

		validateWSDL( wsdlFileLoc, supportedFormats);
	}

	/**
	 * This method validates list of xsd files.
	 * 
	 * @param xsdFileNames
	 * @param baseXsdPath
	 * @param supportedFormats
	 * @throws CodeGenFailedException
	 */
	public static void validateXsds(String[] xsdFileNames, List<FastSerFormatType> supportedFormats) throws CodeGenFailedException{
		FastSerFormatValidationHandler handler = new FastSerFormatValidationHandler( supportedFormats );
		
		try {
			List<FastSerFormatValidationError> errors = handler.validateFile( xsdFileNames );
			if( errors.size() > 0 ){
				String msg = "One or more XSD files are invalid to support Fast Ser Formats like " + supportedFormats.toString();
				throw new FastSerFormatNotSupportedException(msg, errors );
			}
		}catch (FastSerFormatNotSupportedException e ){
			throw e;
		}catch (CodeGenFailedException e){
			throw e;
		}catch (Exception e) {
			throw new CodeGenFailedException("Validation of the WSDL failed.", e);
		} 

	}

	public static class Util {

		/**
		 * Checks whether the given node contains name same as the node name.
		 *  
		 * @param currentNode
		 * @param nodeName
		 * @return
		 */
		public static boolean isInValidNodeName(SchemaNode currentNode, String... nodeNames ){
			for(String nodeName : nodeNames ){
				if(nodeName.equals( currentNode.getNodeName() ) ){
					return true;
				}
			}
			return false;
		}

		/**
		 * Returns the value of the specified attribute.
		 * Returns null if the attribute is not found.
		 *  
		 * @param attrs
		 * @param attributeName
		 * @return
		 */
		public static String getAttributeValue(List<SchemaNodeAttribute> attrs, String attributeName){
			for( SchemaNodeAttribute attr : attrs ){
				if( attributeName.equals(attr.getAttributeName()) ){
					return attr.getAttributeValue();
				}
			}
			return null;
		}

		/**
		 * This method returns the value of the specified attribute name.
		 * This method is fail safe which checks not null for node.
		 *  Returns null if the node is null or attribute is not found.
		 *  
		 * @param node
		 * @param attributeName
		 * @return
		 */
		public static String getAttributeValue(SchemaNode node, String attributeName){
			if( node == null ){
				return null;
			}
			return getAttributeValue(node.getAttributes(), attributeName );
		}

		/**
		 * This method identifies the surrounding type of the specified node.
		 * The surrounding type returned would be either Complex type, Simple Type,
		 * Group or attribute group.
		 * This method is used to build user friendly error message.
		 * 
		 * @param node
		 * @return
		 */
		public static SchemaNode getSurroundingType( SchemaNode node ){
			SchemaNode parent = node.getParentNode();
			while (parent != null){
				String parentNodename = parent.getNodeName();
				
				if(COMPLEXTYPE.equals(parentNodename) || 
						SIMPLETYPE.equals(parentNodename) || 
						GROUP.equals(parentNodename) || 
						ATTRIBUTE_GROUP.equals(parentNodename) ){
					return parent;
				}
				parent = parent.getParentNode();
			}
			return null;
		}

		/**
		 * This method identifies the surrounding element of the specified node.
		 * 
		 * @param node
		 * @return
		 */
		public static SchemaNode getSurroundingElement( SchemaNode node ){
			SchemaNode parent = node.getParentNode();
			while (parent != null){
				String parentNodename = parent.getNodeName();
				
				if(ELEMENT.equals(parentNodename) ){
					return parent;
				}
				parent = parent.getParentNode();
			}
			return null;
		}

		/**
		 * If the root element node which is directly under schema tag then return true;
		 * 
		 * @param currentNode
		 * @return
		 */
		public static boolean isRootNodeInSchema( SchemaNode currentNode ){
			if( currentNode.getParentNode() != null){
				if( SCHEMA.equals( currentNode.getParentNode().getNodeName() ) ){
					//this is because if the element is at root level directly inside schema node then dont validate  
					return true;
				}
			}
			return false;
		}

		public static String replaceWithNodeName(String templateToSearch, String searchString, SchemaNode node){
			templateToSearch = templateToSearch.replace(searchString, ( node!=null ) ? node.getNodeName() : EMPTY_STRING);
			return templateToSearch;
		}
		
		public static String replaceWithNameAttribute(String templateToSearch, String searchString, SchemaNode node){
			String nameAttribute = EMPTY_STRING;
			if( node != null && node.isNameAttrExists()){
				nameAttribute = node.getNameAttrValue();
			}
			templateToSearch = templateToSearch.replace(searchString, nameAttribute );
			return templateToSearch;
		}
	}

}
