package org.ebayopensource.turmeric.tools.codegen.fastserformat;

import java.util.List;

import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;

/**
 * @author rkulandaivel
 *
 * This is the interface for all fast ser format artifacts generation.
 * Currently only protobuf uses this interface.
 * If new formats like avro needs to be introduced, one separate generator needs
 * to be introduced implementing this interface.  
 */
public interface IFastSerFormatArtifactsGenerator {
	void generateArtifacts(List<SchemaType> schemas, CodeGenContext codeGenContext) throws CodeGenFailedException;
}
