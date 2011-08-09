package org.ebayopensource.turmeric.tools.codegen.proto;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.ebayopensource.turmeric.tools.TestResourceUtil;
import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.CodegenTestUtils;
import org.ebayopensource.turmeric.tools.codegen.ServiceGenerator;
import org.ebayopensource.turmeric.tools.codegen.builders.BaseCodeGenerator;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.external.WSDLUtil;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.WSDLParserException;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.FastSerFormatCodegenBuilder;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.ProtoBufCompiler;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.ProtobufSchemaMapper;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.eproto.EProtoGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.exception.ProtobufModelGenerationFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufSchema;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenConstants;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.junit.Test;

public class EProtoGeneratorTest extends AbstractServiceGeneratorTestCase{
	
	
	File wsdl = getCodegenDataFileInput("CalcService.wsdl");
	
	@Override
	public File getProtobufRelatedInput(String name) {
		return TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/proto/"
				+ name);
	}
	

	private static String[] getFindItemsServiceArgs(File wsdl,File destDir) {
		
		File binDir = new File(destDir, "bin");
		String testArgs[] = new String[] {
				"-servicename",
				"FindItemsService",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "ClientNoConfig", 
				"-src",destDir.getAbsolutePath(),
				"-dest", destDir.getAbsolutePath(), 
				"-scv", "1.0.0", 
				"-bin",binDir.getAbsolutePath(),
				 "-enabledNamespaceFolding",
				"-nonXSDFormats", "protobuf" };
		return testArgs;
	}
	

	private static String[] getAllComplexTypeWsdlArgs(File wsdl, File destDir) {
		
		File binDir = new File(destDir, "bin");
		String testArgs[] = new String[] {
				"-servicename",
				"CalculatorService",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "ClientNoConfig", 
				"-envmapper", "org.ebayopensource.turmeric.tools.codegen.EnvironmentMapperImpl",
				"-src",destDir.getAbsolutePath(),
				"-dest", destDir.getAbsolutePath(), 
				"-scv", "1.0.0", 
				"-bin",binDir.getAbsolutePath(),
				 "-enabledNamespaceFolding",
				"-nonXSDFormats", "protobuf" };
		return testArgs;
	}
	

	private static String[] getComplexTypeWsdlFailure(File wsdl, File destDir) {
		
		File binDir = new File(destDir, "bin");
		String testArgs[] = new String[] {
				"-servicename",
				"CalculatorServiceFailure",
				"-wsdl",
				wsdl.getAbsolutePath(),
				"-genType", "ClientNoConfig", 
				"-src",destDir.getAbsolutePath(),
				"-dest", destDir.getAbsolutePath(), 
				"-scv", "1.0.0", 
				"-bin",binDir.getAbsolutePath(),
				 "-enabledNamespaceFolding",
				"-nonXSDFormats", "protobuf" };
		return testArgs;
	}
	
	protected CodeGenContext getCodeGenContext(String[] args) throws Exception{
		
		getToolsJar(this.getClass().getClassLoader());
		return ProtobufSchemaMapperTestUtils.getCodeGenContext(args);
	}
	
	
	protected static void getToolsJar(ClassLoader parentClassLoader)
			throws CodeGenFailedException {

		try {
			Class.forName("com.sun.tools.javac.Main", false, parentClassLoader);
			Class.forName("com.sun.tools.apt.Main", false, parentClassLoader);
			return;
		} catch (ClassNotFoundException e) {
			
		}

		File toolsJar = null;
		boolean toolsJarFound = false;
		String jdkHome = null;
		if (!CodeGenUtil.isEmptyString(ServiceGenerator.s_JdkHome)) {
			jdkHome = ServiceGenerator.s_JdkHome;
		} else
			jdkHome = System.getenv("JDK_HOME");
		
		
		if (jdkHome != null) {
			toolsJar = new File(jdkHome, "lib/tools.jar");
			if (toolsJar.exists())
				toolsJarFound = true;
		}

		if (toolsJarFound == false) {
			String javaHomeStr = System.getProperty("java.home");

			File jreHome = new File(javaHomeStr);
			toolsJar = new File(jreHome.getParent(), "lib/tools.jar");

			if (!toolsJar.exists()) {
				if (javaHomeStr.indexOf("jre") > 0
						|| javaHomeStr.indexOf("JRE") > 0) {
					if (javaHomeStr.endsWith("/")) {
						javaHomeStr = javaHomeStr + "../";
					} else {
						javaHomeStr = javaHomeStr + "/../";
					}
					jreHome = new File(javaHomeStr);
					toolsJar = new File(jreHome.getParent(), "lib/tools.jar");
				}

				if (!toolsJar.exists()) {
					String exceptionMsg = "JdkHome used for loading tools.jar is:"+ jdkHome 
					 + " \n JavaHome used is : "+ javaHomeStr;
					throw new CodeGenFailedException(exceptionMsg + " Failed to load tools.jar with these values.");
				}
			}
		}

		try {
			addURL(toolsJar.toURL());
		} catch (Exception e) {}
	}
	
	protected static void addURL(URL u) throws IOException {
		  Class[] parameters = new Class[]{URL.class};

	      URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	      Class sysclass = URLClassLoader.class;

	      try {
	         Method method = sysclass.getDeclaredMethod("addURL", parameters);
	         method.setAccessible(true);
	         method.invoke(sysloader, new Object[]{u});
	      } catch (Throwable t) {
	         t.printStackTrace();
	         throw new IOException("Error, could not add URL to system classloader");
	      }

	   }

	protected void compileGeneratedEProtos(CodeGenContext context, String eprotoName) throws Exception{
		BaseCodeGenerator.compileJavaFile(CodeGenUtil.toJavaSrcFilePath(
				context.getJavaSrcDestLocation(), eprotoName), context.getBinLocation());
	}	
	private static void generateArtifacts(CodeGenContext codeGenContext) throws Exception{
		String fastSerFormatStr = codeGenContext.getInputOptions().getSupportedFastSerFormats();
		//Is service enabled for fast ser format
		if( null == fastSerFormatStr || "".equals(fastSerFormatStr) ){
			//fast ser format not enabled. Hence return.
			return;
		}
		
		List<SchemaType> listOfSchemaTypes;
		try {
			listOfSchemaTypes = FastSerFormatCodegenBuilder.getInstance().generateSchema( codeGenContext );
		} catch (WSDLParserException e) {
			throw new CodeGenFailedException( "Generate Schema Failed.", e );
		} catch (WSDLException e) {
			throw new CodeGenFailedException( "Generate Schema Failed. Unable to created wsdl definition.", e );
		}
			
		ProtobufSchema schema;
		try {
			schema = ProtobufSchemaMapper.getInstance().createProtobufSchema(listOfSchemaTypes, codeGenContext);
		} catch (ProtobufModelGenerationFailedException e) {
			throw new CodeGenFailedException("Proto buf model generation failed.", e);
		}

		try {
			ProtoBufCompiler.getInstance().compileProtoFile(schema, codeGenContext);
		} catch (CodeGenFailedException codeGenFailedException) {
			throw codeGenFailedException;
		} catch (Exception exception) {
			throw new CodeGenFailedException(exception.getMessage(), exception);
		}
		
		try {
			EProtoGenerator.getInstance().generate(schema, codeGenContext);
		} catch (CodeGenFailedException codeGenFailedException) {
			throw codeGenFailedException;
		} catch (Exception exception) {
			throw new CodeGenFailedException(exception.getMessage(), exception);
		}
	}
	@Test
	public void testDePolymorphizedFindItemServiceWsdl() throws Exception {
		
		File wsdl1 = getProtobufRelatedInput("SearchFindItemServiceV2.wsdl");
		File destDir = testingdir.getDir();
		CodeGenContext context = getCodeGenContext(getFindItemsServiceArgs(wsdl1,destDir));

		String wsdlFileLoc = context.getInputOptions().getInputFile();
		
		if(context.getWsdlDefinition() == null){
			Definition definition = WSDLUtil.getWSDLDefinition(wsdlFileLoc);
			context.setWsdlDefinition(definition);
		}
		context.setMetaSrcDestLocation(CodeGenUtil.genDestFolderPath(destDir.getAbsolutePath(), 
				CodeGenConstants.META_SRC_FOLDER));
		WSDLUtil.populateCodegenCtxWithWSDLDetails(wsdlFileLoc, context);
		

		FastSerFormatCodegenBuilder.getInstance().buildFastSerFormatArtifacts(context);
		generateArtifacts(context);
//		compileMultipleGeneratedEProtos(context);
		
//		Class eprotoClass = IntrospectUtil.loadClass(eprotoName);
//		System.out.println(eprotoClass.getCanonicalName());

		CodeGenUtil.deleteContentsOfDir(new File(context.getJavaSrcDestLocation()));
	}
	
	
	@Test	

	public void testAllComplexTypeWsdlAWsdl() throws Exception {
		
		File wsdl1 = getProtobufRelatedInput("TestAllComplexTypeWsdl.wsdl");
		File destDir = testingdir.getDir();
		
		CodeGenContext context = getCodeGenContext(getAllComplexTypeWsdlArgs(wsdl1,destDir));

		String wsdlFileLoc = context.getInputOptions().getInputFile();
		
		if(context.getWsdlDefinition() == null){
			Definition definition = WSDLUtil.getWSDLDefinition(wsdlFileLoc);
			context.setWsdlDefinition(definition);
		}
		context.setMetaSrcDestLocation(CodeGenUtil.genDestFolderPath(destDir.getAbsolutePath(), 
						CodeGenConstants.META_SRC_FOLDER));

		WSDLUtil.populateCodegenCtxWithWSDLDetails(wsdlFileLoc, context);

		FastSerFormatCodegenBuilder.getInstance().buildFastSerFormatArtifacts(context);
		generateArtifacts(context);
		

		String targetArtifactSnippet = getCodegenDataFileInput("SnippetEProtoXMLCal.txt").getAbsolutePath();
		String generatedFileName = context.getJavaSrcDestLocation() + 
		"com/ebay/test/soaframework/tools/codegen/proto/extended/ETestAllPossibleComplexType.java";

		 CodegenTestUtils.assertGeneratedContent(new File(generatedFileName),new File(targetArtifactSnippet),"BotService","BotService",null);

		
		String targetArtifactSnippet2 = getCodegenDataFileInput("SnippetEProtoNewInstance.txt").getAbsolutePath();
		CodegenTestUtils.assertGeneratedContent(new File(generatedFileName), new File(targetArtifactSnippet2)
				,"BotService","BotService",null);

		String eprotoName = "com.ebay.test.soaframework.tools.codegen.proto.extended.ETestComplexType.java";
		compileGeneratedEProtos(context, eprotoName);
		
		CodeGenUtil.deleteContentsOfDir(new File(context.getJavaSrcDestLocation()));
	}
	
	@Test
	public void testComplexTypeWsdlFailure() throws Exception {
		
		File wsdl1 = getProtobufRelatedInput("TestAllComplexTypeWsdl.wsdl");
		File destDir = testingdir.getDir();
		CodeGenContext context = getCodeGenContext(getComplexTypeWsdlFailure(wsdl1,destDir));

		String wsdlFileLoc = context.getInputOptions().getInputFile();
		
		if(context.getWsdlDefinition() == null){
			Definition definition = WSDLUtil.getWSDLDefinition(wsdlFileLoc);
			context.setWsdlDefinition(definition);
		}

		try {
//			FastSerFormatCodegenBuilder.getInstance().buildFastSerFormatArtifacts(context);
			generateArtifacts(context);
		} catch (CodeGenFailedException e) {
			
		}
		
		
//		Class eprotoClass = IntrospectUtil.loadClass(eprotoName);
//		System.out.println(eprotoClass.getCanonicalName());

		CodeGenUtil.deleteContentsOfDir(new File(context.getJavaSrcDestLocation()));
		
	}
	

}
