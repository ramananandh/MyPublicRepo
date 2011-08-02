package org.ebayopensource.turmeric.tools.codegen;

import java.io.File;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;


public class RestCallCodegenChange extends AbstractServiceGeneratorTestCase{

@Rule public TestName name = new TestName();
	
	File destDir = null;
	File binDir = null;
	final String INTF_PROPERTIES = "service_intf_project.properties";
	final String IMPL_PROPERTIES = "service_impl_project.properties";
	ServiceGenerator gen = null;
	
	Properties implProps = new Properties();
	File intfProperty = null;
	File implProperty = null;
	
	@Before
	
	public void init() throws Exception{
		
		testingdir.ensureEmpty();
		destDir = testingdir.getDir();
		binDir = testingdir.getFile("bin");
			
	}	
	
	
	@Test
	public void testRestCallCodegenChange() throws Exception{
		

		boolean hasOpsName = false;
		boolean hasResponseData = false;
		
		
		String [] testArgs1 = {"-serviceName","AccountService",
				  "-genType","ServerConfig",	
				  "-interface","com.ebayopensource.test.soaframework.tools.codegen.AccountService",
				  "-sicn","com.ebayopensource.test.soaframework.tools.codegen.impl.AccountService",
				  "-gip","com.ebayopensource.test.soaframework.tools.codegen",
				  "-dest",destDir.getAbsolutePath(),
				  "-bin",binDir.getAbsolutePath(),
				  "-src",destDir.getAbsolutePath(),
				  "-slayer","INTERMEDIATE",
				  "-scv","1.0.0",
				  "-pr",destDir.getAbsolutePath()};

	
		performDirectCodeGen(testArgs1);
		
		
		String serviceConfig = getServiceConfigFile(destDir.getAbsolutePath(),"AccountService");
		
        
		
		 String xml = readFileAsString(serviceConfig);
		 Assert.assertTrue(xml.contains("<option name=\"X-TURMERIC-OPERATION-NAME\">path[+1]</option>"));
		 Assert.assertTrue(xml.contains("<option name=\"X-TURMERIC-RESPONSE-DATA-FORMAT\">query[format]</option>"));
	}
	
	 public String getServiceConfigFile(String destDir,String serviceName){
		 
		 return destDir + File.separator +"gen-meta-src/META-INF/soa/services/config/"+serviceName+"/ServiceConfig.xml";
		 
	 }
	
	
}
