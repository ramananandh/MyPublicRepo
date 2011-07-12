package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.eproto;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

public class EnumEProtoGenerator extends BaseEProtoGenerator {
		
	private static Logger s_logger = LogManager.getInstance(EnumEProtoGenerator.class);	
	
	private static EnumEProtoGenerator s_EnumEProtoGenerator  = new EnumEProtoGenerator();	
	
	private Logger getLogger() {
		return s_logger;
	}	
	
	private EnumEProtoGenerator() {}
	
	public static EnumEProtoGenerator getInstance() {
		return s_EnumEProtoGenerator;
	}
	
	/**
	 * Entry method for the EnumEProto generation.
	 * 
	 * Generates EProto class files for all ENUM types available in the model.
	 * The EProto classes acts as the mediator between protoformat and the JAXB objects and helps in serialization/deserialization.
	 * EnumEproto class is just a Utility class that has getter and newInstance method to get/set JAXB/JProto data. Unlike EProtos
	 * for other complex types, this does not extend the POJO class.
	 *   
	 */
	
	public  void createJCodeModel(JAXB2EJProtoMapping mapping, Class<?> fullyQualifiedJAXBName
			, CodeGenContext codeGenContext) throws CodeGenFailedException{
		JCodeModel codeModel = new JCodeModel();

		JDefinedClass targetClass = createNewClass(codeModel, mapping.getEprotoName());
		
		addGetEnum(mapping, fullyQualifiedJAXBName, targetClass, codeModel);
		addNewInstance(mapping, fullyQualifiedJAXBName, targetClass, codeModel);
		generateJavaFile(codeModel, codeGenContext.getJavaSrcDestLocation());
		getLogger().log(Level.INFO, "Successfully generated EProto for " + fullyQualifiedJAXBName.getName());
		
	}

	/**
	 * Adds the getEnum() method.
	 */
	private  JMethod addGetEnum(JAXB2EJProtoMapping mapping, Class<?> fullyQualifiedJAXBName, 
					JDefinedClass targetClass, JCodeModel codeModel) throws CodeGenFailedException{
		

//-------------------------------------------------------------------------------- 
//		Creates
//				
//			    
//	    public static <JAXBEnum> getEnum(<JProtoEnumName> enumValue) {
//
//	        if (enumValue == <JProtoEnumName>.<Value>) {
//	            return JAXBEnum.<Value>;
//	        }
//		..
//		..
//	        if (enumValue == <JProtoEnumName>.<Value>) {
//	            return JAXBEnum.<Value>;
//	        }
//	        return null;
//	    }
//-------------------------------------------------------------------------------- 
		
		
		String parseFromMethodName = "getEnum";
		String getEnumArg = "enumValue";
		JFieldRef argRef = JExpr.ref(getEnumArg);
		
		JClass returnTypeClass = getJClass(fullyQualifiedJAXBName, codeModel);
//		JClass argClass = getJClass(getJProtoMessageName(mapping.getComplexTypeName(), 
//				mapping.getJprotoName()), codeModel); 
		JClass argClass = getJClass(mapping.getJprotoName(), codeModel); 

		JMethod getEnumMethod = addMethod(targetClass, parseFromMethodName, 
					JMod.STATIC | JMod.PUBLIC , returnTypeClass);
		getEnumMethod.param(argClass, getEnumArg);				
		JBlock methodBody = getEnumMethod.body();
		
		Object[] enums = fullyQualifiedJAXBName.getEnumConstants();
		
		for (Object field : enums) {
			JExpression rhsExpression = argClass.staticRef(field.toString());
			JConditional ifServiceLocCondition = methodBody._if(argRef.eq(rhsExpression));
		
			ifServiceLocCondition._then()._return(returnTypeClass.staticRef(field.toString()));
		}
		methodBody._return(JExpr._null());
		return getEnumMethod;		
	}
	

	/**
	 * Adds the newInstance() method.
	 */
	private  JMethod addNewInstance(JAXB2EJProtoMapping mapping, Class<?> fullyQualifiedJAXBName, 
					JDefinedClass targetClass, JCodeModel codeModel) throws CodeGenFailedException{
		

//-------------------------------------------------------------------------------- 
//		Creates
//				
//		public static JProtoEnumName newInstance(<JAXBEnum> enumValue) {
//      	if (enumValue == <JAXBEnum>.<Value>) {
//          return <JProtoEnumName>.<Value>;
//      	}
//			..
//			..
//      	if (enumValue == <JAXBEnum>.<Value>) {
//          	return <JProtoEnumName>.<Value>;
//      	}
//      	return null;
//    	}

//-------------------------------------------------------------------------------- 

		
		String getEnumMethodName = "newInstance";
		String getEnumArg = "enumValue";
			
		JFieldRef argRef = JExpr.ref(getEnumArg);
		
//		JClass returnTypeClass = getJClass(getJProtoMessageName(mapping.getComplexTypeName(), 
//				mapping.getJprotoName()), codeModel); 
		JClass returnTypeClass = getJClass(mapping.getJprotoName(), codeModel); 
						
		JClass argClass = getJClass(fullyQualifiedJAXBName, codeModel);
		JMethod getEnumMethod = addMethod(targetClass, getEnumMethodName, 
				JMod.STATIC | JMod.PUBLIC , returnTypeClass);

		getEnumMethod.param(argClass, getEnumArg);		
		JBlock methodBody = getEnumMethod.body();
				
		Object[] enums = fullyQualifiedJAXBName.getEnumConstants();
		
		for (Object field : enums) {
			JExpression rhsExpression = argClass.staticRef(field.toString());
			JConditional ifServiceLocCondition = methodBody._if(argRef.eq(rhsExpression));
		
			ifServiceLocCondition._then()._return(returnTypeClass.staticRef(field.toString()));
		}
		
		methodBody._return(JExpr._null());		
		return getEnumMethod;		
	}

}
