/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.runtime.codegen.common.SchemaNode;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator.FastSerFormatValidationHandler.Util;

/**
 * @author rkulandaivel
 * 
 * This class collects schema node information of the wsdl/xsds for post validation.
 * The class is called by SchemaParserEventHandler to update the collection maintained by this class.
 * 
 * This class creates separate map for each type because name of a complex type can be same as element name.
 * also it can be same as group type name.
 */
public class SchemaNodeRepresentationByType implements SchemaConstuctConstants {

	private static final List<SchemaNode> EMPTY_LIST = new ArrayList<SchemaNode>();

	private Map<String, List<SchemaNode>> m_mapOfSchemaNodes = new HashMap<String, List<SchemaNode>>();
	private Map<String, List<SchemaNode>> m_mapOfRootLevelSchemaNodes = new HashMap<String, List<SchemaNode>>();

	private Map<QName, SchemaNode> m_mapOfAllComplexAndSimpleTypes = new HashMap<QName, SchemaNode>();
	private Map<QName, SchemaNode> m_mapOfRootAttributeTypes = new HashMap<QName, SchemaNode>();
	private Map<QName, SchemaNode> m_mapOfRootAttributeGroupTypes = new HashMap<QName, SchemaNode>();
	private Map<QName, SchemaNode> m_mapOfRootElementTypes = new HashMap<QName, SchemaNode>();
	private Map<QName, SchemaNode> m_mapOfGroupTypes = new HashMap<QName, SchemaNode>();

	public SchemaNodeRepresentationByType() {
	}

	
	private void updateRootLevelNodesMap(SchemaNode currentNode){
		List<SchemaNode> nodes = m_mapOfRootLevelSchemaNodes.get( currentNode.getNodeName() );
		if( nodes == null){
			nodes = new ArrayList<SchemaNode>();
			m_mapOfRootLevelSchemaNodes.put(currentNode.getNodeName(), nodes);
		}

		nodes.add( currentNode );
		if( currentNode.isNameAttrExists() ){
			Map<QName, SchemaNode> map = getMapToUpdateForRootLevelTypes( currentNode );
			if( map != null ){
				map.put(new QName( currentNode.getTargetNamespace(), currentNode.getNameAttrValue() ), currentNode);	
			}
		}
	}
	public void updateMap(SchemaNode currentNode){
		List<SchemaNode> nodes = m_mapOfSchemaNodes.get( currentNode.getNodeName() );
		if( nodes == null){
			nodes = new ArrayList<SchemaNode>();
			m_mapOfSchemaNodes.put(currentNode.getNodeName(), nodes);
		}

		nodes.add( currentNode );
		
		Map<QName, SchemaNode> map = getMapToUpdate( currentNode );
		if( map != null &&  currentNode.isNameAttrExists() ){
			map.put(new QName( currentNode.getTargetNamespace(), currentNode.getNameAttrValue() ), currentNode);		
		}

		if( Util.isRootNodeInSchema( currentNode ) ){
			updateRootLevelNodesMap( currentNode );
		}
	}
	private Map<QName, SchemaNode> getMapToUpdateForRootLevelTypes(SchemaNode currentNode){
	
		if(Util.isInValidNodeName(currentNode, ELEMENT) ){
			return m_mapOfRootElementTypes;
		}
		if(Util.isInValidNodeName(currentNode, ATTRIBUTE) ){
			return m_mapOfRootAttributeTypes;
		}
		if(Util.isInValidNodeName(currentNode, ATTRIBUTE_GROUP) ){
			return m_mapOfRootAttributeGroupTypes;
		}
		if(Util.isInValidNodeName(currentNode, GROUP) ){
			return m_mapOfGroupTypes;
		}
		return null;
	}
	private Map<QName, SchemaNode> getMapToUpdate(SchemaNode currentNode){
		if(Util.isInValidNodeName(currentNode, COMPLEXTYPE) 
				|| Util.isInValidNodeName(currentNode, SIMPLETYPE)
				){
			return m_mapOfAllComplexAndSimpleTypes;
		}
		return null;
	}
	public List<SchemaNode> getSchemaNodesList( String nodeName ){
		List<SchemaNode> nodes = m_mapOfSchemaNodes.get(nodeName);
		if( nodes == null ){
			nodes = EMPTY_LIST;
		}
		return nodes;
	}
	public List<SchemaNode> getRootLevelSchemaNodesList( String nodeName ){
		List<SchemaNode> nodes = m_mapOfRootLevelSchemaNodes.get(nodeName);
		if( nodes == null ){
			nodes = EMPTY_LIST;
		}
		return nodes;
	}

	public SchemaNode getType(QName typeQName){
		return m_mapOfAllComplexAndSimpleTypes.get(typeQName);
	}
	
	public SchemaNode getRootElementNode(QName typeQName){
		return m_mapOfRootElementTypes.get(typeQName);
	}
	
	public SchemaNode getRootAttributeNode(QName typeQName){
		return m_mapOfRootAttributeTypes.get(typeQName);
	}

	public SchemaNode getRootAttributeGroupNode(QName typeQName){
		return m_mapOfRootAttributeGroupTypes.get(typeQName);
	}

	public SchemaNode getRootGroupNode(QName typeQName){
		return m_mapOfGroupTypes.get(typeQName);
	}

	public List<SchemaNode> getComplexTypeNodes(){
		return getSchemaNodesList(COMPLEXTYPE);
	}
}
