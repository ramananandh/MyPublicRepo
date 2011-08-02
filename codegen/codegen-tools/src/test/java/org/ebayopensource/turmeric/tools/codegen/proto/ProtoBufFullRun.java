package org.ebayopensource.turmeric.tools.codegen.proto;

import java.io.File;

import org.ebayopensource.turmeric.tools.TestResourceUtil;
import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.junit.Test;

public class ProtoBufFullRun extends AbstractServiceGeneratorTestCase {

	public  File getProtobufInputFile(String name) {
		return TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/proto/"
				+ name);
	}
	
	



	
	@Test
	
	public void testComplexTypeWsdlFullFlow() throws Exception {
		
		File destDir = testingdir.getDir();
		File binDir  = new File(destDir, "bin");
		File wsdl = getProtobufInputFile("TestFullRunComplexTypeWsdl.wsdl");
		
		String testArgs[] = new String[] {
				"-servicename",
				"CalculatorService1",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "ClientNoConfig", 
				"-envmapper", "org.ebayopensource.turmeric.tools.codegen.EnvironmentMapperImpl",
				"-src", destDir.getAbsolutePath(),
				"-dest", destDir.getAbsolutePath(), 
				"-mdest", destDir.getAbsolutePath() + "/meta-src/",
				"-scv", "1.0.0", 
				"-bin",binDir.getAbsolutePath(),
				 "-enabledNamespaceFolding",
				"-nonXSDFormats", "protobuf" };
		
		CodeGenUtil.deleteContentsOfDir(new File(destDir + "/gen-meta-src"));
		
		String protoLocation = CodeGenUtil.genDestFolderPath(destDir.getAbsolutePath(), "/meta-src/META-INF/soa/services/proto/CalculatorService1");
		File deleteProtoFileLocation = new File(protoLocation);
		if(deleteProtoFileLocation.exists())
			CodeGenUtil.deleteContentsOfDir(deleteProtoFileLocation);
		
		try {
			performDirectCodeGen(testArgs);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	

		CodeGenUtil.deleteContentsOfDir(new File(destDir.getAbsolutePath() + "/gen-src"));
	}
}
