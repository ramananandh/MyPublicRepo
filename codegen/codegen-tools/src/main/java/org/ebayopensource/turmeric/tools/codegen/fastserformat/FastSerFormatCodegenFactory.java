/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.ProtobufArtifactsGenerator;

/**
 * @author rkulandaivel
 *
 */
public class FastSerFormatCodegenFactory {

	public static List<IFastSerFormatArtifactsGenerator> getGeneratorsForSupportedFormats(List<FastSerFormatType> supportedTypes){
	
		List<IFastSerFormatArtifactsGenerator> listOfGenerators = new ArrayList<IFastSerFormatArtifactsGenerator>();
		
		// TODO ;
		listOfGenerators.add( new ProtobufArtifactsGenerator() );
		
		return listOfGenerators;
	}
}
