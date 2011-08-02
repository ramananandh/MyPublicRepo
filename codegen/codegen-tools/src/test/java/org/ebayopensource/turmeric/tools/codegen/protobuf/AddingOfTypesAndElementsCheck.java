package org.ebayopensource.turmeric.tools.codegen.protobuf;


import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.junit.utils.MavenTestingUtils;
import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
import org.ebayopensource.turmeric.tools.codegen.ServiceGenerator;
import org.junit.Assert;
import org.junit.Test;

public class AddingOfTypesAndElementsCheck extends AbstractServiceGeneratorTestCase {
	
	
  
    public static String getPackageFromNamespace(String namespace) {
    	
    	//Using the method used by JAXB directly to avoid potential conflicts with JAXB generated code
    	//Therefore commenting out the old code which is based on JAXB 2.0 spec
    	return com.sun.tools.xjc.api.XJC.getDefaultPackageName(namespace);
    }
	@Test
	public void testForAddedAndRemovedTypesOrElements() throws Exception{
	
		File destDir = testingdir.getDir();
		File bin = new File(destDir.getAbsolutePath() + "/bin/");
		
		System.out.println(bin.getAbsoluteFile());
		File gensrc = new File(destDir,"gen-src");
		
		
		URL [] urls = {new URL("file:/"+ destDir.getAbsolutePath()+"/bin/"),destDir.toURI().toURL(),gensrc.toURI().toURL()};
		URLClassLoader loader = new URLClassLoader(urls,Thread.currentThread().getContextClassLoader());
	
		Thread.currentThread().setContextClassLoader(loader);
		
		File wsdlpath = getProtobufRelatedInput("TestWsdlComplexType.wsdl");
	
		String wsdlNSToPkg = getPackageFromNamespace("http://codegen.tools.soaframework.test.ebay.com");


		File file = new File(destDir.getAbsolutePath()+"/meta-src/META-INF/soa/services/proto/CalculatorService/CalculatorService.proto");
		
		MavenTestingUtils.ensureEmpty(file.getParentFile());
		generateJaxbClasses(wsdlpath.getAbsolutePath(), destDir.getAbsolutePath());

		ProtoFileParser parser = new ProtoFileParser(file);
		List<Message> msg = parser.parse();
		
		List<String> listofMessageName = getMessageNameList(msg);
		
		assertTagAssignment(msg);
		
		Map<String,List<String>> msgMap1  = pmdInfoForAllMessages(file, listofMessageName, parser);
		
		File genmetasrc = new File(destDir,"gen-meta-src");
		
		MavenTestingUtils.ensureEmpty(genmetasrc);
		
		wsdlpath = getProtobufRelatedInput("ModifiedTestWsdlComplexType.wsdl");
		generateJaxbClasses(wsdlpath.getAbsolutePath(), destDir.getAbsolutePath());
		
		
		msg = parser.parse();
		assertTagAssignment(msg);
		
		listofMessageName = getMessageNameList(msg);
		//adding the removed type to get pmd data for it.PMD data for removed type should not be removed.
		listofMessageName.add("ExtendMyComplexType");
		
		Map<String,List<String>> msgMap2 = pmdInfoForAllMessages(file, listofMessageName, parser);
		//assert type added is present and contains the pmd data.
		
		 Assert.assertTrue("PMD data is removed after the type is removed" ,msgMap2.get("ExtendMyComplexType").containsAll(msgMap1.get("ExtendMyComplexType")));
		//assert the new element is added in the SampleComplexType  "value11"
		boolean containsValue = false;
		Assert.assertTrue("Added type is not present" ,listofMessageName.contains("AddedType"));
		for(String list : msgMap2.get("AddedType")){
			
			if(list.contains("elemF")){
				containsValue = true;
			}
		}
		Assert.assertTrue("elemF  is not in the file",containsValue);
		for(String list : msgMap2.get("SampleComplexType")){
			
			if(list.contains("Value11")){
				containsValue = true;
			}
		}
		Assert.assertTrue("value11  is not in the file",containsValue);
		
		
	}
	
	
	public List<String> getMessageNameList(List<Message> msg){
		
		List<String> list = new ArrayList<String>();
		for(Message m : msg){
			if(m.getClass().getName().contains("Enum")){
				EnumMessage em = (EnumMessage)m;
				list.add(em.getEnumName()+"_e");
				continue;
			}
			list.add(m.getMessageName());
		}
		return list;
	}
	
	
	public Map<String,List<String>> getParamSqequenceInfoForAllMessages(File file){
		
		ProtoFileParser parser1 = new ProtoFileParser(file);
		List<Message> msg = parser1.parse();
		
		return getMessageInfo(msg);
	}
	
public Map<String,List<String>> pmdInfoForAllMessages(File file,List<String> listofMessageName,ProtoFileParser parser ){
		
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
				if(msgName.contains("_e")){
					String msgN = msgName.substring(0,msgName.length()-2);
					if(info.getMessageName().trim().equalsIgnoreCase((msgN.trim()))){
						
						pmd.add(msgN+info.getFieldName().toUpperCase()+info.getSequenceNumber());
						continue;
					}
				}
				if(info.getMessageName().trim().equalsIgnoreCase((msgName.trim()))){
					
					pmd.add(info.getFieldName()+info.getSequenceNumber());
				}
				
			}
			msgMap3.put(msgName,pmd);
		}
		
		return msgMap3;
	}
	
	
	public void assertTagAssignment(List<Message> msg){
		
		int repFieldCount = 0;
		int optFieldCount = 0;
		
		int highSequenceNo = 0;
		
		
		for(Message  m: msg){
			
			if( m.getClass().getName().contains("EnumMessage")){
				continue;
			}
			for(Field f : m.getFields()){
				if(f.getFieldRestriction().equals("repeated") || f.getFieldRestriction().equals("required")){
					repFieldCount++;
				}
				if(f.getFieldRestriction().equals("optional")){
					optFieldCount++;
				}
			}
		
		
				if((repFieldCount >= 10)){
					highSequenceNo = repFieldCount +optFieldCount;
				}
				if((repFieldCount < 10)){
					highSequenceNo = 10+ optFieldCount;
				}
	
		
				boolean assigned =false;
			
			
				
				  for(int i =1; i<=highSequenceNo;i++){
					  
					  
					  if(i > repFieldCount && i <= 10){
						  i =11;
						  
					  }
					  if( i >= 11){
						  for(Field f : m.getFields()){
							  if(Integer.valueOf(f.getSequenceNumber().trim()) == i){
								  assigned =true;
								  if(repFieldCount < 10)
								  Assert.assertTrue("Tag value "+ i+ " is assigned to other than optional field ",f.getFieldRestriction().equals("optional"));
							  }
						  }
						  Assert.assertTrue("Tag value " + i + " is not assigned",assigned);
					      assigned = false;
						  continue;
					  }
					  for(Field f : m.getFields()){
						  if(Integer.valueOf(f.getSequenceNumber().trim()) == i){
							  assigned =true;
							  Assert.assertTrue("Tag value "+ i+ " is assigned to other than repeated or required field ",f.getFieldRestriction().equals("repeated") || f.getFieldRestriction().equals("required"));
						  }
					  }
					  Assert.assertTrue("Tag value " + i + " is not assigned",assigned);
				      assigned = false; 
				  }
				
				  repFieldCount =0;
				  optFieldCount =0;
				  highSequenceNo =0;
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
	
	
	
	
	
	public void generateJaxbClasses(String path,String destDir) throws Exception{
		String [] testArgs = {"-serviceName","CalculatorService",
				  "-genType","ServiceFromWSDLIntf",	
				  "-wsdl",path,
				  "-gip","com.ebay.test.soaframework.tools.codegen",
				  "-dest",destDir,
				  "-src",destDir,
				  "-bin",destDir+"/bin",
				  "-slayer","INTERMEDIATE",
				  "-nonXSDFormats","protobuf",
				  "-enabledNamespaceFolding",
				  "-scv","1.0.0",
				  "-pr",destDir};
		
		ServiceGenerator sgen = new ServiceGenerator();
		sgen.startCodeGen(testArgs);
		
	}
	
	

}
