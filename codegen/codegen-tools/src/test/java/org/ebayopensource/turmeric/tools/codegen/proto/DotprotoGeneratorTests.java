/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.proto;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.ebayopensource.turmeric.tools.TestResourceUtil;
import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.WSDLParserException;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.FastSerFormatCodegenBuilder;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.ProtobufSchemaMapper;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.dotproto.DotProtoGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufEnumMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufField;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufSchema;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.SchemaTypeName;
import org.junit.Test;

/**
 * @author anav
 *
 */
public class DotprotoGeneratorTests extends AbstractServiceGeneratorTestCase{
	
	@Override
	public File getProtobufRelatedInput(String name) {
		return TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/proto/"
				+ name);
	}
	
	
	public static String[] getEmptyWsdlArgs(File wsdl,File destDir) {
		File binDir = new File(destDir, "bin");
		String testArgs[] = new String[] {
				"-servicename",
				"FindItemServiceEmpty",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "ClientNoConfig", "-src",destDir.getAbsolutePath(),
				"-dest", destDir.getAbsolutePath(), "-scv", "1.0.0", "-bin", binDir.getAbsolutePath(),
				 "-enabledNamespaceFolding",
				"-nonXSDFormats", "protobuf" };
		return testArgs;
	}

	@Test
	public void testDotprotoGenerationForEmptyWSDL() throws Exception {
		
		File wsdl1 = getProtobufRelatedInput("FindItemServiceEmpty.wsdl");
		File destDir = testingdir.getDir();
		
		String protoPath = destDir.getAbsolutePath() + "\\meta-src\\META-INF\\soa\\services\\proto\\FindItemServiceEmpty\\FindItemServiceEmpty.proto";
		File emptyProto = new File( protoPath );
		if(emptyProto.exists() ){
			emptyProto.delete();
		}

		
		CodeGenContext context = ProtobufSchemaMapperTestUtils.getCodeGenContext( getEmptyWsdlArgs(wsdl1,destDir) );
		
		FastSerFormatCodegenBuilder.getInstance().validateServiceIfApplicable(context);
		
		List<SchemaType> listOfSchemaTypes;
		try {
			listOfSchemaTypes = FastSerFormatCodegenBuilder.getInstance().generateSchema( context );
		} catch (WSDLParserException e) {
			Assert.fail( "Generate Schema Failed." + e.getMessage() );
			throw e;
		}
		
		ProtobufSchema schema = ProtobufSchemaMapper.getInstance().createProtobufSchema(listOfSchemaTypes, context);

		try {
			DotProtoGenerator.getInstance().generate(schema, context);
		} catch (Exception e1) {
			Assert.fail( "Dot Proto generation failed." + e1.getMessage() );
			throw e1;
		}
			
		File dir = new File( schema.getDotprotoTargetDir() );
		File dotproto = new File(dir, schema.getDotprotoFileName() );
		
		if( !protoPath.equals( dotproto.getPath() ) ){
			Assert.fail("The file paths are different. Expected path =" + protoPath + ". Generated path"+ dotproto.getPath());
		}
		if(dotproto.exists() ){
			ProtobufSchemaMapperTestUtils.loadFindItemServiceManuallyWrittenProtoFile( dotproto.getPath() );
		}
	} 
	
	public static String[] getTestAWsdlArgs(File wsdl,File destDir) {
		
		File binDir = new File(destDir,"bin");
		String testArgs[] = new String[] {
				"-servicename",
				"FindItemService",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "ClientNoConfig", "-src",destDir.getAbsolutePath(),
				"-dest", destDir.getAbsolutePath(), "-scv", "1.0.0", "-bin", binDir.getAbsolutePath(),
				"-nonXSDFormats", "protobuf" };
		return testArgs;
	}

	@Test
	public void testDotprotoGeneration() throws Exception {
		

		File wsdl1 = getProtobufRelatedInput("FindItemServiceAdjustedV3.wsdl");
		File destDir = testingdir.getDir();
		
		CodeGenContext context = ProtobufSchemaMapperTestUtils.getCodeGenContext( getTestAWsdlArgs(wsdl1,destDir) );
		
		List<SchemaType> listOfSchemaTypes;
		try {
			listOfSchemaTypes = FastSerFormatCodegenBuilder.getInstance().generateSchema( context );
		} catch (WSDLParserException e) {
			throw new CodeGenFailedException( "Generate Schema Failed.", e );
		}
		
		ProtobufSchema schema = ProtobufSchemaMapper.getInstance().createProtobufSchema(listOfSchemaTypes, context);

		try {
			DotProtoGenerator.getInstance().generate(schema, context);
		} catch (Exception e1) {
			throw new CodeGenFailedException("Dot Proto generation failed.", e1);
		}
		
		String dotprotofilepath = getProtobufRelatedInput("FindItemServiceAdjustedV3.proto").getAbsolutePath();
		List<ProtobufMessage> messagesFromFile = ProtobufSchemaMapperTestUtils.loadFindItemServiceManuallyWrittenProtoFile( dotprotofilepath );
		
		updateMessagesLoadedFromFile( messagesFromFile, context, "com.ebay.marketplace.search.v1.services" );
		
		File dir = new File( schema.getDotprotoTargetDir() );
		File dotproto = new File(dir, schema.getDotprotoFileName() );
		
		System.out.println(dotproto.getPath());
		List<ProtobufMessage> generatedMessages = ProtobufSchemaMapperTestUtils.loadFindItemServiceManuallyWrittenProtoFile( dotproto.getPath() );
		
		updateMessagesLoadedFromFile( generatedMessages, context, "com.ebay.marketplace.search.v1.services" );
		System.out.println(messagesFromFile);
		System.out.println(generatedMessages);
		System.out.println("done");
		validateTheSchema(generatedMessages, context, messagesFromFile);
	}
	
	
	private void updateMessagesLoadedFromFile(List<ProtobufMessage> messagesFromFile, CodeGenContext context, String basePackage ){
		for( ProtobufMessage message : messagesFromFile ){
			String msgName = message.getMessageName();
			String typeName = msgName;
			if( message.isEnumType() ){
				typeName = ((ProtobufEnumMessage)message).getEnumMessageName();
			}
			message.setJaxbClassName(basePackage + "." + msgName);
			message.setEprotoClassName(basePackage + ".proto.extended.E" + msgName);
			
			if( message.isEnumType() ){
				message.setJprotoClassName(basePackage + ".proto.FindItemService$" + typeName + "$"+msgName);
			}else{
				message.setJprotoClassName(basePackage + ".proto.FindItemService$" + typeName);
			}
			for(ProtobufField field : message.getFields()){
				field.setTypeOfField( ProtobufSchemaMapperTestUtils.getFieldType(field) );
			}

			
			String serviceNamespace = context.getNamespace();


			//Test type FieldValue
			QName fieldValName = new QName(serviceNamespace, msgName);
			SchemaTypeName fieldValueTypeName = new SchemaTypeName( fieldValName );
			message.setSchemaTypeName(fieldValueTypeName);
		}
	}
	
	private void validateTheSchema(List<ProtobufMessage> generatedMessages, CodeGenContext context, List<ProtobufMessage> messagesFromFile) throws Exception{
		Map<SchemaTypeName, ProtobufMessage> schemaTypeMap = ProtobufSchemaMapperTestUtils.createMessageMapFromList( generatedMessages );

		
		for( ProtobufMessage message : messagesFromFile ){
			ProtobufMessage messageFromModel = schemaTypeMap.get(message.getSchemaTypeName());
			if(messageFromModel == null){
				throw new Exception("The model does not have an message corresponding to name " + message.getSchemaTypeName());
			}
			boolean equal = false;
			if( message instanceof ProtobufEnumMessage){
				equal = ProtobufSchemaMapperTestUtils.ProtobufMessageComparator.compareEnumMessage((ProtobufEnumMessage)message, (ProtobufEnumMessage)messageFromModel);
			}else{
				equal = ProtobufSchemaMapperTestUtils.ProtobufMessageComparator.compareMessage(message, messageFromModel);				
			}

			if(!equal){
				System.out.println(message);
				System.out.println(messageFromModel);
				throw new Exception("The proto buf message generated for " + message.getSchemaTypeName() + " has some issues.");
			}
		}
	}
	
}
