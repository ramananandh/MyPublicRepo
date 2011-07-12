package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.builders.BaseCodeGenerator;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufSchema;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.ebayopensource.turmeric.tools.codegen.util.JavacHelper;

public class ProtoBufCompiler {

	private static final ProtoBufCompiler m_instance = new ProtoBufCompiler();
	
	private ProtoBufCompiler(){		
	}
	
	private static Logger s_logger = LogManager.getInstance(ProtoBufCompiler.class);	
	
	private Logger getLogger() {
		return s_logger;
	}	

	public static ProtoBufCompiler getInstance(){
		return m_instance;
	}
	
	public void compileProtoFile(ProtobufSchema protobufSchema, 
			CodeGenContext codeGenContext) throws CodeGenFailedException{
		getLogger().log(Level.INFO, "Start protoc compiler invocation");
		
		String protoPath = "--proto_path=" + protobufSchema.getDotprotoTargetDir();
		String protoFileLocation = protobufSchema.getDotprotoTargetDir() + "/" + protobufSchema.getDotprotoFileName();
		String destLocation = "--java_out=" + codeGenContext.getJavaSrcDestLocation();
		
		File protocFile = createTempExeFile(codeGenContext);
		if(protocFile == null)
			throw new CodeGenFailedException();
		String protoLoc = protocFile.getAbsolutePath();
		List<String> command = new ArrayList<String>();
		command.add(protoLoc);
		command.add(protoPath);
		command.add("--error_format=gcc");
		command.add(destLocation);	
		command.add(protoFileLocation);
		
		getLogger().log(Level.INFO, "protoc invoked with the following options \n" + command);
		
        ProcessBuilder builder = new ProcessBuilder(command);
        Process protocProcess = null;
        try {
			protocProcess = builder.start();
			writeProcessOutput(protocProcess);
		} catch (CodeGenFailedException codeGenFailedException) {
			throw codeGenFailedException;
		} catch (Exception exception) {
			throw new CodeGenFailedException(exception.getMessage(), exception);
		}finally{
			try {
				String jProtoClassName = protobufSchema.getJProtoOuterClassName();
				BaseCodeGenerator.compileJavaFile(CodeGenUtil.toJavaSrcFilePath(
						codeGenContext.getJavaSrcDestLocation(), jProtoClassName), codeGenContext.getBinLocation());
				JavacHelper.addToClasspath(codeGenContext.getBinLocation());
			} catch (Exception exception) {
				throw new CodeGenFailedException(exception.getMessage(), exception);
			}
			deleteFile( protocFile );
		}
	}
	
    private static boolean deleteFile(File file){
    	return file.delete();
    }
	private File createTempExeFile(CodeGenContext codeGenContext) {
		File outputFile = new File(codeGenContext.getDestLocation() + "/protoc.exe");
		try {
			InputStream jarInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/proto/protoc.exe");
			if(jarInputStream!=null) {
				   FileOutputStream os=new FileOutputStream(outputFile);
		            final byte[] buf = new byte[1024];
		            int i = 0;
		            while((i = jarInputStream.read(buf)) != -1) {
		            	os.write(buf, 0, i);
		            }

		           jarInputStream.close();
			       os.close();
			} else {
				getLogger().log(Level.SEVERE, " The protoc.exe could not be copied as the file " +
						"was not available in the classpath. Make sure protobuf.jar is in the classpath.");
			}
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, " Could not copy protoc.exe from the jar properly, this will lead to codegen failure");
		}
		return outputFile;
	}
	

    private void writeProcessOutput(Process process) throws Exception{
    	
    	StringBuilder builder = new StringBuilder();
		InputStream es = process.getErrorStream();
		InputStreamReader esr = new InputStreamReader(es, Charset.defaultCharset());
		BufferedReader ebr = new BufferedReader(esr);

//		InputStream os = process.getInputStream();
//		InputStreamReader osr = new InputStreamReader(os);
//		BufferedReader obr = new BufferedReader(osr);
		String line;

		builder.append("Compilation of Proto files generated resulted in the following error \n");
		try{
			while ((line = ebr.readLine()) != null) {
				String[] errormessage = line.split(":");
				if(errormessage.length == 4){
					
					builder.append("Filename : ").append(errormessage[0])
					.append("\nLine : ").append(errormessage[1])
					.append("\nColumn : ").append(errormessage[2])
					.append("\nErrorMessage : ").append(errormessage[3]);
					
					builder.append("\n");
				} else
					builder.append(line).append("\n");
				
			}
			
		}finally{
			ebr.close();
			es.close();
		}
		
//		os.close();
		
		if(process.exitValue() != 0){
			getLogger().log(Level.INFO, "Compilation of Proto files generated resulted in " +
					"the following error:\n" + builder.toString());			
			CodeGenFailedException codeGenFailedException = new CodeGenFailedException(builder.toString());
			codeGenFailedException.setMessageFormatted(true);
			throw codeGenFailedException;
		}
		
    }
}
