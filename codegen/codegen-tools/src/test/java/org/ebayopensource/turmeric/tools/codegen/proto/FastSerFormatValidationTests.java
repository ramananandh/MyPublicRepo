/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.proto;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatValidationError;
import org.ebayopensource.turmeric.runtime.codegen.common.ValidationRule;
import org.ebayopensource.turmeric.tools.TestResourceUtil;
import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.WSDLParserConstants;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.FastSerFormatCodegenBuilder;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator.FastSerFormatNotSupportedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator.FastSerFormatValidationHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author rkulandaivel
 *
 */
public class FastSerFormatValidationTests extends AbstractServiceGeneratorTestCase {
	
	
	@Override
	public File getProtobufRelatedInput(String name) {
		return TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/proto/"
				+ name);
	}

	public static String[] getTestInvalidArgs(File wsdl,File destDir) {
		
		File binDir = new File(destDir,"bin");
		String testArgs[] = new String[] {
				"-servicename",
				"CalcService",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "All", "-src", destDir.getAbsolutePath(),
				"-dest",destDir.getAbsolutePath(), "-scv", "1.0.0", "-bin",binDir.getAbsolutePath(),
				//"-enabledNamespaceFolding",
				"-nonXSDFormats", "protobuf" };
		return testArgs;
	}

	public static String[] getTestValidArgs(File wsdl, File destDir) {
		File binDir = new File(destDir,"bin");
		String testArgs[] = new String[] {
				"-servicename",
				"CalcService",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "All", "-src", destDir.getAbsolutePath(),
				"-dest",destDir.getAbsolutePath(), "-scv", "1.0.0", "-bin",binDir.getAbsolutePath(),
				"-enabledNamespaceFolding",
				"-nonXSDFormats", "protobuf" };
		return testArgs;
	}
	
	@Test
	public void validateService() throws Exception {
		
		File wsdl1  = getProtobufRelatedInput("CalcServiceProtobufInvalid.wsdl");
		File wsdl2 = getProtobufRelatedInput("CalcServiceProtobufInvalid.wsdl");
		File destDir = testingdir.getDir();
		CodeGenContext context = ProtobufSchemaMapperTestUtils.getCodeGenContext( getTestInvalidArgs(wsdl1,destDir) );
		
		//commented the test case bacause the check for enable namespace folding is removed.
//		try {
//			FastSerFormatCodegenBuilder.getInstance().validateServiceIfApplicable(context);
//		} catch (CodeGenFailedException e) {
//			if( !e.getMessage().contains( FastSerFormatCodegenValidator.NAMESPACE_FOLDING_MESSAGE )){
//				Assert.fail("The Test failed. Expected to fail for namespace folding false");
//			}
//		}
		
		context.getInputOptions().setInputFile(null);
		context.getInputOptions().setEnabledNamespaceFolding(true);
		try {
			FastSerFormatCodegenBuilder.getInstance().validateServiceIfApplicable(context);
		} catch (CodeGenFailedException e) {
			if( !e.getMessage().contains( FastSerFormatValidationHandler.EMPTY_WSDL_PATH )){
				e.printStackTrace();
				Assert.fail("The Test failed. Expected to fail for empty wsdl path");
			}
		}

		context.getInputOptions().setInputFile(wsdl2.getAbsolutePath());
		try {
			FastSerFormatCodegenBuilder.getInstance().validateServiceIfApplicable(context);
		} catch (CodeGenFailedException e) {
			if( !e.getMessage().contains( WSDLParserConstants.NS_URI_1999_SCHEMA_XSD )){
				e.printStackTrace();
				Assert.fail("The Test failed. Expected to fail for invalid xsd namespace");
			}
		}

		context = ProtobufSchemaMapperTestUtils.getCodeGenContext( getTestValidArgs(wsdl2,destDir) );
		try {
			FastSerFormatCodegenBuilder.getInstance().validateServiceIfApplicable(context);
		} catch (CodeGenFailedException e) {
			e.printStackTrace();
			if( !( e instanceof FastSerFormatNotSupportedException ) ){
				Assert.fail("The Test failed. Expected exception FastSerFormatNotSupportedException");
			}

			FastSerFormatNotSupportedException excep = (FastSerFormatNotSupportedException)e;
			if( excep.getErrors().size() == 0 ){
				Assert.fail("The test failed. Expected errors around 10.");
			}

			Set<ValidationRule> rules = new HashSet<ValidationRule>();
			
			for( FastSerFormatValidationError error : excep.getErrors() ){
				if(error.getDescription().contains("{0}") ||  error.getDescription().contains("{1}") || error.getDescription().contains("{2}") ){
					Assert.fail("The test failed. Message formation failed");
				}
				rules.add(error.getError());
			}
			
			for( ValidationRule rule : ValidationRule.values() ){
				if( rule == ValidationRule.REDEFINE_NOT_SUPPORTED 
						|| rule == ValidationRule.OLD_SCHEMAS_NOT_SUPPORTED 
						){
					continue;
				}
				if( !rules.contains(rule) ){
					Assert.fail("The rule "+rule.value() +" is not detected");
				}
			}
			Assert.assertTrue(excep.getMessage(), true);
			
			String message = excep.getMessage();
			if(message.endsWith(";")){
				Assert.fail("Message not formatted properly. It ends with semi colan");
			}
			return ;
			
		}
		Assert.fail("The validation succeede so the test case failed.");
		
	}

	public static String[] getTestArgsForXsdValidation(File wsdl, File destDir) {
		String testArgs[] = new String[] {
				"-servicename",
				"CalcService",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "ValidateXSDsForNonXSDFormats", 
				"-enabledNamespaceFolding",
				"-nonXSDFormats", "protobuf",
				"-xsdPathsForValidation", "types/InValidTypeLibrary/ComplexTypeA.xsd,types/InValidTypeLibrary/ComplexTypeB.xsd"};
		return testArgs;
	}
	@Test
	public void testParsingXSds() {

		File destDir = testingdir.getDir();
		File wsdl1  = getProtobufRelatedInput("CalcServiceProtobufInvalid.wsdl");
		
		Set<ValidationRule> expectedRules = new HashSet<ValidationRule>();
		expectedRules.add(ValidationRule.ANONYMOUS_TYPE_NOT_SUPPORTED );
		expectedRules.add(ValidationRule.POLYMORPHISM_NOT_SUPPORTED );
		try {
			performDirectCodeGen(getTestArgsForXsdValidation(wsdl1,destDir));
		} catch( Exception e ){
			String message = e.getMessage();
			for( ValidationRule rule : expectedRules ){
				if( message.indexOf(rule.value() ) < 0 ){
					Assert.fail("The xsd is supposed to fail for rule '"+rule+"'. But id did not. ");
				}
			}
		}

	}
}
