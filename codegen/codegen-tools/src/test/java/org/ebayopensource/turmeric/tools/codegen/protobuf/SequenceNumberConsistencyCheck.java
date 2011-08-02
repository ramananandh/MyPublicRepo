package org.ebayopensource.turmeric.tools.codegen.protobuf;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
import org.ebayopensource.turmeric.tools.codegen.ServiceGenerator;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.junit.Assert;
import org.junit.Test;

public class SequenceNumberConsistencyCheck extends AbstractServiceGeneratorTestCase {
	
	
  
    public static String getPackageFromNamespace(String namespace) {
    	
    	//Using the method used by JAXB directly to avoid potential conflicts with JAXB generated code
    	//Therefore commenting out the old code which is based on JAXB 2.0 spec
    	return com.sun.tools.xjc.api.XJC.getDefaultPackageName(namespace);
    }
	@Test
	public void testSequenceNumberConsistency() throws Exception{
		File destDir = testingdir.getDir();
		File bin = new File(destDir.getAbsolutePath() + "/bin/");
		
		System.out.println(bin.getAbsoluteFile());
		File gensrc = new File(destDir,"gen-src");
		
		
		URL [] urls = {new URL("file:/"+ destDir.getAbsolutePath()+"/bin/"),destDir.toURI().toURL(),gensrc.toURI().toURL()};
		URLClassLoader loader = new URLClassLoader(urls,Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(loader);

		File wsdlpath = getProtobufRelatedInput("TestWsdlXsdTypes.wsdl");



		File file = new File(destDir.getAbsolutePath() +"/gen-meta-src/META-INF/soa/services/proto/CalculatorService/CalculatorService.proto");
		
		CodeGenUtil.deleteContentsOfDir(new File(file.getParent()));
		generateJaxbClasses(wsdlpath.getAbsolutePath(), destDir.getAbsolutePath(),bin);

		ProtoFileParser parser = new ProtoFileParser(file);
		List<Message> msg = parser.parse();
	
	
		
		
		

		List<String> listofMessageName = new ArrayList<String>();
		Map<String,List<String>> msgMap2 = null;
		for(int i=0; i < 5;i++) { 
		
		
						 parser = new ProtoFileParser(file);
						 msg = parser.parse();
					
					
					
					Map<String,List<String>> msgMap1 = getMessageInfo(msg);
					CodeGenUtil.deleteContentsOfDir(new File(file.getParent()));
					generateJaxbClasses(wsdlpath.getAbsolutePath(), destDir.getAbsolutePath(),bin);
					
					
					ProtoFileParser parser1 = new ProtoFileParser(file);
					List<Message> msg2 = parser1.parse();
					
					msgMap2 = getMessageInfo(msg);
					
					
					for(Message m1 :msg2) {
						listofMessageName.add(m1.getMessageName());
					List<String> fieldsInfo1 = msgMap1.get(m1.getMessageName());
					List<String> fieldsInfo2 = msgMap2.get(m1.getMessageName());
					
					Assert.assertTrue(fieldsInfo1.containsAll(fieldsInfo2));
					}
					//check pmd info is consistent:
					List<String> list1 = FileUtil.readFileAsLines(file);
					List<String> onlyPMD = new ArrayList<String>();
					for(String s : list1){
						if(s.trim().startsWith("//PMD")){
							onlyPMD.add(s);
						}
					}
					List<PMDInfo> list = parser.parsePMDData(onlyPMD);

					Map<String,List<String>> msgMap3 = new HashMap<String, List<String>>();
					for(String msgName:listofMessageName){
						List<String> pmd = new ArrayList<String>();
						for(PMDInfo info : list ){
							if(info.getMessageName().trim().equals(msgName.trim())){
								
								pmd.add(info.getFieldName()+info.getSequenceNumber());
							}
							
						}
						msgMap3.put(msgName,pmd);
					}
					
					for(String msgName : listofMessageName) {
					List<String> fieldsInfo2 = msgMap2.get(msgName);
					List<String> fieldsInfo3 = msgMap3.get(msgName);
					Assert.assertTrue(fieldsInfo2.containsAll(fieldsInfo3));
					}
		}
		
			
		 
	}
	
	public Map<String,List<String>> getMessageInfo(List<Message> msg){
		Map<String,List<String>> msgFields = new HashMap<String,List<String>>();
		
		
		for(Message m :msg){
			String msName = m.getMessageName();
			List<String> list = new ArrayList<String>();
			List<Field> fields = m.getFields();
			for(Field f :fields){
				String fsn = f.getFieldName().trim()+f.getSequenceNumber().trim();
				list.add(fsn);
			}
			msgFields.put(msName,list);
		}
		return msgFields;
		
	}
	

	
	
	
	
	
	
	
	
	
	
	public void generateJaxbClasses(String path,String destDir,File binDir) throws Exception{
		String [] testArgs = {"-serviceName","CalculatorService",
				  "-genType","ServiceFromWSDLIntf",	
				  "-wsdl",path,
				  "-gip","com.ebay.test.soaframework.tools.codegen",
				  "-dest",destDir,
				  "-src",destDir,
				  "-bin",binDir.getAbsolutePath(),
				  "-slayer","INTERMEDIATE",
				  "-nonXSDFormats","protobuf",
				  "-enabledNamespaceFolding",
				  "-scv","1.0.0",
				  "-pr",destDir};
		
		ServiceGenerator sgen = new ServiceGenerator();
		sgen.startCodeGen(testArgs);
		
	}
	
	public List<String> getTypeInformation(File fieldInfo){
		List<String> info = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			FileReader in = new FileReader(fieldInfo);
			reader = new BufferedReader(in);
			String line = reader.readLine();
			while(line != null ){
				info.add(line);
				line = reader.readLine();
			}
			
		} catch (FileNotFoundException e) {
		
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}finally{
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return info;
		
	}
	
	

}
