package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.eproto;


import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.runtime.binding.exception.SerializationException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufSchema;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.ebayopensource.turmeric.tools.codegen.util.ContextClassLoaderUtil;

import com.google.protobuf.ByteString;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;

public class EProtoGenerator extends BaseEProtoGenerator {
	
	private static final String BUILDERMEMBER =  "m_Builder";
	private static final String JAXBPOJOCLASS = "JAXBPOJOCLASS";
	private static final String GETENUMMETHOD = "getEnum";
	
	private Map<String, JAXB2EJProtoMapping> jaxbProtoMapping;
	
	private List<Class<?>> wrapperClasses;
	
	private static Logger s_logger = LogManager.getInstance(EProtoGenerator.class);	
	
	private static EProtoGenerator s_EProtoGenerator  = new EProtoGenerator();	
	
	private Logger getLogger() {
		return s_logger;
	}	
	
	private EProtoGenerator() {}
	
	public static EProtoGenerator getInstance() {
		return s_EProtoGenerator;
	}
	
	
	/**
	 * Entry method for the EProto generation.
	 * 
	 * Generates EProto class files for all the types available in the model except for abstract types.
	 * The EProto classes acts as the mediator between protoformat and the JAXB objects and helps in serialization/deserialization.
	 * EProto classes has all the getters in the super class (JAXB class) overridden with logic to work with protobuf. In addition 
	 * to the getters, a newInstance method gives the JProto object given a POJO object.  
	 */
	
	public  void generate(ProtobufSchema protobufSchema, 
				CodeGenContext codeGenContext) throws CodeGenFailedException{
		
		if(protobufSchema == null){
			throw new CodeGenFailedException("EProto generation Failed, the proto model is empty and" +
					" hence cannot proceed with EProto generation ");			
			
		}
		
		populateMapping(protobufSchema);
		
		for(Entry<String, JAXB2EJProtoMapping> mapEntry : jaxbProtoMapping.entrySet()){
			String jaxbName = mapEntry.getKey();
			Class<?> jaxbPojoClass = ContextClassLoaderUtil.loadRequiredClass(jaxbName);
			
			if(Modifier.isAbstract(jaxbPojoClass.getModifiers()))
				continue;
			else if(jaxbPojoClass.isEnum()){
				/**
				 * EProto for ENUM is a little different and hence we have a seperate generator for those types.
				 */
				EnumEProtoGenerator generator = EnumEProtoGenerator.getInstance();
				generator.createJCodeModel(mapEntry.getValue(), jaxbPojoClass, codeGenContext);
				getLogger().log(Level.INFO, "Successfully generated EProto for " + jaxbName);
				continue;
			}
			JAXB2EJProtoMapping mapValue = mapEntry.getValue();
			
			EProtoContext protoContext = getPojoInfo(mapValue.getEprotoName(),
					mapValue.getComplexTypeName(),
					mapValue.getJprotoName(),
					mapValue.getJaxbName());
			protoContext.setRootType(mapValue.isRootType());
			createJCodeModel(protoContext, codeGenContext);
			getLogger().log(Level.INFO, "Successfully generated EProto for " + jaxbName);
			
		}
	}

	/**
	 * Creates a context that would be used during the EProto generation. The context would contain
	 * Message name, JAXB name, JProto name, EProto name that are obtained from the Proto Model 
	 * populated in the previous step.   
	 * Also populates the 8 wrapper classes for the primitve types in a list
	 * which would be used in following methods
	 */
	
	private void populateMapping(ProtobufSchema protobufSchema){
		getLogger().log(Level.INFO, "Populating the map with the information from proto model generation ");
		
		jaxbProtoMapping = new HashMap<String, JAXB2EJProtoMapping>();
		wrapperClasses = new ArrayList<Class<?>>();
		List<ProtobufMessage> protoMessage = protobufSchema.getMessages();
		
		for (ProtobufMessage protobufMessage : protoMessage) {
			JAXB2EJProtoMapping protoMapping = new JAXB2EJProtoMapping();
			
			protoMapping.setRootType(protobufMessage.isRootType());
			protoMapping.setComplexTypeName(protobufMessage.getMessageName());
			protoMapping.setEprotoName(protobufMessage.getEprotoClassName());
			protoMapping.setJaxbName(protobufMessage.getJaxbClassName());
			protoMapping.setJprotoName(protobufMessage.getJprotoClassName());
			
			jaxbProtoMapping.put(protobufMessage.getJaxbClassName(), protoMapping);
		}
		wrapperClasses.add(Byte.class);
		wrapperClasses.add(Short.class);
		wrapperClasses.add(Integer.class);
		wrapperClasses.add(Long.class);
		wrapperClasses.add(Float.class);
		wrapperClasses.add(Double.class);
		wrapperClasses.add(Character.class);
		wrapperClasses.add(Boolean.class);
		wrapperClasses.add(String.class);
		wrapperClasses.add(BigDecimal.class);
		wrapperClasses.add(BigInteger.class);
		wrapperClasses.add(QName.class);
		
		getLogger().log(Level.FINE, "Proto model details in the map \n" + jaxbProtoMapping.toString());
		
	}
	

	
	public  void createJCodeModel(EProtoContext info, 
			CodeGenContext codeGenContext) throws CodeGenFailedException{

		getLogger().log(Level.FINE, "Starting codegeneration for " + info.getFullyQualifiedEProtoName());
		JCodeModel codeModel = new JCodeModel();

		JDefinedClass targetClass = createNewClass(codeModel, info.getFullyQualifiedEProtoName());
		
		JClass basePOJOClazz = getJClass(info.getFullyQualifiedName(), codeModel);
		extend(targetClass, basePOJOClazz);
		addFieldsAndConstructor(info, targetClass, codeModel);
		addGetters(info, targetClass, codeModel);
		if(info.isRootType()){
			addParseFrom(info, targetClass, codeModel);
			addGetDeserializedJProtoObject(info, targetClass, codeModel);
		}
		addGetJProtoBuilderObject(info, targetClass, codeModel);
		addNewInstanceMethod(info, targetClass, codeModel);

		generateJavaFile(codeModel, codeGenContext.getJavaSrcDestLocation());
	}

	/**
	 * Adds the fields and COnstructor for the EProto class to be generated.
	 * the fields are 
	 * 
	 * 		1) a constant containing fully qualified POJO class name
	 * 		2) JProto builder member 
	 * 
	 * and the constructor with the JProto as the argument
	 */
	private  void addFieldsAndConstructor(EProtoContext info, JDefinedClass targetClass, JCodeModel codeModel) 
						throws CodeGenFailedException{


//-------------------------------------------------------------------------------- 
//		Creates
//		
//	    private final static String JAXBPOJOCLASS = <fullyQualifiedPOJOClassname>;
//	    private<JProtoMessageName>.Builder m_Builder = null;
//
//	    public <EProtoClassName>(<JProtoMessageName> errormessage) {
//	        m_Builder = <JProtoMessageName>.newBuilder((errormessage));
//	    }
//-------------------------------------------------------------------------------- 
		getLogger().log(Level.FINE, "Adding constructors and fields for " + info.getFullyQualifiedEProtoName());
		JClass stringJClass = getJClass(String.class, codeModel);
		
		Class<?> something = ContextClassLoaderUtil.loadRequiredClass(info.getBuilderClass());
		JClass builderClass = getJClass(something, codeModel);
		targetClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, stringJClass, JAXBPOJOCLASS, 
								JExpr.lit(info.getFullyQualifiedName()));
		targetClass.field(JMod.PRIVATE, builderClass, BUILDERMEMBER, JExpr._null());
	
		String newBuilderMethod = "newBuilder";
		String consArg = info.getComplexTypeName().toLowerCase();
		JMethod constructorWithArgs = targetClass.constructor(JMod.PUBLIC);		
		JClass protoMessageClass = getJClass(info.getFullyQualifiedJProtoMessageName(), codeModel);
		constructorWithArgs.param(protoMessageClass, consArg);
		JBlock constructorWithArgsBody = constructorWithArgs.body();
		
		JInvocation parseFromInvoker = protoMessageClass.staticInvoke(newBuilderMethod);		
		parseFromInvoker.arg(JExpr.direct(consArg));		
		constructorWithArgsBody.assign(JExpr.ref(BUILDERMEMBER), parseFromInvoker);
		
	}

	/**
	 * Generates Getters methods for all the fields defined in the POJO classes. The getters in the 
	 * POJO classes are overridden and the logic involves in getting POJO information from the 
	 * underlying proto information. The getters logic will be different for different return types namely
	 * 		1)primitive types
	 * 		2)String
	 * 		3)Date
	 * 		4)Enum
	 * 		5)User defined Complex type
	 * 		6)List of types (complex and simple)
	 */
	private  void addGetters(EProtoContext info, JDefinedClass targetClass, JCodeModel codeModel) 
						throws CodeGenFailedException{
		getLogger().log(Level.FINE, "Adding getter method for " + info.getFullyQualifiedEProtoName());
		List<Method> getterMethods = info.getMethods();
		
		for (Method method : getterMethods) {
			JMethod addedMethod = overrideGetter(method, targetClass, codeModel);

			getLogger().log(Level.FINE, "Adding " + method.getName() + " getter method of the class " 
					+ info.getFullyQualifiedEProtoName());
			
			// To add @Override to all the getters.
			
			addedMethod.annotate(Override.class);
			
			String fieldName = info.getGetterToFields().get(method.getName());			
			JBlock methodBody = addedMethod.body();		
			
			   
			if(method.getReturnType().isPrimitive()){
				primitiveMethodLogic(info, method, addedMethod, methodBody, codeModel, fieldName);
			} 
			else if(wrapperClasses.contains(method.getReturnType())){
				checkMembersForNull(methodBody, fieldName, codeModel, false);
				stringMethodLogic(info, method, addedMethod, methodBody, codeModel, false, fieldName);
			} 
			else if(method.getReturnType().equals(XMLGregorianCalendar.class)
							|| method.getReturnType().equals(Duration.class)){
				checkMembersForNull(methodBody, fieldName, codeModel, false);
				dateMethodLogic(info, method, addedMethod, methodBody, codeModel, fieldName);
			} 
			else if(method.getReturnType().isEnum()){
				checkMembersForNull(methodBody, fieldName, codeModel, false);
				addComplexTypeMethodLogic(info, method, addedMethod, methodBody, codeModel, true, fieldName);
			} 
			else if(method.getReturnType().isArray()){
				checkMembersForNull(methodBody, fieldName, codeModel, false);
				stringMethodLogic(info, method, addedMethod, methodBody, codeModel, true, fieldName);
			} 
			else{
				if(method.getReturnType().equals(List.class)){
					checkMembersForNull(methodBody, fieldName, codeModel, true);
					addListMethod(info, method, addedMethod, methodBody, codeModel, targetClass, fieldName);
				}
				else{ 
					checkMembersForNull(methodBody, fieldName, codeModel, false);
					addComplexTypeMethodLogic(info, method, addedMethod, methodBody, codeModel, false, fieldName);
				}
			}
		}
		
	}
	
	private void checkMembersForNull(JBlock methodBody, String fieldName, JCodeModel codeModel, boolean isList){
		
		JFieldRef fieldNameRef = JExpr.ref(fieldName);
		JConditional ifValNullCondition = null;
		if(isList){
			JExpression isEmptyExpression = fieldNameRef.invoke("isEmpty").not();
			ifValNullCondition = methodBody._if(fieldNameRef.ne(JExpr._null()).cand(isEmptyExpression));
		}
		else
			ifValNullCondition = methodBody._if(fieldNameRef.ne(JExpr._null()));
		ifValNullCondition._then()._return(fieldNameRef);
		
	}


	private  JMethod overrideGetter(Method method, JDefinedClass targetClass, 
			JCodeModel codeModel) throws CodeGenFailedException{		
		
			JType returnType = getJType(method.getGenericReturnType(), codeModel);
			JMethod eachMethod = addMethod(targetClass, method.getName(), method.getModifiers() , returnType);

			return eachMethod;
		
	}
	


	private  void addHasFieldCheck(JFieldRef valueFieldRef, JExpression actualExpression,
				JBlock methodBody, String hasFieldMethodName) {	
		
		JFieldRef builderField = JExpr.ref(BUILDERMEMBER);
		JExpression hasFieldExpression = builderField.invoke(hasFieldMethodName);
		JConditional ifServiceLocCondition = methodBody._if(hasFieldExpression);
		
		ifServiceLocCondition._then().assign(valueFieldRef, actualExpression);	
			
		
	}
	

	private  void addGetFieldCountForListCheck(JBlock methodBody, JExpression pojoListRef, String getParamCountMethodName) {	
		JFieldRef builderField = JExpr.ref(BUILDERMEMBER);
		JExpression hasFieldExpression = builderField.invoke(getParamCountMethodName).gt(JExpr.lit(0)).not();
		JConditional ifServiceLocCondition = methodBody._if(hasFieldExpression);
		
		ifServiceLocCondition._then()._return(pojoListRef);	
		
	}


	/**
	 * Getter method for primitive types like int, boolean, long etc. Straight forward logic. 
	 * No null check, no usage of EProto classes.
	 * Generates the following construct.
	 */
	private void primitiveMethodLogic(EProtoContext info, Method method, JMethod addedMethod, 
						JBlock methodBody, JCodeModel codeModel, String filedName){

//-------------------------------------------------------------------------------- 
//		Creates
//						
//			    @Override
//			    public <primitive type> get<MethodName>() {
//			        com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = getDescriptor(m_Builder, fieldName);
//			        return (<Boxed type> m_Builder.getField((fieldDescriptor)));
//			    }
//-------------------------------------------------------------------------------- 
		
		JFieldRef builderField = JExpr.ref(BUILDERMEMBER);
		JType returnJType = getJType(method.getGenericReturnType(), codeModel);
		String getterMethodName = getGetterMethodNameForFields(info, method);
		JInvocation getFieldInvocation = builderField.invoke(getterMethodName);
		
		JPrimitiveType primitiveType = (JPrimitiveType) returnJType;
		JType wrapperType = returnJType.boxify();
		JExpression actualParamExpr = primitiveType.unwrap(JExpr.cast(wrapperType, getFieldInvocation));
		methodBody._return(actualParamExpr);
	
	}
	
	/**
	 * Getter method for String types. Straight forward logic. No null check, no usage of EProto classes.
	 * Generates the following construct.
	 */
	private void stringMethodLogic(EProtoContext info, Method method, JMethod addedMethod, JBlock methodBody, 
			JCodeModel codeModel, boolean isArray, String filedName){
	
//-------------------------------------------------------------------------------- 
//		Creates
//				
//	    @Override
//	    public String get<MethodName>() {
//	        com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = getDescriptor(m_Builder, fieldName);
//	        return ((String) m_Builder.getField((fieldDescriptor)));
//	    }
//-------------------------------------------------------------------------------- 
		String valueVariable = "protobufTempValue";
		Class<?> returnType = method.getReturnType();
		JClass stringClass = getJClass(String.class, codeModel);
		JClass integerClass = getJClass(Integer.class, codeModel);
		JFieldRef builderField = JExpr.ref(BUILDERMEMBER);
		JFieldRef valueVariableField = JExpr.ref(valueVariable);
		JType returnJType = getJType(method.getGenericReturnType(), codeModel);
		String getterMethodName = getGetterMethodNameForFields(info, method);
		JInvocation getFieldInvocation = builderField.invoke(getterMethodName);
		String hasFieldMethodName = getHasFieldMethodNameForFields(info, method);
		JExpression actualParamExpr = null;
		
		if(returnType.equals(BigInteger.class) || returnType.equals(BigDecimal.class)
					|| returnType.equals(QName.class)){
			actualParamExpr = JExpr.cast(stringClass, getFieldInvocation);
			methodBody.decl(stringClass, valueVariable, JExpr._null());
			addHasFieldCheck(valueVariableField, actualParamExpr, methodBody, hasFieldMethodName);
			JClass bigJClass = getJClass(returnType, codeModel);
			actualParamExpr = JExpr._new(bigJClass).arg(valueVariableField);
			
			addEqualsNullCondition(methodBody, valueVariableField);
		} 
		else if(returnType.equals(Short.class) || returnType.equals(Byte.class)){
				actualParamExpr = JExpr.cast(integerClass, getFieldInvocation);
				methodBody.decl(integerClass, valueVariable, JExpr._null());
				addHasFieldCheck(valueVariableField, actualParamExpr, methodBody, hasFieldMethodName);
				JClass bigJClass = getJClass(returnType, codeModel);
				actualParamExpr = JExpr._new(bigJClass).arg(valueVariableField.invoke("toString"));
				
				addEqualsNullCondition(methodBody, valueVariableField);
			
		}
		else if(isArray){

			if(returnType.getCanonicalName().equals("byte[]")){
				JClass byteStringClass = getJClass(ByteString.class, codeModel);
				actualParamExpr = JExpr.cast(byteStringClass, getFieldInvocation);
				methodBody.decl(byteStringClass, valueVariable, JExpr._null());
				addHasFieldCheck(valueVariableField, actualParamExpr, methodBody, hasFieldMethodName);
				actualParamExpr = valueVariableField.invoke("toByteArray");
				
			}
		}
		else{
					
			actualParamExpr = JExpr.cast(returnJType, getFieldInvocation);
			methodBody.decl(returnJType, valueVariable, JExpr._null());	
			addHasFieldCheck(valueVariableField, actualParamExpr, methodBody, hasFieldMethodName);			
//			addEqualsNullCondition(methodBody, valueVariableField);	
			actualParamExpr = valueVariableField;
		}
		JFieldRef fieldNameRef = JExpr.ref(filedName);
		methodBody.assign(fieldNameRef, actualParamExpr);
		methodBody._return(fieldNameRef);
	
	}
	
	private void addEqualsNullCondition(JBlock methodBody, JFieldRef valueVariableField){

		JConditional ifValNullCondition = methodBody._if(valueVariableField.eq(JExpr._null()));
		ifValNullCondition._then()._return(JExpr._null()); 
	}
	

	/**
	 * 	Date has to be handled separately unlike other complex types. This method overrides the corresponding date
	 * 	method in the JAXB class and includes the logic to convert between long(time in milliseconds) and
	 * 	complex type (XMLGregorianCalendar)
	 */
	private void dateMethodLogic(EProtoContext info, Method method, JMethod addedMethod, JBlock methodBody, 
			JCodeModel codeModel, String filedName){
		

//-------------------------------------------------------------------------------- 
//		Creates the following based on the return type
//		
//	    @Override
//	    public XMLGregorianCalendar getTimestamp() {
//	        com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = getDescriptor(m_Builder, "timestamp");
//	        Long val = ((Long) m_Builder.getField((fieldDescriptor)));
//	        if (val == null) {
//	            return null;
//	        }
//	        try {
//	            GregorianCalendar greCal = new GregorianCalendar();
//				greCal.setTimeInMillis(timeStamp);
//	            return DatatypeFactory.newInstance().newXMLGregorianCalendar(greCal);
//	        } catch (DatatypeConfigurationException _x) {
//	            return null;
//	        }
//	    }
//		
//		
//	    @Override
//	    public Duration  getTimestamp() {
//	        com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = getDescriptor(m_Builder, "timestamp");
//	        Long val = ((Long) m_Builder.getField((fieldDescriptor)));
//	        if (val == null) {
//	            return null;
//	        }
//	        try {
//				return DatatypeFactory.newInstance().newDuration(val);
//	        } catch (DatatypeConfigurationException _x) {
//	            return null;
//	        }
//	    }
//-------------------------------------------------------------------------------- 

		
		String dateFieldDesc = "protobufTempValue";
		Class<?> returnTypeClass = method.getReturnType();
		
		JFieldRef builderField = JExpr.ref(BUILDERMEMBER);
		JFieldRef valInstanceRef = JExpr.ref(dateFieldDesc);
		
		JClass longClass = getJClass(Long.class, codeModel);
		JClass dataTypeFactoryClass = getJClass(DatatypeFactory.class, codeModel);

		JExpression datatypeFactoryExpr = dataTypeFactoryClass.staticInvoke("newInstance");
		
		String getterMethodName = getGetterMethodNameForFields(info, method);
		String hasFieldMethodName = getHasFieldMethodNameForFields(info, method);
		JInvocation getFieldInvocation = builderField.invoke(getterMethodName);
		
		
		JExpression actualParamExpr = JExpr.cast(longClass, getFieldInvocation);
		methodBody.decl(longClass, dateFieldDesc, JExpr._null());
		addHasFieldCheck(valInstanceRef, actualParamExpr, methodBody, hasFieldMethodName);
		JConditional ifValNullCondition = methodBody._if(valInstanceRef.eq(JExpr._null()));
		ifValNullCondition._then()._return(JExpr._null());	

		
		JTryBlock jTryBlock = methodBody._try();
		JBlock tryBody = jTryBlock.body();
		JFieldRef fieldNameRef = JExpr.ref(filedName);
		JExpression returnExpr = null;
		
		if(returnTypeClass.equals(XMLGregorianCalendar.class)){
			returnExpr = getXMLGregorianCalTryBlock(tryBody, codeModel, datatypeFactoryExpr, valInstanceRef,filedName);
		}
		
		else if(returnTypeClass.equals(Duration.class)){
			returnExpr = datatypeFactoryExpr.invoke("newDuration").arg(valInstanceRef);
		}
		tryBody.assign(fieldNameRef, returnExpr);
		tryBody._return(fieldNameRef);
		
		JCatchBlock jCatchBlock =
			jTryBlock._catch(getJClass(DatatypeConfigurationException.class, codeModel));
		jCatchBlock.body()._return(JExpr._null());
	
		
	}

	private JExpression getXMLGregorianCalTryBlock(JBlock tryBody, JCodeModel codeModel,
			JExpression dataFactExpr, JFieldRef valInstanceRef, String filedName){
		
		String greCalDesc = "greCal";
		JClass greCalClass = getJClass(GregorianCalendar.class, codeModel);
		
		JInvocation urlClassObjCreater = JExpr._new(greCalClass);
		JFieldRef calRef = JExpr.ref(greCalDesc);
		tryBody.decl(greCalClass, greCalDesc, urlClassObjCreater);
		
		JInvocation setMilliInvocation = calRef.invoke("setTimeInMillis").arg(valInstanceRef);
		tryBody.add(setMilliInvocation);
		
		JInvocation returnExpr = dataFactExpr.invoke("newXMLGregorianCalendar").arg(calRef);

		return returnExpr;
	}

	/**
	 * 
	 * @param info ProtoContext containing various info about the message
	 * @param method java.lang.Method object
	 * @param addedMethod
	 * @param methodBody
	 * @param codeModel
	 * @param isEnumField Decides the type of getter to be generated. Enum and COmplex type getter has very
	 * 			minimal difference and hence clubbed into one single method based on this value.
	 * @throws CodeGenFailedException 
	 */
	private void addComplexTypeMethodLogic(EProtoContext info, Method method, JMethod addedMethod, JBlock methodBody, 
			JCodeModel codeModel, boolean isEnumField, String filedName) throws CodeGenFailedException{
		
//-------------------------------------------------------------------------------- 
//		
//		Creates the following if isEnumField is true (ENUM field)
//		
//	    @Override
//	    public <Enum> get<Enum>() {
//	        com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = getDescriptor(m_Builder, enumFieldName);
//	        com.google.protobuf.Descriptors.EnumValueDescriptor val = ((com.google.protobuf.Descriptors.EnumValueDescriptor) m_Builder.getField((fieldDescriptor)));
//	        if (val == null) {
//	            return null;
//	        }
//	        return (<EnumEProto>.getEnum(<JProto>.valueOf(val)));
//	    }
//--------------------------------------------------------------------------------
		
//--------------------------------------------------------------------------------
//		
//		Creates the following if isEnumField is false (ComplexType field)
//		
//	    @Override
//	    public <JAXBType> get<JAXB Method name>() {
//	        com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = getDescriptor(m_Builder, jaxbFieldName);
//	        <JProtoMessageName> val = ( <JProtoMessageName> m_Builder.getField((fieldDescriptor)));
//	        if (val == null) {
//	            return null;
//	        }
//	        return (new  <EProtoName>(val));
//	    }
//		
//-------------------------------------------------------------------------------- 		

		JClass complexTypeDescriptorClass = null;
		String enumFieldDesc = "protobufTempValue";
		JFieldRef builderField = JExpr.ref(BUILDERMEMBER);
		JFieldRef valInstanceRef = JExpr.ref(enumFieldDesc);
		
		String methodReturnType = method.getReturnType().getName();
		if(jaxbProtoMapping.get(methodReturnType) == null){
			getLogger().log(Level.WARNING, "There seems to be duplicate class issue, the class " 
					+ methodReturnType + "or the class containing " + methodReturnType + " may contain duplicates.");			
			return;
		}
		
		String jProtoMessageClass = jaxbProtoMapping.get(methodReturnType).getJprotoName();	
		String eProtoClass = jaxbProtoMapping.get(methodReturnType).getEprotoName();


		complexTypeDescriptorClass = getJClass(jProtoMessageClass, codeModel);

		String getterMethodName = getGetterMethodNameForFields(info, method);
		String hasFieldMethodName = getHasFieldMethodNameForFields(info, method);
		JInvocation getFieldInvocation = builderField.invoke(getterMethodName);
		
//		Builder.getField always returns java.lang.Object. Hence we need to type cast it to
//		the type we need it to be.
		
		JExpression actualParamExpr = JExpr.cast(complexTypeDescriptorClass, getFieldInvocation);
		
		methodBody.decl(complexTypeDescriptorClass, enumFieldDesc, JExpr._null());
		addHasFieldCheck(valInstanceRef, actualParamExpr, methodBody, hasFieldMethodName);				
		JConditional ifServiceLocCondition = methodBody._if(valInstanceRef.eq(JExpr._null()));
		ifServiceLocCondition._then()._return(JExpr._null());
		
		JExpression returnInvocation = null;

		
//		EProto class for the types used in here may not have been generated by the time
//		the control comes here for a given EProto class. Hence we need to use JExpr.direct instead
//		of creating a JClass and invoking the methods.
		
		if(isEnumField){
			
			StringBuilder builder = new StringBuilder();
			builder.append(eProtoClass)
				.append(".").append(GETENUMMETHOD).append("(")
				.append(enumFieldDesc).append(")");

			returnInvocation = JExpr.direct(builder.toString());			
		}
		else{

			StringBuilder builder = new StringBuilder();
			builder.append("new ").append(eProtoClass)
				.append("(").append(enumFieldDesc).append(")");
			
			returnInvocation = JExpr.direct(builder.toString());			
		}

		JFieldRef fieldNameRef = JExpr.ref(filedName);
		methodBody.assign(fieldNameRef, returnInvocation);
		methodBody._return(fieldNameRef);
				
	}
	
	/**
	 * When the List types are used, the info inside the JProto list had to be copied to POJO class' list.
	 * This logic involves iterating through one list and populating the other and return the resultant list.
	 * The list can contain either User defined types or primitive types supported by java.
	 * This method generates the following construct.
	 */
	private  void addListMethod(EProtoContext info, Method method, JMethod addedMethod, JBlock methodBody, 
			JCodeModel codeModel,JDefinedClass targetClass, String filedName) throws CodeGenFailedException{
	
		
//--------------------------------------------------------------------------------
//				
//		Creates the following
//		
//	    @Override
//	    public List<JAXBType> getError() {
//	        com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = getDescriptor(m_Builder, fieldName);
//	        List<JAXBType> pojoList = new ArrayList<JAXBType>();
//	        List<JProtoMessageName> protoList = 
//					((ArrayList<JProtoMessageName> ) m_Builder.getField((fieldDescriptor)));
//	        if ((protoList!= null)&&(protoList.size()> 0)) {
//	            for (<JProtoMessageName> protobufListItem: protoList) {
//	                pojoList.add((<EProtoName>(protobufListItem)));
//	            }
//	        }
//	        return pojoList;
//	    }
//-------------------------------------------------------------------------------- 		

		
		String returnName = "pojoList";
		String protoListMember = "protoList";

		JFieldRef protoListRef = JExpr.ref(protoListMember);
		JFieldRef pojoListRef = JExpr.ref(returnName);
		//List would have only one Parameterized type
		
		Type[] typesInside = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments();
		Class<?> returnClass = null;
		if((Class.class).equals((typesInside[0]).getClass()))
			returnClass = (Class<?>) typesInside[0];
		else{
			getLogger().log(Level.WARNING, "There seems to be problem wih the" +
					" return type of the method " + method.getName() + ". This may lead to " +
					"erratic behavior during runtime");
			return;
		}
		String typeName = returnClass.getName();
		
		JAXB2EJProtoMapping mapping = jaxbProtoMapping.get(typeName);
		JFieldRef builderField = JExpr.ref(BUILDERMEMBER);
		JClass arrayListClass = getJClass(ArrayList.class, codeModel);
		
		JClass dataTypeFactoryClass = getJClass(DatatypeFactory.class, codeModel);				
		JExpression datatypeFactoryExpr = dataTypeFactoryClass.staticInvoke("newInstance");
				
		if(mapping != null || returnClass.equals(BigDecimal.class) || returnClass.equals(BigInteger.class)
				|| returnClass.equals(QName.class) || returnClass.equals(Byte.class) || returnClass.equals(Short.class) 
				|| returnClass.equals(XMLGregorianCalendar.class) || returnClass.equals(Duration.class) ){

			String foreachElement = "protobufListItem";  
			JFieldRef valInstanceRef = JExpr.ref(foreachElement);
			Class<?> protoClass = null; 
			Class<?> pojoClass = null;
			if(mapping != null){
				protoClass = ContextClassLoaderUtil.loadRequiredClass(mapping.getJprotoName());
				pojoClass = returnClass;
			}
			else if(returnClass.equals(BigDecimal.class) || returnClass.equals(BigInteger.class)
							|| returnClass.equals(QName.class)){
				pojoClass = returnClass;
				protoClass = String.class;
			}
			else if(returnClass.equals(Short.class) || returnClass.equals(Byte.class)){
				pojoClass = returnClass;
				protoClass = Integer.class;
			}
			else if(returnClass.equals(XMLGregorianCalendar.class) || returnClass.equals(Duration.class)){
				pojoClass = returnClass;
				protoClass = Long.class;
			}			
			
			
			JClass listClass = getJClass(List.class, codeModel);
			
			methodBody.decl(listClass.narrow(pojoClass), returnName, JExpr._new(arrayListClass.narrow(pojoClass)));

			String getterMethodName = getGetterMethodNameForFields(info, method);
			String fieldCountMethodName = getFieldCountMethodNameForFields(info, method);
			JInvocation getFieldInvocation = builderField.invoke(getterMethodName);

			JExpression actualParamExpr = JExpr.cast(listClass.narrow(protoClass), getFieldInvocation);
			
			addGetFieldCountForListCheck(methodBody, pojoListRef, fieldCountMethodName);
			methodBody.decl(listClass.narrow(protoClass), protoListMember, actualParamExpr);
			
			JExpression secondCondition = protoListRef.invoke("size").gt(JExpr.lit(0));
			JConditional ifServiceLocCondition = methodBody._if(protoListRef.ne(JExpr._null()).cand(secondCondition));
			
			
//			EProto class for the types used in here may not have been generated by the time
//			the control comes here for a given EProto class. Hence we need to use JExpr.direct instead
//			of creating a JClass and invoking the methods.
			
			JForEach foreachLoop = ifServiceLocCondition._then().forEach(getJClass(protoClass, codeModel), foreachElement, protoListRef);		
			JInvocation addToListInvoker = null;
			
			if(mapping != null){
				StringBuilder builder = new StringBuilder();
				builder.append("new ").append(mapping.getEprotoName()).append("(protobufListItem)");
				addToListInvoker = pojoListRef.invoke("add").arg(JExpr.direct(builder.toString()));
				foreachLoop.body().add(addToListInvoker);
			}
			else if(returnClass.equals(QName.class)){
				JClass qNameClass = getJClass(QName.class, codeModel);
				JExpression qnameValueExpr = qNameClass.staticInvoke("valueOf").arg(JExpr.ref(foreachElement));
				addToListInvoker = pojoListRef.invoke("add").arg(qnameValueExpr);
				foreachLoop.body().add(addToListInvoker);
			}
			else if(returnClass.equals(BigDecimal.class) || returnClass.equals(BigInteger.class)){
				JExpression integerValueExpr = JExpr._new(getJClass(returnClass, codeModel)).arg(JExpr.ref(foreachElement));
				addToListInvoker = pojoListRef.invoke("add").arg(integerValueExpr);
				foreachLoop.body().add(addToListInvoker);
			}
			else if(returnClass.equals(Short.class) || returnClass.equals(Byte.class)){
				JExpression integerValueExpr = JExpr._new(getJClass(returnClass, codeModel))
								.arg(JExpr.ref(foreachElement).invoke("toString"));
				addToListInvoker = pojoListRef.invoke("add").arg(integerValueExpr);
				foreachLoop.body().add(addToListInvoker);
			}
			else if(returnClass.equals(XMLGregorianCalendar.class)){

				JTryBlock jTryBlock = foreachLoop.body()._try();
				JBlock tryBody = jTryBlock.body();
				
				JExpression returnExpression = getXMLGregorianCalTryBlock(tryBody, codeModel, datatypeFactoryExpr, valInstanceRef, filedName);
				addToListInvoker = pojoListRef.invoke("add").arg(returnExpression);
				tryBody.add(addToListInvoker);
				JCatchBlock jCatchBlock =
					jTryBlock._catch(getJClass(DatatypeConfigurationException.class, codeModel));
				jCatchBlock.body()._return(JExpr._null());
			}
			else if(returnClass.equals(Duration.class)){

				JTryBlock jTryBlock = foreachLoop.body()._try();
				JBlock tryBody = jTryBlock.body();
				JExpression returnExpression = datatypeFactoryExpr.invoke("newDuration").arg(valInstanceRef);
				addToListInvoker = pojoListRef.invoke("add").arg(returnExpression);
				tryBody.add(addToListInvoker);
				JCatchBlock jCatchBlock =
					jTryBlock._catch(getJClass(DatatypeConfigurationException.class, codeModel));
				jCatchBlock.body()._return(JExpr._null());
			}
				
			
			JFieldRef fieldNameRef = JExpr.ref(filedName);
			methodBody.assign(fieldNameRef, JExpr.ref(returnName));
			methodBody._return(fieldNameRef);
				

		} else{
			 /**
			  * The collection is a list of JAVA supported types
    			@Override
    			public List<WrapperTypes of Primitive types> get<MethodName>() {
        			com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = getDescriptor(m_Builder, fieldName);
        			return ((List<WrapperTypes of Primitive types> ) m_Builder.getField((fieldDescriptor)));
    			}
			  */
			
			String valueField = "protobufTempValue";
			JFieldRef valueFieldRef = JExpr.ref(valueField);
			
			JType returnJType = getJType(method.getGenericReturnType(), codeModel);

			String getterMethodName = getGetterMethodNameForFields(info, method);
			String fieldCountMethodName = getFieldCountMethodNameForFields(info, method);
			JInvocation getFieldInvocation = builderField.invoke(getterMethodName);

			JType wrapperType = returnJType.boxify();
			JExpression actualParamExpr = JExpr.cast(wrapperType, getFieldInvocation);

			addGetFieldCountForListCheck(methodBody, JExpr._new(arrayListClass.narrow(returnClass)), fieldCountMethodName);
			methodBody.decl(returnJType, valueField, actualParamExpr);			
			
			actualParamExpr = valueFieldRef;
			

			JFieldRef fieldNameRef = JExpr.ref(filedName);
			methodBody.assign(fieldNameRef, actualParamExpr);
			methodBody._return(fieldNameRef);
			
		
		}
		
	}

	/**
	 * Generates the utility method ParseFrom. This is used to convert the stream into corresponding JAXB object.
	 */
	private  JMethod addParseFrom(EProtoContext info, JDefinedClass targetClass, JCodeModel codeModel) throws CodeGenFailedException{

//--------------------------------------------------------------------------------
//		Creates
//				
//		public static <JAXBName> parseFrom(InputStream data)
//        throws Exception
//        {
//            <JprotoMessageName> deserializedObject = null;
//            deserializedObject = <JprotoMessageName>.parseFrom((data));
//            return (new EProtoName(deserializedObject));
//        }
//--------------------------------------------------------------------------------
		getLogger().log(Level.FINE, "Adding parseFrom private method for " + info.getFullyQualifiedEProtoName());
		String parseFromMethodName = "parseFrom";
		String streamArg = "data";
		String returnName = "deserializedObject";
		
		JClass returnTypeClass = getJClass(ContextClassLoaderUtil.loadRequiredClass(info.getFullyQualifiedName()), codeModel);
		JMethod parseFromMethod = addMethod(targetClass, parseFromMethodName, 
						JMod.STATIC | JMod.PUBLIC , returnTypeClass);

		parseFromMethod.param(InputStream.class, streamArg);
		parseFromMethod._throws(Exception.class);
		
		JBlock methodBody = parseFromMethod.body();
		
		JClass protoMessageClass = getJClass(info.getFullyQualifiedJProtoMessageName(), codeModel);
		
		methodBody.decl(protoMessageClass, returnName, JExpr._null());
		JFieldRef svcInstanceRef = JExpr.ref(returnName);
		JInvocation parseFromInvoker = protoMessageClass.staticInvoke(parseFromMethodName);
		parseFromInvoker.arg(JExpr.direct(streamArg));
		methodBody.assign(svcInstanceRef, parseFromInvoker);
		
//		EProto class for the types used in here may not have been generated by the time
//		the control comes here for a given EProto class. Hence we need to use JExpr.direct 
//		instead of creating a JClass and invoking the methods.

		StringBuilder builder = new StringBuilder();
		builder.append("new ")
			.append(info.getFullyQualifiedEProtoName())
			.append("(").append(returnName)
			.append(")");
		
		
		JExpression returnInvocation = JExpr.direct(builder.toString());
		methodBody._return(returnInvocation);
		
		return parseFromMethod;
		
	}
	


	/**
	 * Generates the utility method getDeserializedJProtoObject. This is used to convert the stream into corresponding JProto object.
	 */
	private  JMethod addGetDeserializedJProtoObject(EProtoContext info, JDefinedClass targetClass, JCodeModel codeModel) 
			throws CodeGenFailedException{

//--------------------------------------------------------------------------------
//		Creates
//				
//		public static <JprotoMessageName> getDeserializedJProtoObject(InputStream data)
//        throws Exception
//        {
//            return <JprotoMessageName>.parseFrom((data));
//        }
//--------------------------------------------------------------------------------
		getLogger().log(Level.FINE, "Adding parseFrom private method for " + info.getFullyQualifiedEProtoName());
		String parseFromMethodName = "parseFrom";
		String streamArg = "data";
		
		JClass protoMessageClass = getJClass(info.getFullyQualifiedJProtoMessageName(), codeModel);
		
		JMethod deserializedObjectMethod = addMethod(targetClass, "getDeserializedJProtoObject", 
						JMod.STATIC | JMod.PUBLIC , protoMessageClass);

		deserializedObjectMethod.param(InputStream.class, streamArg);
		deserializedObjectMethod._throws(Exception.class);
		
		JBlock methodBody = deserializedObjectMethod.body();
		JInvocation parseFromInvoker = protoMessageClass.staticInvoke(parseFromMethodName);
		parseFromInvoker.arg(JExpr.direct(streamArg));
		
		methodBody._return(parseFromInvoker);
		
		return deserializedObjectMethod;
		
	}
	

	/**
	 * Generates the utility method getDeserializedJProtoObject. This is used to convert the stream into corresponding JProto object.
	 */
	private  JMethod addGetJProtoBuilderObject(EProtoContext info, JDefinedClass targetClass, JCodeModel codeModel) 
			throws CodeGenFailedException{

//--------------------------------------------------------------------------------
//		Creates
//				
//		public static <JprotoMessageName> getDeserializedJProtoObject(InputStream data)
//        throws Exception
//        {
//            return <JprotoMessageName>.parseFrom((data));
//        }
//--------------------------------------------------------------------------------
		
		Class<?> something = ContextClassLoaderUtil.loadRequiredClass(info.getBuilderClass());
		JClass builderClass = getJClass(something, codeModel);		
		
		JMethod deserializedObjectMethod = addMethod(targetClass, "getJProtoBuilderObject", 
						 JMod.PUBLIC , builderClass);
		
		JBlock methodBody = deserializedObjectMethod.body();		
		methodBody._return(JExpr.ref(BUILDERMEMBER));
		
		return deserializedObjectMethod;
		
	}
	
	/**
	 * This method is to convert the jaxb pojo type to jproto type at runtime. 
	 * This method basically uses jproto builder to build the jproto object. 
	 * This method also checks if runtime polymorphism is used and if yes, we throw a PolymorphismNotSupportedException
	 * Use Google's FieldDescriptor APIs corresponding to the field to set the values
	 * Similar to the getters logic, newInstance logic will be different for different return types namely
	 * 		1)primitive types
	 * 		2)String
	 * 		3)Date
	 * 		4)Enum
	 * 		5)User defined Complex type
	 * 		6)List of types (complex and simple)
	 */

	private  JMethod addNewInstanceMethod(EProtoContext info, JDefinedClass targetClass, 
			JCodeModel codeModel) throws CodeGenFailedException{

//--------------------------------------------------------------------------------
//	Creates
//				
//    public static <JProtoMessageName> newInstance(<JAXBPojoClassName> pojoClass)
//        throws Exception
//    {
//        if (!JAXBPOJOCLASS.equals(pojoClass.getClass().getName())) {
//            throw new PolymorphismNotSupportedException(("Polymorphism is not supported. " + 
// 			"Class " + pojoClass.getClass().getName() + " is used instead of <JAXBPojoClassName>"));
//        }
//
//        <JProtoMessageName>.Builder builder = <JProtoMessageName>.newBuilder();
//
//        com.google.protobuf.Descriptors.FieldDescriptor fieldDescriptor = null;
//
//        if (pojoClass.get<Methods>!= null) {
//            fieldDescriptor = getDescriptor(builder, <fieldName>);
//            builder.setField(fieldDescriptor, <POJOClass.get<Method>>/<EProto.newInstance>);
//        }
//
//        return builder.build();
//    }
//--------------------------------------------------------------------------------
		getLogger().log(Level.FINE, "Adding newInstance private method for " + info.getFullyQualifiedEProtoName());
		String newInstanceMethodName = "newInstance";
		String methodArg = "pojoClass";
		String returnName = "builder";
		
		JClass methodArgClass = getJClass(ContextClassLoaderUtil.loadRequiredClass(info.getFullyQualifiedName()), codeModel);
		JClass returnTypeClass = getJClass(ContextClassLoaderUtil.loadRequiredClass(info.getFullyQualifiedJProtoMessageName()), codeModel);
		JClass builderClass = getJClass(ContextClassLoaderUtil.loadRequiredClass(info.getBuilderClass()), codeModel);

		JFieldRef protoListRef = JExpr.ref(JAXBPOJOCLASS);
		JFieldRef methodArgRef = JExpr.ref(methodArg);
		JFieldRef returnRef = JExpr.ref(returnName);
		
		JMethod newInstanceMethod = addMethod(targetClass, newInstanceMethodName, 
				JMod.STATIC | JMod.PUBLIC , returnTypeClass);

		newInstanceMethod.param(methodArgClass, methodArg);
		newInstanceMethod._throws(Exception.class);		
		
		JBlock newInstanceMethodBody = newInstanceMethod.body();
				
		JClass polyNotSupportedClass = getJClass(SerializationException.class, codeModel);		
		JExpression condition = methodArgRef.invoke("getClass").invoke("getName");
		JConditional ifServiceLocCondition = newInstanceMethodBody._if(protoListRef.invoke("equals").arg(condition).not());
		
		StringBuilder errorMessageBuilder = new StringBuilder();
		errorMessageBuilder.append("\"Polymorphism is not supported. \" + \n \t\t\t\"Class \" + ")
							.append(methodArg)
							.append(".getClass().getName() + \" is used instead of ")
							.append(info.getFullyQualifiedName())
							.append("\"");
		
		JBlock ifBlock = ifServiceLocCondition._then();
		ifBlock._throw(JExpr._new(polyNotSupportedClass).arg(JExpr.direct(errorMessageBuilder.toString())));
		
		
		newInstanceMethodBody.decl(builderClass, returnName, returnTypeClass.staticInvoke("newBuilder"));

		
		List<Method> getterMethods = info.getMethods();
		for (Method method : getterMethods) {
			getLogger().log(Level.FINE, "Adding newInstance for the field " + info.getGetterToFields().get(method.getName()));
			
			JBlock insideMethodBody = newInstanceMethodBody;
			/**
			 * addStatements - convenience flag. Except for list type, the
			 * builder.setField(fieldDescriptor, <POJOClass.get<Method>>/<EProto.newInstance>);
			 * is gonna be similar for all other types. Hence the expression to be added as 
			 * argument would be figured out first and at the end of the loop(based on the flag's value) 
			 * would be added to the if block. In the case of List, it would be added as and when list is traversed. 
			 */
			boolean addStatements = true;
			JExpression setFieldSecondArg = methodArgRef.invoke(method.getName());
			JConditional ifCondition = null;
			JAXB2EJProtoMapping returntypeMapping = null;
		
			String newMethodName = getSetterMethodNameForFields(info, method);			
			
			if(!method.getReturnType().isPrimitive()){
				/** return type is primitive

        			fieldDescriptor = getDescriptor(builder, primitiveFieldName);
        			builder.setField(fieldDescriptor, pojoClass.get<Methodname>());
				 */
				ifCondition = newInstanceMethodBody._if(setFieldSecondArg.ne(JExpr._null()));
				insideMethodBody = ifCondition._then();				
				returntypeMapping = jaxbProtoMapping.get(method.getReturnType().getName());							
			}
			/** return type is String/List<PrimitiveTypes>

					if (pojoClass.get<Methodname>()!= null) {
            			fieldDescriptor = getDescriptor(builder, fieldName);
            			builder.setField(fieldDescriptor, pojoClass.get<Methodname>());
        			}
			 */
			if(method.getReturnType().equals(BigInteger.class) || method.getReturnType().equals(BigDecimal.class)
						|| method.getReturnType().equals(QName.class)){
				setFieldSecondArg = setFieldSecondArg.invoke("toString");
			}
			else if(method.getReturnType().equals(Duration.class)){

				/** return type is Duration(Date), these constructs are fixed.

						if (pojoClass.get<Methodname>()!= null) {
            				fieldDescriptor = getDescriptor(builder, dateFieldName);
            				builder.setField(fieldDescriptor, pojoClass.get<Methodname>().getTimeInMillis(Calendar.getInstance()));
        				}
				 */
				JClass calendarClass = getJClass(Calendar.class, codeModel);
				setFieldSecondArg = setFieldSecondArg.invoke("getTimeInMillis").arg(calendarClass.staticInvoke("getInstance"));
			} 
			else if(method.getReturnType().equals(XMLGregorianCalendar.class)){

				/** return type is XMLGregorianCalendar(Date), these constructs are fixed.

						if (pojoClass.get<Methodname>()!= null) {
            				fieldDescriptor = getDescriptor(builder, dateFieldName);
            				builder.setField(fieldDescriptor, pojoClass.get<Methodname>().toGregorianCalendar().getTimeInMillis());
        				}
				 */
				
				setFieldSecondArg = setFieldSecondArg.invoke("toGregorianCalendar").invoke("getTimeInMillis");
			}
			else if(method.getReturnType().getCanonicalName().equals("byte[]")){

				/** return type is byte[], these constructs are fixed.

				 */
				String byteStringVariable = "byteString";
				JFieldRef valueFieldRef = JExpr.ref(byteStringVariable);
				JClass byteStringClass = getJClass(ByteString.class, codeModel);
				
				JExpression actualParamExpr = byteStringClass.staticInvoke("copyFrom").arg(setFieldSecondArg);
				insideMethodBody.decl(byteStringClass, byteStringVariable, actualParamExpr);			
				
				setFieldSecondArg = valueFieldRef;
			}
			else if(method.getReturnType().isEnum()){
				/** return type is ENUM
	
        				if (pojoClass.get<Methodname>()!= null) {
            				fieldDescriptor = getDescriptor(builder, enumFieldName);
            				builder.setField(fieldDescriptor, (<EProtoClass>.newInstance(pojoClass.get<Methodname>()).getValueDescriptor()));
        				}
				 */
				if(returntypeMapping != null){
					StringBuilder builder = new StringBuilder();
					builder.append(returntypeMapping.getEprotoName())
							.append(".newInstance(")
							.append(methodArg).append(".")
							.append(method.getName()).append("())");
					setFieldSecondArg = JExpr.direct(builder.toString());
				}
				
			} 
			else{
				
					if(method.getReturnType().equals(List.class)){
						/** return type is List<ComplexTypes/UserDefined types>
						
        					if (pojoClass.get<Methodname>()!= null) {
            					for (<JAXBPOJOClassName> protobufListItem: pojoClass.get<Methodname>()) {
                					builder.addRepeatedField(fieldDescriptor, (<EProtoClass>.newInstance(protobufListItem)));
            					}
        					}

						 */
						String typeName = null;
						String indiMember = "protobufListItem";
						Class<?> returnClass = null;
						Type[] typesInside = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments();
						if((Class.class).equals((typesInside[0]).getClass()))
							returnClass = (Class<?>) typesInside[0];
						else{
							getLogger().log(Level.WARNING, "There seems to be problem wih the" +
									" return type of the method " + method.getName() + ". This may lead to " +
									"erratic behavior during runtime");
							continue;
						}
						typeName = returnClass.getName();						
						if(returntypeMapping == null){				
							returntypeMapping = jaxbProtoMapping.get(typeName);						
							
						}
						if(returntypeMapping != null  || returnClass.equals(BigDecimal.class) || returnClass.equals(BigInteger.class)
								|| returnClass.equals(QName.class) || returnClass.equals(Byte.class) || returnClass.equals(Short.class)
								|| returnClass.equals(XMLGregorianCalendar.class) || returnClass.equals(Duration.class)
								|| wrapperClasses.contains(returnClass)){
							
							JClass listClass = getJClass(typeName, codeModel);
							JForEach foreachLoop = ifCondition._then().forEach(listClass, indiMember, methodArgRef.invoke(method.getName()));		
							
							JInvocation addToListInvoker= returnRef.invoke(newMethodName);
							
							if(returntypeMapping != null){
								
								StringBuilder builder = new StringBuilder();							
								builder.append(returntypeMapping.getEprotoName())
											.append(".newInstance(")
											.append(indiMember).append(")");
								addToListInvoker.arg(JExpr.direct(builder.toString()));
							}
							else if(returnClass.equals(QName.class) || returnClass.equals(BigDecimal.class) 
									|| returnClass.equals(BigInteger.class)){
								// these types converted to string in proto world
								addToListInvoker.arg(JExpr.ref(indiMember).invoke("toString"));
							}
							else if(returnClass.equals(Short.class) || returnClass.equals(Byte.class)){
								// these types converted to sint32 in proto world
								JClass integerClass = getJClass(Integer.class, codeModel);
								addToListInvoker.arg(JExpr._new(integerClass).arg(JExpr.ref(indiMember)));
							}
							else if(returnClass.equals(XMLGregorianCalendar.class)){
								addToListInvoker.arg(JExpr.ref(indiMember).invoke("toGregorianCalendar").invoke("getTimeInMillis"));
							}
							else if(returnClass.equals(Duration.class)){
								JClass calendarClass = getJClass(Calendar.class, codeModel);
								addToListInvoker.arg(JExpr.ref(indiMember).invoke("getTimeInMillis")
										.arg(calendarClass.staticInvoke("getInstance")));
							}
							else if(wrapperClasses.contains(returnClass)){
								addToListInvoker.arg(JExpr.ref(indiMember));
							}
							
							JBlock forEachBlock = foreachLoop.body();
							forEachBlock.add(addToListInvoker);	
							addStatements = false;
						}

					}
					else {
						/** return type is ComplexTypes/UserDefined types
						
        					if (pojoClass.get<Methodname>()!= null) {
            					fieldDescriptor = getDescriptor(builder, fieldName);
            					builder.setField(fieldDescriptor, (<EProtoClass>.newInstance(pojoClass.get<Methodname>)));
        					}
						 */
						if(returntypeMapping != null){
							StringBuilder builder = new StringBuilder();
							builder.append(returntypeMapping.getEprotoName())
									.append(".newInstance(")
									.append(methodArg).append(".")
									.append(method.getName()).append("())");
							setFieldSecondArg = JExpr.direct(builder.toString());
						}
					}
			}
			
			JInvocation setFieldInvocation = returnRef.invoke(newMethodName);
			if(setFieldSecondArg != null)
				setFieldInvocation.arg(setFieldSecondArg);
			
			if(addStatements){
				insideMethodBody.add(setFieldInvocation);				
			}

			
		}
		
		JExpression returnInvocation = returnRef.invoke("build");
		newInstanceMethodBody._return(returnInvocation);
		
		return newInstanceMethod;		
	}

	private String getSetterMethodNameForFields(EProtoContext info, Method method){
		String newMethodName = null;
		String fieldName = CodeGenUtil.makeFirstLetterUpper(info.getGetterToFields().get(method.getName()).replace("_", ""));			
		if(method.getReturnType().equals(List.class))
			newMethodName = "add" + fieldName;
		else
			newMethodName = "set" + fieldName;		
		return newMethodName;
	}

	private String getGetterMethodNameForFields(EProtoContext info, Method method){
		String newMethodName = info.getGetterToFields().get(method.getName());
		String fieldName = CodeGenUtil.makeFirstLetterUpper(info.getGetterToFields().get(method.getName()).replace("_", ""));			
		newMethodName = "get" + fieldName;			
		if(method.getReturnType().equals(List.class))
			newMethodName = newMethodName + "List";
		return newMethodName;
	}
	
	private String getHasFieldMethodNameForFields(EProtoContext info, Method method){
		String newMethodName = info.getGetterToFields().get(method.getName());
		String fieldName = CodeGenUtil.makeFirstLetterUpper(info.getGetterToFields().get(method.getName()).replace("_", ""));			
		newMethodName = "has" + fieldName;			
		return newMethodName;
	}
	
	private String getFieldCountMethodNameForFields(EProtoContext info, Method method){
		String newMethodName = info.getGetterToFields().get(method.getName());
		String fieldName = CodeGenUtil.makeFirstLetterUpper(info.getGetterToFields().get(method.getName()).replace("_", ""));			
		newMethodName = "get" + fieldName;			
		if(method.getReturnType().equals(List.class))
			newMethodName = newMethodName + "Count";
		return newMethodName;
	}
	
}
