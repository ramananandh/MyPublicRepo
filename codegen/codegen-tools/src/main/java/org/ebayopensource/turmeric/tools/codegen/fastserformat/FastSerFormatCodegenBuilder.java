/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatType;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.InputOptions;
import org.ebayopensource.turmeric.tools.codegen.SourceGenerator;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.exception.PreProcessFailedException;
import org.ebayopensource.turmeric.tools.codegen.external.WSDLUtil;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.WSDLParserException;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator.FastSerFormatValidationHandler;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
/**
 * @author rkulandaivel
 *
 */
public class FastSerFormatCodegenBuilder implements SourceGenerator{
	private static Logger s_logger = LogManager
	.getInstance(FastSerFormatCodegenBuilder.class);

	private Logger getLogger() {
		return s_logger;
	}

	private static final FastSerFormatCodegenBuilder s_instance = new FastSerFormatCodegenBuilder();

	private FastSerFormatCodegenBuilder(){
		
	}

	public static FastSerFormatCodegenBuilder getInstance(){
		return s_instance;
	}

	public List<SchemaType> generateSchema(CodeGenContext codeGenContext) throws WSDLParserException, WSDLException, PreProcessFailedException{

		Definition wsdlDefinition = codeGenContext.getWsdlDefinition();
		if (wsdlDefinition == null) {
			String wsdlFileLoc = codeGenContext.getInputOptions().getInputFile();
			wsdlDefinition = WSDLUtil.getWSDLDefinition(wsdlFileLoc);
			codeGenContext.setWsdlDefinition(wsdlDefinition);
		}

		List<SchemaType> listOfSchemaTypes = new ArrayList<SchemaType>();
		try {
			WSDLUtil.persistAndPopulateAllSchemaTypes(codeGenContext, listOfSchemaTypes, null);
		} catch (WSDLParserException e) {
			getLogger().log(Level.WARNING,
							"WSDL definition parsing failed in the method generateSchema in SchemaGenerator. \n"
									+ "Exception is :" + e.getMessage(), e);
			throw e;
		}

		return listOfSchemaTypes;
	}

	/**
	 * Builds the fast ser format artifacts for the service project.
	 * It First validates whether the service is enabled for fast ser format.
	 * If enabled, it checks whether the dependent TLs are also enabled for fast ser formats.
	 * If all dependent TLs supports the format, then each service is validated and then artifacts are generated. 
	 * @param codeGenContext
	 * @throws CodeGenFailedException
	 */
	public void buildFastSerFormatArtifacts(CodeGenContext codeGenContext) throws CodeGenFailedException{
		if( !isServiceEnabled( codeGenContext ) ){
			return ;
		}

		String fastSerFormatStr = codeGenContext.getInputOptions().getSupportedFastSerFormats();
		List<FastSerFormatType> supportedTypes = getSupportedTypes( fastSerFormatStr );
		
		List<IFastSerFormatArtifactsGenerator> listOfGenerators = FastSerFormatCodegenFactory.getGeneratorsForSupportedFormats(supportedTypes);

		//Parse schema
		List<SchemaType> listOfSchemaTypes;
		try {
			listOfSchemaTypes = generateSchema( codeGenContext );
		} catch (WSDLParserException e) {
			throw new CodeGenFailedException( "Generate Schema Failed.", e );
		} catch (WSDLException e) {
			throw new CodeGenFailedException( "Generate Schema Failed. Unable to created wsdl definition.", e );
		} catch (PreProcessFailedException e) {
			throw new CodeGenFailedException( "Generate Schema Failed. Unable to created wsdl definition.", e );
		} 
		
		//Generate artifacts
		for( IFastSerFormatArtifactsGenerator generator : listOfGenerators){
			generator.generateArtifacts(listOfSchemaTypes, codeGenContext);
		}
	}
	
	public boolean isServiceEnabled(CodeGenContext codeGenContext){
		String fastSerFormatStr = codeGenContext.getInputOptions().getSupportedFastSerFormats();
		//Is service enabled for fast ser format
		if( null == fastSerFormatStr || "".equals(fastSerFormatStr) ){
			getLogger().info("The Service does not support fast serialization");
			//fast ser format not enabled. Hence return.
			return false;
		}
		
		
		List<FastSerFormatType> supportedTypes = getSupportedTypes( fastSerFormatStr );
		
		if( supportedTypes.size() == 0){
			getLogger().info("The Service does not support fast serialization");
			return false;
		}
		
		return true;
	}
	public void validateServiceIfApplicable(CodeGenContext codeGenContext) throws CodeGenFailedException{
		if( !isServiceEnabled( codeGenContext ) ){
			return ;
		}

		String fastSerFormatStr = codeGenContext.getInputOptions().getSupportedFastSerFormats();
		List<FastSerFormatType> supportedTypes = getSupportedTypes( fastSerFormatStr );

		//Validate the service
		FastSerFormatCodegenValidator.getInstance().validateService(codeGenContext, supportedTypes);
	}



	/**
	 * Splits the given comma separated string and returns list of supported formats.
	 * @param supportedFormatStr
	 * @return
	 */
	private List<FastSerFormatType> getSupportedTypes(String supportedFormatStr){
		List<FastSerFormatType> supportedTypes = new ArrayList<FastSerFormatType>();

		String[] supportedFormatArray = supportedFormatStr.split(",");
		for(String supportedFormat : supportedFormatArray){
			supportedTypes.add( FastSerFormatType.fromValue(supportedFormat) );
		}

		return supportedTypes;
	}

	


	@Override
	public boolean continueOnError() {
		return false;
	}

	public void validateXSDs(CodeGenContext codeGenContext) throws CodeGenFailedException{
		if( !isServiceEnabled( codeGenContext ) ){
			return ;
		}
		
		String fastSerFormatStr = codeGenContext.getInputOptions().getSupportedFastSerFormats();
		List<FastSerFormatType> supportedTypes = getSupportedTypes( fastSerFormatStr );
		
		String filePathStr = codeGenContext.getInputOptions().getXsdPathsForNonXSDFormatsValidation();
		if( CodeGenUtil.isEmptyString(filePathStr) ){
			throw new CodeGenFailedException("Could not validate xsd files because the file path passed are empty.");
		}
		String[] xsdFileNames =  filePathStr.split(",");
		List<String> trimmedXsdFileNames = new ArrayList<String>();
		for(String fileName : xsdFileNames ){
			String name = fileName.trim();
			if( !CodeGenUtil.isEmptyString(name) ){
				trimmedXsdFileNames.add(name);
			}
		}
		FastSerFormatValidationHandler.validateXsds(trimmedXsdFileNames
				.toArray(new String[trimmedXsdFileNames.size()]), supportedTypes);
	}
	/**
	 * This method generates the artifacts required for all the Fast Ser Formats.
	 */
	@Override
	public void generate(CodeGenContext codeGenCtx)
			throws CodeGenFailedException, WSDLException {
		if( codeGenCtx.getInputOptions().getCodeGenType() == InputOptions.CodeGenType.ValidateXSDsForNonXSDFormats ){
			validateXSDs( codeGenCtx );
		}else{
			buildFastSerFormatArtifacts( codeGenCtx );
		}
		
	}

	@Override
	public String getFilePath(String serviceAdminName, String interfaceName) {
		// TODO Auto-generated method stub
		return null;
	}
}
