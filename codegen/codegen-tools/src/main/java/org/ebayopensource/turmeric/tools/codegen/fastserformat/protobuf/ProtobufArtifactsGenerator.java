/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.IFastSerFormatArtifactsGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.dotproto.DotProtoGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.eproto.EProtoGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.exception.ProtobufModelGenerationFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufSchema;

/**
 * @author rkulandaivel
 *
 */
public class ProtobufArtifactsGenerator implements
		IFastSerFormatArtifactsGenerator {

	private static Logger s_logger = LogManager
	.getInstance(ProtobufArtifactsGenerator.class);

	private Logger getLogger() {
		return s_logger;
	}

	public ProtobufArtifactsGenerator() {

	}


	/**
	 * This method generates all the artifacts required to support protobuf.
	 * First it creates protobuf schema from the schema types passed.
	 * From the protobuf schema, the following artifacts are generated.
	 * 1. dot proto file, 2. jproto file and 3. extended proto file.
	 * @throws CodeGenFailedException 
	 *  
	 */
	@Override
	public void generateArtifacts(List<SchemaType> schemaTypes,
			CodeGenContext codeGenContext) throws CodeGenFailedException {
		
		//step 1 create protobuf schema
		long startTime = System.currentTimeMillis();
		ProtobufSchema schema;
		try {
			schema = ProtobufSchemaMapper.getInstance().createProtobufSchema(schemaTypes, codeGenContext);
		} catch (ProtobufModelGenerationFailedException e) {
			throw new CodeGenFailedException("Proto buf model generation failed.", e);
		}
		
		long endTime = System.currentTimeMillis();
		getLogger().log(Level.INFO, "The time take to createProtobufSchema ="+ (endTime-startTime) +" secs");

		//step 2 create dot proto
		startTime = System.currentTimeMillis();
		try {
			DotProtoGenerator.getInstance().generate(schema, codeGenContext);
		} catch (Exception e1) {
			throw new CodeGenFailedException("Dot Proto generation failed.", e1);
		}
		endTime = System.currentTimeMillis();
		getLogger().log(Level.INFO, "The time take to DotProtoGenerator ="+ (endTime-startTime) +" secs");

		
		
		
		//step 3 compile dot proto
		startTime = System.currentTimeMillis();
		try {
			ProtoBufCompiler.getInstance().compileProtoFile(schema, codeGenContext);
		} catch (CodeGenFailedException codeGenFailedException) {
			throw codeGenFailedException;
		} catch (Exception exception) {
			throw new CodeGenFailedException(exception.getMessage(), exception);
		}
		endTime = System.currentTimeMillis();
		getLogger().log(Level.INFO, "The time take to DotProtoGenerator ="+ (endTime-startTime) +" secs");

		
		
		//step 4 generated eproto
		startTime = System.currentTimeMillis();
		try {
			EProtoGenerator.getInstance().generate(schema, codeGenContext);
		} catch (CodeGenFailedException codeGenFailedException) {
			throw codeGenFailedException;
		} catch (Exception exception) {
			throw new CodeGenFailedException(exception.getMessage(), exception);
		}

		endTime = System.currentTimeMillis();
		getLogger().log(Level.INFO, "The time take to EProtoGenerator ="+ (endTime-startTime) +" secs");

		
		
		getLogger().info( schema.toString() );
	}

}
