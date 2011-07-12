/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.InputOptions;
import org.ebayopensource.turmeric.tools.codegen.external.WSDLOperationType;
import org.ebayopensource.turmeric.tools.codegen.external.WSDLUtil;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ComplexType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ElementType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.GroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.exception.ProtobufModelGenerationFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufEnumMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufField;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufFieldType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufOption;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufOptionType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufSchema;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeMap;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeName;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenConstants;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;

/**
 * @author rkulandaivel
 *
 */
public class ProtobufSchemaMapper {
	private static final String DOT_PROTO_FILE_EXTENSION = ".proto";
	private static final String PROTO_RELATIVE_PATH = CodeGenUtil.toOSFilePath("META-INF/soa/services/proto/");

	private static final ProtobufSchemaMapper s_instance = new ProtobufSchemaMapper();
	private static Logger s_logger = LogManager.getInstance(ProtobufSchemaMapper.class);
	private ProtobufSchemaMapper(){
		
	}

	public static ProtobufSchemaMapper getInstance(){
		return s_instance;
	}

	private static Logger getLogger(){
		return s_logger;
	}

	public ProtobufSchema createProtobufSchema(List<SchemaType> schemaTypes,
			CodeGenContext codeGenContext) throws ProtobufModelGenerationFailedException{
		ProtobufSchema schema = new ProtobufSchema();
		
		schema.setDotprotoFileName( codeGenContext.getServiceAdminName() + DOT_PROTO_FILE_EXTENSION);

		Map<String, String> ns2PkgMap = getNS2PkgMap(codeGenContext);
		String serviceNamesapce = codeGenContext.getNamespace();

		String pkfFromServiceNamespace = getPackageFromNamespace(serviceNamesapce, ns2PkgMap);
		schema.setDotprotoFilePackage( pkfFromServiceNamespace );
		
		SchemaTypeMap schemaTypeMap = SchemaTypeMap.createSchemaTypeMapFromList(schemaTypes);

		MapperInstanceProvider mapperInstanceProvider = MapperInstanceProvider
				.createMapperInstanceProvider(schemaTypeMap);

		Map<SchemaTypeName, ProtobufMessage> protoMessageMap = createProtoMessageForEachSchemaType(
				schemaTypeMap, mapperInstanceProvider, codeGenContext);

		populateClassNamesForAllMessages(schema, protoMessageMap,
				pkfFromServiceNamespace, codeGenContext, ns2PkgMap);

		schema.getMessages().addAll( protoMessageMap.values() );

		ProtobufOption option = new ProtobufOption();
		option.setOptionType( ProtobufOptionType.JAVA_PACKAGE_NAME );
		option.setOptionValue(pkfFromServiceNamespace + ".proto");
		schema.getDotprotoOptions().add( option );

		option = new ProtobufOption();
		option.setOptionType( ProtobufOptionType.JAVA_OUTER_CLASS_NAME );
		option.setOptionValue(codeGenContext.getServiceAdminName());
		schema.getDotprotoOptions().add( option );

		option = new ProtobufOption();
		option.setOptionType( ProtobufOptionType.OPTIMIZE_FOR );
		option.setOptionValue( "SPEED" );
		schema.getDotprotoOptions().add( option );

		
		//Following block finds the destination directory for dot proto file.
		//The path is <ProjectRoot>/meta-src/META-INF/soa/services/proto/<ServiceAdminName>/ServiceAdminName.proto.
		
		String destRootPath = null;
		InputOptions inputOptions = codeGenContext.getInputOptions();
		
		String projectRoot = inputOptions.getProjectRoot();
		
		if(!CodeGenUtil.isEmptyString(projectRoot)){
			destRootPath = projectRoot;
		}
		else {
			destRootPath = inputOptions.getDestLocation();
		}
		
		//inputOptions.getDestLocation() would never be empty
		//this block of code can be depricated but required for turmeric
		if(CodeGenUtil.isEmptyString(destRootPath) ){
			String mDestRootPath = CodeGenUtil.toOSFilePath( codeGenContext.getMetaSrcDestLocation() );
			String genMetaSrcPath = CodeGenUtil.toOSFilePath( CodeGenConstants.GEN_META_SRC_FOLDER );
			String metaSrcPath = CodeGenUtil.toOSFilePath( CodeGenConstants.META_SRC_FOLDER );
			
			//the mdest path might end with "gen-meta-src" so remove it
			int indexOfMetaSrcPath = mDestRootPath.indexOf( genMetaSrcPath );
			
			//the mdest path might end with "meta-src" so remove it
			if( indexOfMetaSrcPath < 0 ){
				indexOfMetaSrcPath = mDestRootPath.indexOf( metaSrcPath );
			}
			
			//impossible case. still have a check to avoid runtime exception 
			//the mdest path does not end with gen-meta-src or meta-src. so ignore it
			if( indexOfMetaSrcPath < 0 ){
				indexOfMetaSrcPath = mDestRootPath.length();
			}
			
			destRootPath = mDestRootPath.substring(0, indexOfMetaSrcPath);
		}
			
		
		String metaSrcPath    = CodeGenUtil.toOSFilePath(destRootPath) + CodeGenConstants.META_SRC_FOLDER;

		String inputPath = CodeGenUtil.toOSFilePath( metaSrcPath );// CodeGenUtil.toOSFilePath( codeGenContext.getMetaSrcDestLocation() );
		
		inputPath = inputPath + PROTO_RELATIVE_PATH + inputOptions.getServiceAdminName() ;
		
		inputPath = CodeGenUtil.toOSFilePath( inputPath ) ;

		try {
			CodeGenUtil.createDir(inputPath);
			schema.setDotprotoTargetDir(inputPath);
		} catch (IOException e) {
			throw new ProtobufModelGenerationFailedException("Target Directory creation failed for proto destination path", e);
		}
		return schema;
	}
	
	private Map<SchemaTypeName, ProtobufMessage> createProtoMessageForEachSchemaType(
			SchemaTypeMap schemaTypeMap,
			MapperInstanceProvider mapperInstanceProvider,
			CodeGenContext codeGenContext) throws ProtobufModelGenerationFailedException{
		
		Map<SchemaTypeName, ProtobufMessage> protoMessageMap = new HashMap<SchemaTypeName, ProtobufMessage>();
		
		createProtoMessageForEachSchemaType(schemaTypeMap
				.getAllComplexAndSimpleTypes(), mapperInstanceProvider,
				codeGenContext, protoMessageMap);
		
		createProtoMessageForEachSchemaType(schemaTypeMap.getAllElementTypes(), mapperInstanceProvider,
				codeGenContext, protoMessageMap);
		
		return protoMessageMap;
	}
	private void createProtoMessageForEachSchemaType(
			Map<SchemaTypeName, SchemaType> schemaTypeMap,
			MapperInstanceProvider mapperInstanceProvider,
			CodeGenContext codeGenContext,
			Map<SchemaTypeName, ProtobufMessage> protoMessageMap) throws ProtobufModelGenerationFailedException{
		
		
		
		for(Map.Entry<SchemaTypeName, SchemaType> entry : schemaTypeMap.entrySet()){
			SchemaTypeName schemaTypeName = entry.getKey();
			
			ProtobufMessage protoMessage = createProtoMessage( entry.getKey(), entry.getValue(), mapperInstanceProvider );
			
			if(protoMessage != null){
				protoMessageMap.put(schemaTypeName, protoMessage);
			}
		}

	
	}

	private ProtobufMessage createProtoMessage(SchemaTypeName schemaTypeName, SchemaType schemaType, MapperInstanceProvider mapperInstanceProvider) throws ProtobufModelGenerationFailedException{
		ProtobufMessage protoMessage = null;

		if( (schemaType instanceof AttributeGroupType) || 
				(schemaType instanceof GroupType)  ){
			return null;
		}
		if(schemaType instanceof ComplexType){
			ComplexType complexType = (ComplexType)schemaType;
			if( complexType.isAbstract() ){
				return null;
			}
			
			protoMessage = mapperInstanceProvider.getComplexTypeMapper().createProtobufMessage(complexType);
			
		}else if( schemaType instanceof SimpleType){
			SimpleType simpleType = (SimpleType)schemaType;
			if( (simpleType.getRestriction() != null) && (simpleType.getRestriction().getEnumerations().size() > 0) ){
				protoMessage = mapperInstanceProvider.getSimpleTypeMapper().createEnumProtoMessage(schemaTypeName, simpleType);
			}
			
		}else if( schemaType instanceof ElementType ){
			ElementType elementType = (ElementType)schemaType;
			if(elementType.getElementType() == null){
				if( elementType.hasSimpleType() ){
					protoMessage = createProtoMessage( schemaTypeName, elementType.getSimpleType(), mapperInstanceProvider);
				}else if( elementType.hasComplexType() ){
					protoMessage = createProtoMessage( schemaTypeName, elementType.getComplexType(), mapperInstanceProvider);
				}
			}
		}
		
		return protoMessage;
	}

	private void populateClassNamesForAllMessages(ProtobufSchema schema,
			Map<SchemaTypeName, ProtobufMessage> protoMessageMap,
			String pkfFromServiceNamespace,
			CodeGenContext codeGenContext, Map<String, String> ns2PkgMap) {

		String jprotoPackage = pkfFromServiceNamespace + ".proto";
		String eprotoPackage = jprotoPackage + ".extended";
		String jprotoOuterClassName = jprotoPackage + "." + codeGenContext.getServiceAdminName();
		
		schema.setJProtoOuterClassName(jprotoOuterClassName);
		Set<QName> rootTypes = new HashSet<QName>();
		getLogger().log(Level.INFO, "Input file path "+codeGenContext.getInputOptions());

		Map<String, WSDLOperationType> wsdlOperations = WSDLUtil
				.getWSDLOparations(codeGenContext.getInputOptions().getInputFile(), codeGenContext);
		for( WSDLOperationType operationType : wsdlOperations.values() ){
			
			if( operationType.getInMessage() != null ){
				QName typeName = operationType.getInMessage().getSchemaTypeQName();
				rootTypes.add(typeName);
		     }

			if( operationType.getOutMessage() != null ){
				QName typeName = operationType.getOutMessage().getSchemaTypeQName();
				rootTypes.add(typeName);
			}

			//TODO what about faults?
		}
		
		
		for(Map.Entry<SchemaTypeName, ProtobufMessage> entry : protoMessageMap.entrySet()){
			SchemaTypeName typeName = entry.getKey();
			ProtobufMessage message = entry.getValue();
			QName currTypeQName = typeName.getTypeName();

			
			String NS = currTypeQName.getNamespaceURI();
			String pkg = getPackageFromNamespace(NS, ns2PkgMap );
			
			
			if (CodeGenUtil.isEmptyString(pkg)) {
				pkg = WSDLUtil.getPackageFromNamespace(NS);
			}

			String messageName = message.getMessageName();
			String javaTypeFullName = pkg + "."	+ messageName ;
			message.setJaxbClassName(javaTypeFullName);

			message.setEprotoClassName(eprotoPackage + ".E"+ messageName);
			message.setJprotoClassName(jprotoOuterClassName + "$" + messageName);

			if(message.isEnumType()){
				message.setJprotoClassName( message.getJprotoClassName() + "Enum$" + messageName);
			}

			if(rootTypes.contains(currTypeQName) ){
				message.setRootType(true);
			}

			for(ProtobufField field : message.getFields()){
				QName xsdType = field.getXsdTypeName();

				if(field.getTypeOfField() == ProtobufFieldType.COMPLEX_TYPE){
					SchemaTypeName complexTypeName = new SchemaTypeName(xsdType);
					ProtobufMessage complexTypeMessage = protoMessageMap.get(complexTypeName);
					field.setProtobufTypeName( complexTypeMessage.getMessageName() );

				}else if(field.getTypeOfField() == ProtobufFieldType.ENUM_TYPE){
					SchemaTypeName enumTypeName = new SchemaTypeName(xsdType);
					ProtobufEnumMessage enumTypeMessage = (ProtobufEnumMessage)protoMessageMap.get(enumTypeName);
					field.setProtobufTypeName( enumTypeMessage.getEnumMessageName() + "." + enumTypeMessage.getMessageName() );

				}else if(InBuiltType2ProtobufTypeMap.isValidInBuiltType( xsdType ) ){
					field.setProtobufTypeName( InBuiltType2ProtobufTypeMap.getProtoType(xsdType) );
				}

			}
		}
	}
	
	private Map<String, String> getNS2PkgMap(CodeGenContext codeGenCtx) {

		return WSDLUtil.getNS2PkgMappings(codeGenCtx.getInputOptions());
	}
	
	private String getPackageFromNamespace( String nameSpace, Map<String, String> ns2PkgMap){
		String pkg = ns2PkgMap.get(nameSpace);
		
		
		if (CodeGenUtil.isEmptyString(pkg)) {
			pkg = WSDLUtil.getPackageFromNamespace(nameSpace);
		}
		
		return pkg;
	}
}
