/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatType;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator.FastSerFormatValidationHandler;

/**
 * This class is responsible for all the validations to enable fast ser format on a service or the type library.
 * @author rkulandaivel
 *
 */
public class FastSerFormatCodegenValidator {
	public static final String NAMESPACE_FOLDING_MESSAGE = "The service which is enable namespace folding false cannot support Fast Serialization like ";

	private static Logger s_logger = LogManager
	.getInstance(FastSerFormatCodegenValidator.class);
	private static FastSerFormatCodegenValidator s_instance = new FastSerFormatCodegenValidator();

	private static Logger getLogger() {
		return s_logger;
	}

	private FastSerFormatCodegenValidator(){
		
	}

	public static FastSerFormatCodegenValidator getInstance(){
		return s_instance;
	}

	/**
	 * This method does validations on the service to check whether the service can enable fast ser format.
	 * Following validations are performed in steps
	 * 
	 * 1. The service should be enable namespace folding true
	 * 2. The wsdl is validated against unsupported constructs.
	 * 
	 * 
	 * @param codeGenContext
	 * @param supportedTypes
	 * @throws CodeGenFailedException
	 */
	public void validateService(CodeGenContext codeGenContext, List<FastSerFormatType> supportedTypes) throws CodeGenFailedException{
		//step 1, Enable NamespaceFolding Set
		if( !codeGenContext.getInputOptions().isEnabledNamespaceFoldingSet() ){
			//commented the check. Instead of checking for flag check for multiple namespaces in wsdl
			//throw new CodeGenFailedException( NAMESPACE_FOLDING_MESSAGE + supportedTypes);
		}

		getLogger().log(Level.INFO, "Enable Namespace folding validation succeeded");

		//step 2, validate wsdl
		FastSerFormatValidationHandler.validateServiceForFastSerFormatSupport(codeGenContext, supportedTypes);
	}


}
