/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.runtime.codegen.common.SchemaNode;
import org.ebayopensource.turmeric.runtime.codegen.common.SchemaNodeAttribute;
import org.ebayopensource.turmeric.runtime.codegen.common.SchemaNodeLibraryInfo;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is the handler for SAX parser which FastSerFormatValidationHandler uses to validate the wsdl or xsd.
 * On each event, it creates a SchemaNode object and delegates to FastSerFormatValidationHandler to
 * do the validation.
 * 
 * Apart from delegating, it captures all the node of the schema and caches in a tree structure SchemaNode.
 * This tree structure is used later by FastSerFormatValidationHandler to do further validations like polymorphism.
 *   
 * @author rkulandaivel
 *
 */
public class SchemaParserEventHandler extends DefaultHandler implements SchemaConstuctConstants {

	private FastSerFormatValidationHandler m_validator = null;
	private SchemaNode m_currentSchemaNode = null;
	private SchemaNodeRepresentationByType m_mapOfAllNodes = null;
	private Locator locator = null;

	private String m_targetNamespace = null;
	private String m_fileName = null;
	private Map<String, NamespaceNode> m_prefix2NamespaceLink = new HashMap<String, NamespaceNode>();

	private static Logger s_logger = LogManager
	.getInstance(SchemaParserEventHandler.class);

	private static Logger getLogger() {
		return s_logger;
	}
	public SchemaParserEventHandler(FastSerFormatValidationHandler validator,
			SchemaNode rootSchemaNode,
			SchemaNodeRepresentationByType mapOfAllNodes ) {
		this.m_validator = validator;
		m_currentSchemaNode = rootSchemaNode;
		this.m_mapOfAllNodes = mapOfAllNodes;
	}

	/**
	 * The method used to store the locator which is used to retrive the line numbers
	 * on each event.
	 */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    
	public void setFileName(String fileName) {
		this.m_fileName = fileName;
	}


	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		//Dereference the prefix from map m_prefix2NamespaceLink.
		NamespaceNode node = m_prefix2NamespaceLink.get(prefix);
		NamespaceNode prevNode = node.previous;
		node.previous = null; //derefence it
		m_prefix2NamespaceLink.put(prefix, prevNode );
	}


	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

		/*
		 * For each URI, create a new NamespaceNode.
		 * Update the map m_prefix2NamespaceLink with the NamespaceNode instance
		 * If the map contains prefix already, reference the map value to 'previous' property of NamespaceNode.
		 */
		NamespaceNode newNode = new NamespaceNode();
		newNode.currentNamespace = uri;
		newNode.previous = m_prefix2NamespaceLink.get(prefix);
		m_prefix2NamespaceLink.put(prefix, newNode);

		try {
			m_validator.doNamespaceValidation( uri, m_fileName );
		} catch (CodeGenFailedException e) {
			throw new SAXException( e );
		}
	}

	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		m_validator.doEndElementValidation(m_currentSchemaNode);
		if(SCHEMA.equals( localName ) ){
			m_targetNamespace = null;
		}
		m_currentSchemaNode = m_currentSchemaNode.getParentNode();
	}


	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(SCHEMA.equals( localName ) ){
			m_targetNamespace = getAttribute(attributes, "targetNamespace" );
		}
		SchemaNode lastNode = m_currentSchemaNode;
		m_currentSchemaNode = new SchemaNode();
		m_currentSchemaNode.setNodeName(localName);
		lastNode.getChildNodes().add( m_currentSchemaNode );
		m_currentSchemaNode.setParentNode(lastNode);
		m_currentSchemaNode.setLineNumber(locator.getLineNumber());
		m_currentSchemaNode.setColumnNumber(locator.getColumnNumber());
		m_currentSchemaNode.setTargetNamespace(m_targetNamespace);
		m_currentSchemaNode.setFileName(m_fileName);

		//if m_targetNamespace is not null, then the current element would be a schema 
		//or a child node of schema
		//hence it is good to resolve attributes.
		mapAttributes( m_currentSchemaNode, attributes, m_targetNamespace != null );
		updateMap( m_currentSchemaNode );

		captureLibraryInfo( m_currentSchemaNode );
		m_validator.doStartElementValidation(m_currentSchemaNode);
		
	}

	private void updateMap(SchemaNode currentNode){
		m_mapOfAllNodes.updateMap(currentNode);
	}

	/**
	 * Converts the attributes collection to collection of SchemaNodeAttribute.
	 * The frequent attributes like base, type, ref are resolved to QName here.
	 * The attributes gets resolved only if resolveAttributes is true.
	 * 
	 * 
	 * @param node
	 * @param attributes
	 * @param resolveAttributes
	 */
	private void mapAttributes(SchemaNode node, Attributes attributes, boolean resolveAttributes){
		for( int i=0; i<attributes.getLength(); i++){
			String localName = attributes.getLocalName(i);
			String value = attributes.getValue(i);
			
			SchemaNodeAttribute attr = new SchemaNodeAttribute();
			attr.setAttributeName(localName);
			attr.setAttributeValue(value);

			node.getAttributes().add(attr);

			//The following section resolves the attributes for the prefix and converts to QName.
			//The code will work good only for the attributes defined in schema node and its child nodes
			//the flag resolveAttributes should say whether 
			if( !resolveAttributes ){
				continue ;
			}

			//value cannot be null.
			if( CodeGenUtil.isEmptyString(value) ){
				String msg = "The value attribute is empty on node '"
						+ node.getNodeName() + "', attribute name '"
						+ localName + "' . Is the wsdl valid?";
				getLogger().log(Level.SEVERE, msg);
				
				//it is not good to throw exception here
				//because many attributes are valid with out any value.
				//throw new RuntimeException(msg);
				continue;
			}
			if(ABSTRACT.equals(localName)){
				node.setAbstractAttrExists(true);
				node.setAbstractAttrValue(Boolean.parseBoolean(value));

			}else if(MAXOCCURS.equals(localName) ){
				node.setMaxoccursAttrExists(true);
				node.setMaxoccursAttrValue(value);

			}else if(NAMEATTR.equals(localName) ){
				node.setNameAttrExists(true);
				node.setNameAttrValue(value);

			}else if(BASE.equals(localName) ){
				node.setBaseAttrExists(true);
				node.setBaseAttrValue( resolveAttributeValue( value ) );

			}else if(REF.equals(localName) ){
				node.setRefAttrExists(true);
				node.setRefAttrValue( resolveAttributeValue( value ) );

			}else if(TYPEATTR.equals(localName) ){
				node.setTypeAttrExists(true);
				node.setTypeAttrValue( resolveAttributeValue( value ) );

			}else if(ITEMTYPE.equals(localName) ){
				node.setItemTypeAttrExists(true);
				node.setItemTypeAttrValue( resolveAttributeValue( value ) );

			}
		}
	}

	private QName resolveAttributeValue(String attributeValue){
		if(CodeGenUtil.isEmptyString(attributeValue)){
			return null;
		}
		String prefix = "";
		String value = attributeValue;
		int colanIndex = attributeValue.indexOf( ":" );
		if(colanIndex == 0){
			String msg = "The value '"+attributeValue+"' does not have proper prefix. If colan is used the prefix should be atleast one character.";
			getLogger().log(Level.SEVERE, msg);
			throw new RuntimeException(msg);

		}else if( colanIndex > 0 ){
			prefix = attributeValue.substring(0, colanIndex);
			value = attributeValue.substring(colanIndex + 1);
		}
		
		prefix = prefix.trim();
		value = value.trim();

		NamespaceNode namespace = m_prefix2NamespaceLink.get(prefix);
		if( namespace == null ){
			getLogger().log(Level.SEVERE, "The resolved prefix '"+prefix+"' is invalid. Could not find namespace from available map " );
			getLogger().log(Level.SEVERE, "The available values from map are " + m_prefix2NamespaceLink );

			throw new RuntimeException("The resolved prefix '"+prefix+"' is invalid. Could not find namespace from available map " + m_prefix2NamespaceLink);
		}

		QName qName = new QName( namespace.currentNamespace, value );
		getLogger().log(Level.INFO, "Resolved '"+attributeValue+"' as " + qName);
		return qName;
		
	}

	private String getAttribute(Attributes attributes, String attributeName){
		String value = null;
		for( int i=0; i<attributes.getLength(); i++){
			String localName = attributes.getLocalName(i);
			if(attributeName.equals(localName) ){
				value = attributes.getValue(i);
				break;
			}
		}
		return value;
	}
	
	/**
	 * Captures the typeLibrarySource tag if present.
	 * 
	 * @param node
	 */
	private void captureLibraryInfo( SchemaNode node ){
		if(TYPE_LIB_SOURCE.equals(node.getNodeName())){

			if( !isValidTypeLibNode( node ) ){
				return ;
			}
		
			SchemaNode parent = getSurroundingType(node);
			String libraryName = null;
			String namespace = null;
			for( SchemaNodeAttribute attr : node.getAttributes() ){
				if(LIBRARY.equals( attr.getAttributeName() ) ){
					libraryName = attr.getAttributeValue();
					continue;
				}
				if(NAMESPACEATTR.equals( attr.getAttributeName() ) ){
					namespace = attr.getAttributeValue();
					continue;
				}				
			}
			
			if( namespace != null && libraryName!= null ){
				SchemaNodeLibraryInfo libInfo = new SchemaNodeLibraryInfo();
				libInfo.setLibraryName(libraryName);
				libInfo.setNamespace(namespace);
				parent.setLibraryInfo(libInfo);
			}
		}
	}

	private boolean isValidTypeLibNode( SchemaNode node ){
		if(node.getParentNode() == null || node.getParentNode().getParentNode() == null ){
			return false;
		}
		if (APPINFO_TAG.contains( node.getParentNode().getNodeName() )
				&& ANNOTATION_TAG.contains(node.getParentNode().getParentNode().getNodeName() )){
			return true;
		}
		return false;
	}
	private SchemaNode getSurroundingType( SchemaNode node ){
		SchemaNode parent = node.getParentNode();
		while (parent != null){
			String parentNodename = parent.getNodeName();
			
			if(SchemaConstuctConstants.COMPLEXTYPE.equals(parentNodename) || 
					SchemaConstuctConstants.SIMPLETYPE.equals(parentNodename) 
					){
				return parent;
			}
			parent = parent.getParentNode();
		}
		return null;
	}

	/**
	 * While SAX parsing, prefix2Namespace map can be defined at definitions level and 
	 * at schema level.
	 * The scope of prefix defined at definitions level is global i.e. applicable for entire wsdl.
	 * The scope of prefix defined at schema level is applicable only schema node.
	 * 
	 * Also, for the same prefix, namespace can be defined at definitions level and at schema level.
	 * So the prefix resolver should be intelligent enough to handle prefix2Namespace Map.
	 * 
	 * To achieve that, NamespaceNode contains the namespace value and the reference to previous namespace.
	 * 
	 * @author rkulandaivel
	 *
	 */
	private static class NamespaceNode{
		public String currentNamespace = null;
		public NamespaceNode previous = null;
		@Override
		public String toString() {
			return "NamespaceNode [currentNamespace=" + currentNamespace + "]";
		}
		
	}
}
