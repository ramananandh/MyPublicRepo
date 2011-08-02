package org.ebayopensource.turmeric.tools.codegen.protobuf;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.ebayopensource.turmeric.tools.TestResourceUtil;
import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator.FastSerFormatNotSupportedException;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.junit.Before;
import org.junit.Test;

public class TestTypeLibraryProtobuf extends AbstractServiceGeneratorTestCase {
	

String destDir = null;
Properties libProps = new Properties();
final String LIB_PROPERTIES = "service_intf_project.properties";
File libProperty = null;
@Before
public void init() throws Exception{

		
		destDir = testingdir.getDir().getAbsolutePath();
		mavenTestingRules.setFailOnViolation(false);
		
		
		CodeGenUtil.deleteContentsOfDir(new File(destDir));
		try {
			libProperty =	createPropertyFile(destDir,LIB_PROPERTIES);
			
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		libProps.put("nonXSDFormats","protobuf");
		
	}

	private File getPathOfFile(String name){
	return TestResourceUtil.getResource("xsd/"
			+ name);
	
}

	@Test(expected=FastSerFormatNotSupportedException.class)
	public void testCase1() throws Exception{
		
		File xsd1 = getPathOfFile("TestXsd.xsd");
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation","xsd/TestXsd.xsd",
				  "-nonXSDFormats","protobuf",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs);
	}
	
	@Test(expected=FastSerFormatNotSupportedException.class)
	public void testCase2() throws Exception{
		
		fillProperties(libProps, libProperty);
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation","xsd/TestXsd.xsd",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs);
		
		
	}	
	
	
	@Test(expected=FastSerFormatNotSupportedException.class)
	public void testCase3() throws Exception{
		
		
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation","xsd/TestXsd.xsd,xsd/TestXsd1.xsd",
				  "-nonXSDFormats","protobuf",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs); 
	
		
	}
	
	
	@Test(expected=FastSerFormatNotSupportedException.class)
	public void testCase4() throws Exception{
		
		
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation","xsd/TestXsd2.xsd",
				  "-nonXSDFormats","protobuf",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs);
		
		
		
	}
	
	@Test(expected=FastSerFormatNotSupportedException.class)
	public void testCase5() throws Exception{
		
		File xsd1 = getPathOfFile("TestXsd.xsd");
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation","xsd/TestXsd3.xsd",
				  "-nonXSDFormats","protobuf",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs);
		
		
	}
	
	@Test
	public void testCase6() throws Exception{
		
		File xsd1 = new File("wsdlorxsd/TestXsd.xsd");
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation","xsd/SupportedXsd.xsd",
				  "-nonXSDFormats","protobuf",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs);
		
	}
	
	@Test
	public void testCase7() throws Exception{
		
		File xsd1 = getPathOfFile("SupportedXsd.xsd");
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation",xsd1.getAbsolutePath(),
				  "-nonXSDFormats","protobuf",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs);
		
	}
	
	
	@Test(expected=FastSerFormatNotSupportedException.class)
	public void testCase8() throws Exception{
		
		//SOATestUserTypeLibrary.jar
		File xsd1 = new File("src/test/resources/xsd");
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation","jar:file:\\"+ xsd1.getAbsolutePath() +"/SOATestUserTypeLibrary.jar!/types/RegistrationInfoType.xsd,jar:file:\\"+ xsd1.getAbsolutePath() +"/SOATestUserTypeLibrary.jar!/types/UserInfoType.xsd",
				  "-nonXSDFormats","protobuf",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs);
		
	}
	
	
	@Test
	public void testCase9() throws Exception{
		
	
		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
		

		
		String [] testArgs = {
				  "-servicename","ShippingService",
				  "-wsdl",wsdlFile.getAbsolutePath(),
				  "-genType","ValidateXSDsForNonXSDFormats",
				  "-xsdPathsForValidation","types/RegistrationInfoType.xsd,types/UserInfoType.xsd",
				  "-nonXSDFormats","protobuf",
				  "-pr",destDir,
				  "-dest",destDir
				  
				 };
		
		performDirectCodeGen(testArgs);
		
	}
	
	// URL url = new URL("jar:file://+xsd1.getAbsolutePath() +"/SOATestUserTypeLibrary.jar!/UserInfoType.xsd);
	//RegistrationInfoType
	//jar:file://+xsd1.getAbsolutePath() +"/SOATestUserTypeLibrary.jar!/RegistrationInfoType.xsd
	
}
