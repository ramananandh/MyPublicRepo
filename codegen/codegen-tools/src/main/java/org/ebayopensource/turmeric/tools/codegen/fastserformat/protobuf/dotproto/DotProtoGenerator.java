/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.dotproto;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
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
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.ProtobufArtifactsGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufEnumEntry;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufEnumMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufField;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufSchema;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.DefaultProtobufTagGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufMetadataFileWriter;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufMetadataWriter;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufTagGenerator;

/**
 * @author rkulandaivel
 * 
 * This class creates the Dot proto file, from the protobuf schema.
 * Creates a single file for the entire schema object.
 * The file is created with path as 
 * <ProjectRoot>/meta-src/META-INF/soa/services/proto/<ServiceAdminName>/ServiceAdminName.proto
 * 
 * 
 */
public class DotProtoGenerator {
	private static Logger s_logger = LogManager
	.getInstance(ProtobufArtifactsGenerator.class);

	private static DotProtoGenerator s_instance = new DotProtoGenerator();

	private Logger getLogger() {
		return s_logger;
	}
	public static DotProtoGenerator getInstance(){
		return s_instance;
	}

	/**
	 * Creates the proto file under path <ProjectRoot>/meta-src/META-INF/soa/services/proto/<ServiceAdminName>/ServiceAdminName.proto.
	 * It uses input option dest location as base path.
	 * If location is empty, then it uses project root  
	 * 
	 * Before it writes the file, it generates the sequence number for each message with the help of ProtobufTagGenerator.
	 * 
	 * 
	 * @param schema
	 * @param codeGenContext
	 * @throws CodeGenFailedException
	 */
	public void generate( ProtobufSchema schema, CodeGenContext codeGenContext ) throws CodeGenFailedException{
		
		FileContentWriter writer;
		try {
			writer = new FileContentWriter( new File( schema.getDotprotoTargetDir() ) );
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to create File Content writer", e);
			throw new CodeGenFailedException("Failed to create File Content writer", e);
		}

		getLogger().log(Level.INFO, "Start updating sequence numbers");
		updateSchemaWithSequenceNumbers( schema, codeGenContext );

		getLogger().log(Level.INFO, "Start generating dot proto file");
		try {
			build(writer, schema);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to write the dot proto file", e);
			throw new CodeGenFailedException("Failed to write the dot proto file", e);
		}		
	}

	/**
	 * This method is responsible for two things.
	 * 1. Updates all the fields with the sequence number generated.
	 * 2. get the tags to be persisted from   ProtobufTagGenerator, and persist in protobuf schema.
	 * 
	 * @param schema
	 * @param codeGenContext
	 * @throws CodeGenFailedException
	 */
	private void updateSchemaWithSequenceNumbers( ProtobufSchema schema, CodeGenContext codeGenContext) throws CodeGenFailedException{
		String wsdlFile = codeGenContext.getInputOptions().getInputFile();
		File protoFile = new File( schema.getDotprotoTargetDir(), schema.getDotprotoFileName() );

		if( protoFile.exists() && !protoFile.canWrite() ){
			String message = "The dot proto file under the path '"+protoFile.getPath()+"' is not writable. Please make the file writable.";
			getLogger().log(Level.SEVERE, message);
			throw new CodeGenFailedException(message);
		}
		ProtobufTagGenerator generator;
		try {
			generator = new DefaultProtobufTagGenerator(new File(wsdlFile),
					protoFile);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Protobuf tag generator creation failed.", e);
			throw new CodeGenFailedException("Protobuf tag generator creation failed.", e);
		}
		
		try {
			updateSchemaWithSequenceNumbers( schema, generator, false );
		} catch (Exception e1) {
			getLogger().log(Level.SEVERE, "Protobuf Sequence number retreival and updation failed.", e1);
			throw new CodeGenFailedException("Protobuf Sequence number retreival and updation failed.", e1);
		}

		
		ByteArrayOutputStream outputStream;
		try {
			outputStream = new ByteArrayOutputStream();
			ProtobufMetadataWriter metadataWriter = new ProtobufMetadataFileWriter();
			metadataWriter.write(generator.getTagsToPersist(), outputStream );
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Protobuf metadata writer failed.", e);
			throw new CodeGenFailedException("Protobuf metadata writer failed.", e);
		}
		
		//persist the metadata bytes which will written at the end of proto file.
		schema.setMetadataBytes( outputStream.toByteArray() );
	}

	/**
	 * Updates the Sequence number for each field in all messages with the help of ProtobufTagGenerator.
	 * If strictUpdate and if any of the following occurs, exception is thrown.
	 * 1. If the field name returned by the  ProtobufTagGenerator does not match with any of the fields in ProtobufMessage.
	 * 2. If one or more fields from the message schema does not have tag numbers generated.
	 * 
	 * For #1, exception is thrown immediately.
	 * But for #2, exception is thrown at last by collecting all the fields which does not have tag numbers generated.
	 * 
	 * @param schema
	 * @param codeGenContext
	 * @param writer
	 * @throws CodeGenFailedException 
	 */
	public void updateSchemaWithSequenceNumbers( ProtobufSchema schema, ProtobufTagGenerator generator, boolean strictUpdate ) throws CodeGenFailedException{


		Map<QName, Set<String>> fieldsDoesNotHaveSeqNo = new HashMap<QName, Set<String>>();

		for( ProtobufMessage message : schema.getMessages() ){
			QName typeName = message.getSchemaTypeName().getTypeName();

			Map<String, Integer> tagsForType = generator.getTagsForType( typeName );

			if(message.isEnumType()){
				Set<String> enumFieldsDontHaveSeqNo = new HashSet<String>();

				for( ProtobufEnumEntry enumField : ((ProtobufEnumMessage)message).getEnumEntries() ) {
					Integer seqNo = tagsForType.get(enumField.getXsdEnumValue() );
					if( seqNo == null ){
						enumFieldsDontHaveSeqNo.add( enumField.getXsdEnumValue() );
					}else{
						enumField.setSequenceNumber( seqNo );
					}
				}
				if( enumFieldsDontHaveSeqNo.size() > 0 ){
					fieldsDoesNotHaveSeqNo.put(typeName, enumFieldsDontHaveSeqNo );
				}
				
			}else{
				//if no fields, then continue
				if(message.getFields().size() == 0 ){
					continue;
				}
				
				Map<String, ProtobufField> fieldsMap = getFieldsMap( message.getFields() );
				
				for( Map.Entry<String, Integer> entry : tagsForType.entrySet() ){
					String fieldname = entry.getKey();
					if( fieldname.startsWith("@") ){
						fieldname = fieldname.replace("@", typeName.getLocalPart()+"_" );
					}
					if(fieldsMap.get(fieldname) == null ){
						if( strictUpdate ){
							throw new CodeGenFailedException("The field name '"+ fieldname+"' does not exists in proto message " + typeName);
						}
						continue;
					}
					fieldsMap.get(fieldname).setSequenceTagNumber( entry.getValue() );
					fieldsMap.remove(fieldname);
				}

				if( fieldsMap.size() > 0 ){
					fieldsDoesNotHaveSeqNo.put(typeName, fieldsMap.keySet() );
				}
			}
			
		}
		
		
		if( fieldsDoesNotHaveSeqNo.size() > 0 ){
			
			if( strictUpdate ){
				throw new CodeGenFailedException("Sequence no generation is not successful for following fields. " + fieldsDoesNotHaveSeqNo.toString() );
			}
		}
	}

	
	private Map<String, ProtobufField> getFieldsMap( List<ProtobufField> fields){
		Map<String, ProtobufField> fieldsMap = new HashMap<String, ProtobufField>();
		
		for(ProtobufField field : fields ){
			fieldsMap.put(field.getFieldName(), field );
		}
		
		return fieldsMap;
	}
	/**
	 * Creates the formatter and writes the file.
	 * 
	 * @param writer
	 * @param schema
	 * @throws Exception
	 */
	private void build( FileContentWriter writer,  ProtobufSchema schema  ) throws Exception{
		DotProtoFormatter formatter = createSourceFileWriter( writer, schema );
		
		formatter.write( new ProtobufSchemaWriter( schema ) );
		formatter.close();
	}

	/**
	 * Creates the formatter which takes care of indentation, formatting comments.
	 * 
	 * @param writer
	 * @param schema
	 * @return
	 * @throws Exception
	 */
	private DotProtoFormatter createSourceFileWriter( FileContentWriter writer,  ProtobufSchema schema  ) throws Exception{
		Writer bw = new BufferedWriter( writer.openSource( schema.getDotprotoFileName() ) );
        return new DotProtoFormatter(new PrintWriter(bw));
	}
}
