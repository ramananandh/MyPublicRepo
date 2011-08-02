/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.tools.codegen.builders;


import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.runtime.common.registration.ClassLoaderRegistry;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.runtime.sif.service.EnvironmentMapper;
import org.ebayopensource.turmeric.runtime.sif.service.Service;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceInvokerOptions;
import org.ebayopensource.turmeric.tools.codegen.CodeGenContext;
import org.ebayopensource.turmeric.tools.codegen.InputOptions;
import org.ebayopensource.turmeric.tools.codegen.SourceGenerator;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;
import org.ebayopensource.turmeric.tools.codegen.util.ContextClassLoaderUtil;
import org.ebayopensource.turmeric.tools.codegen.util.IntrospectUtil;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;


/**
 * Generates consumer class for a Service
 *
 * @author rmandapati
 * @author arajmony
 */
public class ServiceConsumerGenerator extends BaseCodeGenerator implements SourceGenerator {

	private static final String GET_PROXY_METHOD_NAME = "getProxy";
	private static final String SET_SVC_LOC_METHOD_NAME = "setServiceLocation";
	private static final String GET_SVC_LOC_METHOD_NAME = "getServiceLocation";
	private static final String SET_USER_PROV_SEC_METHOD_NAME = "setUserProvidedSecurityCredentials";
	private static final String SET_SEC_CRE_SEC_METHOD_NAME = "setAuthToken";
	private static final String GET_SEC_CRE_SEC_METHOD_NAME = "getAuthToken";
	private static final String SET_COOKIES_METHOD_NAME = "setCookies";
	private static final String GET_COOKIES_METHOD_NAME = "getCookies";
	private static final String GET_SVC_INVOKER_OPTIONS_METHOD_NAME ="getServiceInvokerOptions";
	private static final String GET_INVOKER_OPTIONS_METHOD_NAME = "getInvokerOptions";
	private static final String GET_SERVICE_METHOD_NAME = "getService";
	private static final String INIT_METHOD_NAME = "init";

	private static final String SVC_LOCATION_FIELD_NAME = "m_serviceLocation";
	private static final String SVC_PROXY_FIELD_NAME = "m_proxy";
	private static final String AUTH_TOKEN_FIELD_NAME = "m_authToken";
	private static final String AUTH_COOKIE_FIELD_NAME = "m_cookies";
	private static final String SVC_INSTANCE_NAME = "m_service";
	private static final String SVC_STATIC_NAME = "SVC_ADMIN_NAME";
	private static final String ENV_MAPPER = "s_envMapper";
	private static final String CLIENT_FIELD_NAME = "m_clientName";
	private static final String ENV_FIELD_NAME = "m_environment";
	private static final String USE_DEFAULT_CONFIG = "m_useDefaultClientConfig";

	// Added for setHostName() method
	private static final String HOST_NAME_FIELD_NAME = "m_hostName";
	private static final String URL_PATH_FIELD_NAME = "m_urlPath";
	private static final String PORT_FIELD_NAME = "m_port";
	private static final String PROTOCOL_SCHEME_FIELD_NAME = "m_protocolScheme";
	
	private static final String HTTP_NON_SECURE = "HTTP_NON_SECURE";
	private static final String SET_HOST_NAME = "setHostName";
	private static final String GET_HOST_NAME = "getHostName";
	private static final String SET_TARGET_LOCATION_COMPONENTS_NAME = "setTargetLocationComponents";	
	private static final String GET_LOCATION_FROM_COMPONENTS_NAME = "getLocationFromComponents";	
	private static final String IS_EMPTY_STRING_NAME = "isEmptyString";
	
	private static final String DEFAULT = "production";
	private static final String EXCEPTION_CLIENT_MSG = "clientName can not be null";
	private static final String ENV_MAPPER_GET_DEPLOYED_ENV = "getDeploymentEnvironment";
	private static final String oldConsJavadoc = "This constructor should be used, when a ClientConfig.xml is located in the "
		+ "\n\"client\" bundle, so that a ClassLoader of this Shared Consumer can be used.\n";
	private static final String newConsJavadoc = "This constructor should be used, when a ClientConfig.xml is located "
		+ "\nin some application bundle. Shared Consumer then will call ClassLoaderRegistry "
		+ "\nto register a ClassLoader of an application bundle.\n";


	private static Logger s_logger = LogManager.getInstance(ServiceConsumerGenerator.class);
	private boolean m_shouldUsePublicMethodsConsumer = false;

	private static ServiceConsumerGenerator s_serviceConsumerGenerator  = new ServiceConsumerGenerator();



	private ServiceConsumerGenerator() {}


	public static ServiceConsumerGenerator getInstance() {
		return s_serviceConsumerGenerator;
	}


	private Logger getLogger() {
		return s_logger;
	}

	public boolean continueOnError() {
		return true;
	}



	public void generate(CodeGenContext codeGenCtx) throws CodeGenFailedException {
		
		String qualifiedInterfaceName = "";
		Class<?> interfaceClass = null;
		m_shouldUsePublicMethodsConsumer = codeGenCtx.getInputOptions()
			.shouldUsePublicMethodsConsumer();
		if(codeGenCtx.isAsyncInterfaceRequired()){
			try {
				qualifiedInterfaceName  = getAsyncInterfaceClassName(codeGenCtx.getServiceInterfaceClassName());
				interfaceClass = ContextClassLoaderUtil.loadClass(qualifiedInterfaceName);
			} catch (ClassNotFoundException e) {
				// Load the base interface class. Async interface does not exists
				qualifiedInterfaceName = codeGenCtx.getServiceInterfaceClassName();
				interfaceClass = ContextClassLoaderUtil.loadRequiredClass(qualifiedInterfaceName);
			}
		}else{
			qualifiedInterfaceName = codeGenCtx.getServiceInterfaceClassName();
			interfaceClass = ContextClassLoaderUtil.loadRequiredClass(qualifiedInterfaceName);
		}
			
		//Class interfaceClass = loadClass(qualifiedInterfaceName);
		
		JCodeModel jCodeModel = new JCodeModel();

		String  testClientClassName =
				getServiceConsumerClassName(codeGenCtx.getServiceAdminName(), qualifiedInterfaceName,codeGenCtx);
		JDefinedClass testClientClass = createNewClass(jCodeModel, testClientClassName);
		
		if(codeGenCtx.getInputOptions().isConsumerAnInterfaceProjectArtifact())
			implement(testClientClass, interfaceClass);
        
		addFields(jCodeModel, interfaceClass, testClientClass,codeGenCtx);
		//shared consumer should not have default constructor
		if(! codeGenCtx.getInputOptions().isConsumerAnInterfaceProjectArtifact())
			addConstructor(jCodeModel, testClientClass);
		addConstructorwithClientName(jCodeModel, testClientClass,codeGenCtx);
		addConstructorWithTwoParams(jCodeModel, testClientClass,codeGenCtx);
		addConstructorWithThreeParams(jCodeModel, testClientClass,codeGenCtx);
		addConstructorWithFourParams(jCodeModel, testClientClass, codeGenCtx);
		//init() method added
		addInitMethod(testClientClass,jCodeModel);
		addSetSvcLocationMethod(testClientClass, jCodeModel, codeGenCtx);
		addSetUserProvidedSecurityCredentialsMethod(testClientClass, jCodeModel);
		addSetSecurtiyCredentials(testClientClass, jCodeModel, codeGenCtx);
		addSetCookiesMethod(testClientClass, jCodeModel, codeGenCtx);
		addGetServiceInvokerOptionsMethod(testClientClass,jCodeModel,codeGenCtx);
		addGetProxyMethod(testClientClass, interfaceClass, jCodeModel, codeGenCtx);
		addGetServiceMethod(testClientClass,jCodeModel,codeGenCtx);

		if(m_shouldUsePublicMethodsConsumer){
			addsetTargetLocationComponentsMethod(testClientClass, jCodeModel, codeGenCtx);
			addisEmptyStringMethod(testClientClass, jCodeModel, codeGenCtx);
			addGetSetHostNameMethod(testClientClass, jCodeModel, codeGenCtx);
			addgetLocationFromComponentsMethod(testClientClass, jCodeModel, codeGenCtx);
		}


		addServiceMethods(interfaceClass, testClientClass, jCodeModel);
		addJavaDocs(testClientClass);
		//adding comments for Consumer
		testClientClass.javadoc().append("\n"+"This class is not thread safe");
		
		generateJavaFile(codeGenCtx, testClientClass, "");

		if (codeGenCtx.getInputOptions().isNoCompile() == false) {
			compileJavaFilesNoException(
					codeGenCtx.getGeneratedJavaFiles(),
					codeGenCtx.getBinLocation());
		}

		getLogger().log(Level.INFO, "Successfully generated " + testClientClassName);
	}

	//--------------------------------------------------------------------
	//generates:
	//public void init() throws ServiceException 
	//{
	//getService();
	//}
	//--------------------------------------------------------------------
    private void addInitMethod(JDefinedClass testClientClass,JCodeModel codeModel)
    {
    	JMethod initMethod = addMethod(testClientClass, INIT_METHOD_NAME,JMod.PUBLIC,codeModel.VOID);
    	initMethod._throws(ServiceException.class);
    	JBlock initMethodBody = initMethod.body();
		JInvocation getServiceInvoker = JExpr.invoke(GET_SERVICE_METHOD_NAME);
		initMethodBody.add(getServiceInvoker);
		
		//Adding javadocs
		initMethod.javadoc().add("Use this method to initialize ConsumerApp after creating a Consumer instance");

    }
	
	private void addGetServiceMethod(JDefinedClass testClientClass, JCodeModel jCodeModel, CodeGenContext codeGenCtx) {
		
		//---------------------------------------------------------------
		// generates:
		// public Service getService()  throws ServiceException
		// --------------------------------------------------------------
		JClass svcJClass = getJClass(Service.class, jCodeModel);
		JMethod getServiceMethod =
				addMethod(testClientClass,
						GET_SERVICE_METHOD_NAME,
						JMod.PUBLIC,
						svcJClass);
		getServiceMethod._throws(ServiceException.class);
		
		//adding javadocs
		getServiceMethod.javadoc().add("Method returns an instance of Service which has been initilized for this Consumer");
		
		

		JBlock getServiceMethodBody = getServiceMethod.body();
		

		//---------------------------------------------------------------
		// generates:
		// if( m_service == null)
		//    m_service = ServiceFactory.create(SVC_ADMIN_NAME,  m_environment,m_clientName, m_serviceLocation);
		//---------------------------------------------------------------
		
		
		JClass svcFactoryJClass = getJClass(ServiceFactory.class, jCodeModel);
		JInvocation svcFactoryInvoker = svcFactoryJClass.staticInvoke("create");
		svcFactoryInvoker.arg(JExpr.ref(SVC_STATIC_NAME));
		svcFactoryInvoker.arg(JExpr.ref(ENV_FIELD_NAME));
		svcFactoryInvoker.arg(JExpr.ref(CLIENT_FIELD_NAME));
		if(m_shouldUsePublicMethodsConsumer)
			svcFactoryInvoker.arg(JExpr._null());
		else
			svcFactoryInvoker.arg(JExpr.ref(SVC_LOCATION_FIELD_NAME));
		svcFactoryInvoker.arg(JExpr.lit(false));
		svcFactoryInvoker.arg(JExpr.ref(USE_DEFAULT_CONFIG));
		

		JFieldRef svcInstanceRef = JExpr.ref(SVC_INSTANCE_NAME);
		JConditional ifServiceCondition = getServiceMethodBody._if(svcInstanceRef.eq(JExpr._null()));
		ifServiceCondition._then().assign(svcInstanceRef, svcFactoryInvoker);
		
	
		

		//------------------------------------------------------------
		// generates:
		//	setUserProvidedSecurityCredentials(m_service);
		//------------------------------------------------------------
		JInvocation setUserProvidedSecurityCredentialsInvoker = JExpr.invoke("setUserProvidedSecurityCredentials");
		setUserProvidedSecurityCredentialsInvoker.arg(svcInstanceRef);
		getServiceMethodBody.add(setUserProvidedSecurityCredentialsInvoker);


		//------------------------------------------------
		// generates:
		// return m_service;
		//------------------------------------------------
		getServiceMethodBody._return(svcInstanceRef);
		
		
	}


	private void addGetServiceInvokerOptionsMethod(JDefinedClass testClientClass, JCodeModel jCodeModel, CodeGenContext codeGenCtx) {
		
		//---------------------------------------------------------------
		// generates:
		// public  ServiceInvokerOptions getServiceInvokerOptions()
		// --------------------------------------------------------------
		
		JMethod getInvokerOptionsMethod =
				addMethod(testClientClass,
						GET_SVC_INVOKER_OPTIONS_METHOD_NAME,
						JMod.PUBLIC,
						getJClass(ServiceInvokerOptions.class, jCodeModel));
		getInvokerOptionsMethod._throws(ServiceException.class);
		getInvokerOptionsMethod.javadoc().add("Use this method to get the Invoker Options on the Service and set them to user-preferences");
		

		JBlock getInvokerOptionsMethodBody = getInvokerOptionsMethod.body();
		//---------------------------------------------------------------
		// generates:
		// m_service = getService();
		//---------------------------------------------------------------
		
		JFieldRef svcInstanceRef = JExpr.ref(SVC_INSTANCE_NAME);
		getInvokerOptionsMethodBody.assign(svcInstanceRef, JExpr.invoke(GET_SERVICE_METHOD_NAME));
		

		/*
		 * generates
		 *    return m_service.getInvokerOptions();
		 */
		JInvocation invokerOptionsCall = svcInstanceRef.invoke(GET_INVOKER_OPTIONS_METHOD_NAME);
		getInvokerOptionsMethodBody._return(invokerOptionsCall);
		
		
	}


	private String getServiceConsumerClassName(String svcAdminName, String interfaceClassName,CodeGenContext codegenCtx) {
		String svcConsumerClassName = null;
		InputOptions inputOptions = codegenCtx.getInputOptions();
		String response = null;
		
		if(inputOptions.isConsumerAnInterfaceProjectArtifact()){
			svcConsumerClassName = "Shared" + CodeGenUtil.makeFirstLetterUpper(svcAdminName) + "Consumer";
			
			if( !CodeGenUtil.isEmptyString(inputOptions.getShortPathForSharedConsumer())){
				String shortPkgPath = inputOptions.getShortPathForSharedConsumer();
				if(!shortPkgPath.endsWith("."))
					shortPkgPath += ".";
				response = shortPkgPath + svcConsumerClassName;
			}
		}
		else{
			svcConsumerClassName = "Base" + CodeGenUtil.makeFirstLetterUpper(svcAdminName) + "Consumer";
		}
		
		if(response == null)
			response = generateQualifiedClassName(interfaceClassName, "gen", svcConsumerClassName,codegenCtx);
		
		return response;
	}
	
	
	private String generateQualifiedClassName(String classNameForPkg,
			String pkgPrefix, String genClassName, CodeGenContext codegenCtx) {
		// The new structure would be package>/<serviceNmae/
		String genPkgName = null;

		int lastDotPos = classNameForPkg.lastIndexOf(DOT);
		if (lastDotPos > -1) {
			String destinationLoc = null;

			//for sharedConsumer,destination location includes AdminName.
			if(codegenCtx.getInputOptions().isConsumerAnInterfaceProjectArtifact())
				destinationLoc = codegenCtx.getInputOptions().getServiceAdminName().toLowerCase();
			genPkgName = destinationLoc == null ? classNameForPkg.substring(0,
					lastDotPos)
					+ DOT + pkgPrefix : classNameForPkg
					.substring(0, lastDotPos)
					+ DOT
					+ destinationLoc
					+ DOT
					+ pkgPrefix;
		} else {
			genPkgName = pkgPrefix;
		}

		return genPkgName.toLowerCase() + DOT + genClassName;
	}

	private void addFields(
				JCodeModel jCodeModel,
				Class<?> interfaceClass,
				JDefinedClass sharedConsumerClass,CodeGenContext codegenCtx) throws CodeGenFailedException {
		//--------------------------------------------------
		// adds private URL m_serviceLocation = null;
		//--------------------------------------------------
//		do not generate m_serviceLocation if SIPP version >= 1.2
		if(!m_shouldUsePublicMethodsConsumer){
			JClass urlJClass = getJClass("java.net.URL", jCodeModel);
			JFieldVar servicLocField =
					sharedConsumerClass.field(JMod.PRIVATE, urlJClass, SVC_LOCATION_FIELD_NAME);
			servicLocField.init(JExpr._null());
		}
		
		sharedConsumerClass.field(JMod.PRIVATE, JPrimitiveType.parse(jCodeModel, "boolean"), USE_DEFAULT_CONFIG);
		
		//--------------------------------------------------
		//adds private static final String SVC_ADMIN_NAME = <serviceName>;
		//--------------------------------------------------
		 int FIELD_MODS = (JMod.PRIVATE | JMod.STATIC | JMod.FINAL);
		JClass stringJClass = getJClass(String.class, jCodeModel);
		JFieldVar serviceAdminName = sharedConsumerClass.field(FIELD_MODS,stringJClass, SVC_STATIC_NAME);
		JExpression svcNameExpr = JExpr.lit(codegenCtx.getServiceAdminName());
		serviceAdminName.init(svcNameExpr);
		
		//--------------------------------------------------
		//adds private String m_clientName = <client name"; from -cn option for BaseConsumer, in the case of SharedConsumer it should always be null
		//--------------------------------------------------
		JFieldVar clientName = sharedConsumerClass.field(JMod.PRIVATE, stringJClass, CLIENT_FIELD_NAME);
		if(codegenCtx.getInputOptions().isConsumerAnInterfaceProjectArtifact())
			clientName.init(null);
		else{
			//for pre 2.4 consumer, override cientName with serviceName
			if(codegenCtx.getInputOptions().isServiceNameRequired())
				codegenCtx.getInputOptions().setClientName(codegenCtx.getServiceAdminName());
			JExpression clientNameExpr =  JExpr.lit(codegenCtx.getInputOptions().getClientName());
			clientName.init(clientNameExpr);
		}
			
	    
	    //-------------------------------------------------
	    //adds private String m_environment ="default"
	    //-------------------------------------------------
		JFieldVar environmentName = sharedConsumerClass.field(JMod.PRIVATE, stringJClass, ENV_FIELD_NAME);
	    JExpression environmentExp = JExpr.lit(DEFAULT);
	    if(codegenCtx.getInputOptions().isServiceNameRequired() && ! codegenCtx.getInputOptions().isConsumerAnInterfaceProjectArtifact())
	    	environmentName.init(null);
	    else
	    	environmentName.init(environmentExp);
	   
	    
	    


		//--------------------------------------------------
		// adds private <InterfaceClass> m_proxy = null;
		//--------------------------------------------------
		JClass interfaceJClass = getJClass(interfaceClass, jCodeModel);
		JFieldVar proxyField =
			sharedConsumerClass.field(JMod.PRIVATE, interfaceJClass, SVC_PROXY_FIELD_NAME);
		proxyField.init(JExpr._null());


		//--------------------------------------------------
		// adds private String m_authToken = null;
		//--------------------------------------------------
		JFieldVar authTokenField =
				sharedConsumerClass.field(JMod.PRIVATE, stringJClass, AUTH_TOKEN_FIELD_NAME);
		authTokenField.init(JExpr._null());


		//--------------------------------------------------
		// adds private Cookie[] m_cookies;
		//--------------------------------------------------
        JClass cookieClass = getJClass("org.ebayopensource.turmeric.runtime.common.types.Cookie",jCodeModel).array();
        sharedConsumerClass.field(JMod.PRIVATE,cookieClass,AUTH_COOKIE_FIELD_NAME);

        
        //--------------------------------------------------
		// adds private Service m_service = null;
		//--------------------------------------------------
		JFieldVar serviceInstanceField =
				sharedConsumerClass.field(JMod.PRIVATE, Service.class, SVC_INSTANCE_NAME);
		serviceInstanceField.init(JExpr._null());
		
		if (!CodeGenUtil.isEmptyString(codegenCtx.getInputOptions()
				.getEnvironmentMapper())) {
			// --------------------------------------------------
			// adds private String final EnvironmentMapper s_envMapper = new
			// org.ebayopensource.turmeric.MarketplaceEnvironmentMapperImpl();
			int FIELD_MODE = (JMod.PRIVATE | JMod.STATIC | JMod.FINAL);
			JClass envMapperClass = getJClass(EnvironmentMapper.class,
					jCodeModel);
			JFieldVar envmappername = sharedConsumerClass.field(FIELD_MODE,
					envMapperClass, ENV_MAPPER);
			JClass envmapperClass = getJClass(
					codegenCtx.getInputOptions()
					.getEnvironmentMapper(),
					jCodeModel);
			JInvocation envmapperObjCreater = JExpr._new(envmapperClass);
			envmappername.init(envmapperObjCreater);
		}
		//added for setHostName()
		//--------------------------------------------------
	    //		private final static String HTTP_NON_SECURE = "http";		
		//--------------------------------------------------
		if(m_shouldUsePublicMethodsConsumer){
			JFieldVar httpConstant = sharedConsumerClass.field(FIELD_MODS,stringJClass, HTTP_NON_SECURE);
			httpConstant.init(JExpr.lit("http"));
			sharedConsumerClass.field(JMod.PRIVATE,stringJClass,HOST_NAME_FIELD_NAME);
			sharedConsumerClass.field(JMod.PRIVATE,stringJClass,URL_PATH_FIELD_NAME);
			sharedConsumerClass.field(JMod.PRIVATE,int.class,PORT_FIELD_NAME);
			sharedConsumerClass.field(JMod.PRIVATE,stringJClass,PROTOCOL_SCHEME_FIELD_NAME);
		}
	}

	private void addConstructorJavaDoc(JMethod constructorWithArgs, String javaDocComment){

		JDocComment javaDocs = constructorWithArgs.javadoc();
		javaDocs.add(javaDocComment);
		JVar[] params = constructorWithArgs.listParams();
		for (JVar var : params) {
			javaDocs.add("\n@param " + var.name());
		}
		javaDocs.add("\n@throws ServiceException");
	}

	private void addConstructor(JCodeModel jCodeModel, JDefinedClass testClientClass)
		throws CodeGenFailedException {

		//---------------------------------------------------------------
		// generates:
		// public <Class Name>() {
		// }
		//---------------------------------------------------------------
		
		testClientClass.constructor(JMod.PUBLIC);
		
	}
	
	private void addConstructorwithClientName(JCodeModel jCodeModel,
			JDefinedClass testClientClass, CodeGenContext ctx)
			throws CodeGenFailedException {

		// ---------------------------------------------------------------
		// generates:
		// public BaseCalculatorServiceConsumer(String clientName)
		// throws ServiceException
		// {
		// 		this(clientName, s_envMapper.getDeploymentEnvironment());
		// }
		// ---------------------------------------------------------------
		JMethod constructorWithArgs = testClientClass.constructor(JMod.PUBLIC);
		JClass stringJClass = getJClass(String.class, jCodeModel);
		String paramClientName = "clientName";
		constructorWithArgs.param(stringJClass, paramClientName);
		constructorWithArgs._throws(ServiceException.class);
		JBlock constructorWithArgsBody = constructorWithArgs.body();
		
		JInvocation thisInvoker = constructorWithArgsBody.invoke("this");		
		thisInvoker.arg(JExpr.ref(paramClientName));
		
		JInvocation envMapperInvocation = null;

		if (!CodeGenUtil.isEmptyString(ctx.getInputOptions()
				.getEnvironmentMapper())) {
			envMapperInvocation = JExpr.ref(ENV_MAPPER).invoke(
					ENV_MAPPER_GET_DEPLOYED_ENV);
		}
		if(envMapperInvocation != null)
			thisInvoker.arg(envMapperInvocation);
		else
			thisInvoker.arg(JExpr._null());
		addConstructorJavaDoc(constructorWithArgs, oldConsJavadoc);
		
	}




	private void addConstructorWithTwoParams(JCodeModel jCodeModel,
			JDefinedClass testClientClass,CodeGenContext codegenCtx) {
		//---------------------------------------------------------------
		//generates:
		//public <Class Name>(String clientName, String environment)throws ServiceException{
		//if(environment ==null)
		//throw new ServiceException("environment can not be null");
		//m_clientName = clientName;
		//m_environment = environment;
		//}
		//---------------------------------------------------------------
		JMethod constructorWithArgs = testClientClass.constructor(JMod.PUBLIC);
		JClass stringJClass = getJClass(String.class,jCodeModel);
		String paramClientName = "clientName";
		constructorWithArgs.param(stringJClass, paramClientName);
		String paramEnvironment = "environment";
		constructorWithArgs.param(stringJClass, paramEnvironment);
		constructorWithArgs._throws(ServiceException.class);
		
		JBlock constructorWithArgsBody = constructorWithArgs.body();
		JInvocation thisInvoker = constructorWithArgsBody.invoke("this");		
		thisInvoker.arg(JExpr.ref(paramClientName));
		thisInvoker.arg(JExpr.ref(paramEnvironment));
		thisInvoker.arg(JExpr._null());
		thisInvoker.arg(JExpr.lit(false));
		addConstructorJavaDoc(constructorWithArgs, oldConsJavadoc);
		
		
	
	}

	private void addConstructorWithThreeParams(JCodeModel jCodeModel,
			JDefinedClass testClientClass,CodeGenContext codegenCtx) {
		//---------------------------------------------------------------
		//generates:
		//public <Class Name>(String clientName, String environment)throws ServiceException{
		//if(environment ==null)
		//throw new ServiceException("environment can not be null");
		//m_clientName = clientName;
		//m_environment = environment;
		//}
		//---------------------------------------------------------------
		JMethod constructorWithArgs = testClientClass.constructor(JMod.PUBLIC);
		JClass stringJClass = getJClass(String.class,jCodeModel);
		String paramClientName = "clientName";
		constructorWithArgs.param(stringJClass, paramClientName);
		String paramCaller = "caller";
		constructorWithArgs.param(Class.class, paramCaller);
		String paramUseDefaultClientConfig = "useDefaultClientConfig";
		constructorWithArgs.param(JPrimitiveType.parse(jCodeModel, "boolean"), paramUseDefaultClientConfig);
		constructorWithArgs._throws(ServiceException.class);
		
		JBlock constructorWithArgsBody = constructorWithArgs.body();
		JInvocation thisInvoker = constructorWithArgsBody.invoke("this");		
		thisInvoker.arg(JExpr.ref(paramClientName));

		JInvocation envMapperInvocation = null;
		if (!CodeGenUtil.isEmptyString(codegenCtx.getInputOptions()
				.getEnvironmentMapper())) {
			envMapperInvocation = JExpr.ref(ENV_MAPPER).invoke(
					ENV_MAPPER_GET_DEPLOYED_ENV);
		}
		if(envMapperInvocation != null)
			thisInvoker.arg(envMapperInvocation);
		else
			thisInvoker.arg(JExpr._null());
		
		thisInvoker.arg(JExpr.ref(paramCaller));
		thisInvoker.arg(JExpr.ref(paramUseDefaultClientConfig));
		addConstructorJavaDoc(constructorWithArgs, newConsJavadoc);
		
		
	
	}


	private void addConstructorWithFourParams(JCodeModel jCodeModel,
			JDefinedClass testClientClass,CodeGenContext codegenCtx) {
		//---------------------------------------------------------------
		//generates:
		//public <Class Name>(String clientName, String environment)throws ServiceException{
		//if(environment ==null)
		//throw new ServiceException("environment can not be null");
		//m_clientName = clientName;
		//m_environment = environment;
		//}
		//---------------------------------------------------------------
		JMethod constructorWithArgs = testClientClass.constructor(JMod.PUBLIC);
		JClass stringJClass = getJClass(String.class,jCodeModel);
		String paramClientName = "clientName";
		constructorWithArgs.param(stringJClass, paramClientName);
		String paramEnvironment = "environment";
		constructorWithArgs.param(stringJClass, paramEnvironment);
		String paramCaller = "caller";
		constructorWithArgs.param(Class.class, paramCaller);
		String paramUseDefaultClientConfig = "useDefaultClientConfig";
		constructorWithArgs.param(JPrimitiveType.parse(jCodeModel, "boolean"), paramUseDefaultClientConfig);
		constructorWithArgs._throws(ServiceException.class);
		
		JBlock constructorWithArgsBody = constructorWithArgs.body();
		JVar[] allLocalVariables = constructorWithArgs.listParams();
		JExpression isEmptyExpr = allLocalVariables[1].invoke("isEmpty");
		JClass ExceptionClass = getJClass(ServiceException.class, jCodeModel);
		
		
		JConditional ifResultCondition2 = constructorWithArgs.body()._if(allLocalVariables[0].eq(JExpr._null()));
		JInvocation invokeNewException2 = JExpr._new(ExceptionClass);
		invokeNewException2.arg(EXCEPTION_CLIENT_MSG);
		ifResultCondition2._then()._throw(invokeNewException2);
		
		constructorWithArgsBody.assign(
				JExpr.ref(CLIENT_FIELD_NAME),
				JExpr.ref(paramClientName)
				);

		if (!CodeGenUtil.isEmptyString(codegenCtx.getInputOptions()
				.getEnvironmentMapper())) {

			JConditional ifResultCondition = constructorWithArgsBody._if(allLocalVariables[1].eq(JExpr._null()).cor(isEmptyExpr));
			JInvocation invocation = JExpr.ref(ENV_MAPPER).invoke(ENV_MAPPER_GET_DEPLOYED_ENV);
			JBlock block =ifResultCondition._then(); 
			block.assign(allLocalVariables[1],
				invocation);
			constructorWithArgsBody.assign(JExpr.ref(ENV_FIELD_NAME), allLocalVariables[1]);
		} else{
			JConditional ifResultCondition = constructorWithArgsBody._if(allLocalVariables[1].ne(JExpr._null()));
			ifResultCondition._then().assign(JExpr.ref(ENV_FIELD_NAME), allLocalVariables[1]);			
		}
		
		constructorWithArgsBody.assign(JExpr.ref(USE_DEFAULT_CONFIG), allLocalVariables[3]);
		
		JClass classLoaderReg = getJClass(ClassLoaderRegistry.class, jCodeModel);
		
		JInvocation classloaderRegExpr = classLoaderReg.staticInvoke("instanceOf").invoke("registerServiceClient");
		
		classloaderRegExpr.arg(JExpr.ref(CLIENT_FIELD_NAME));
		classloaderRegExpr.arg(JExpr.ref(ENV_FIELD_NAME));
		classloaderRegExpr.arg(JExpr.ref(SVC_STATIC_NAME));
		classloaderRegExpr.arg(JExpr.direct(testClientClass.name() + ".class"));
		classloaderRegExpr.arg(allLocalVariables[2]);
		classloaderRegExpr.arg(JExpr.ref(USE_DEFAULT_CONFIG));
		constructorWithArgsBody.add(classloaderRegExpr);
		addConstructorJavaDoc(constructorWithArgs, newConsJavadoc);
		
		if(m_shouldUsePublicMethodsConsumer){
			String targetLocation = "targetLocation";
			JInvocation serviceLocationInvocation = JExpr.invoke(GET_SERVICE_METHOD_NAME).invoke(GET_SVC_LOC_METHOD_NAME);
			
			JVar targetLocField =
				constructorWithArgsBody.decl(getJClass(URL.class, jCodeModel), targetLocation, serviceLocationInvocation);
			constructorWithArgsBody.invoke(SET_TARGET_LOCATION_COMPONENTS_NAME).arg(targetLocField);
		}
	}




	private void addSetUserProvidedSecurityCredentialsMethod(
			JDefinedClass testClientClass,
			JCodeModel jCodeModel
			) throws CodeGenFailedException {

		//---------------------------------------------------------------
		// generates:
		//  private void  setUserProvidedSecurityCredentials(Service service) {
		//  ...
		//  ...
		// }
		//---------------------------------------------------------------

		JMethod  setUserProvidedSecurityCredentialsMethod =
				addMethod(testClientClass,
						  SET_USER_PROV_SEC_METHOD_NAME,
						  JMod.PRIVATE,
						  jCodeModel.VOID
						);
		JBlock setUserProvidedSecurityCredentialsMethodBody = setUserProvidedSecurityCredentialsMethod.body();

		JClass serviceClass = getJClass(Service.class,jCodeModel);
		setUserProvidedSecurityCredentialsMethod.param(serviceClass,"service");

		JFieldRef proxyFieldRef = JExpr.ref("service");



		//----------------------------------------------------------------------------
		// generates:
		//  if(m_authToken != null)
		//      service.setSessionTransportHeader(SOAHeaders.AUTH_TOKEN, m_authToken);
		//----------------------------------------------------------------------------
        JFieldRef authTokenFieldRef  = JExpr.ref(AUTH_TOKEN_FIELD_NAME);
        JConditional ifAuthTokenCheck = setUserProvidedSecurityCredentialsMethodBody._if(authTokenFieldRef.ne(JExpr._null()));

        JClass soaHeaderClass = getJClass(SOAHeaders.class,jCodeModel);
        JInvocation setSessionTransportHeaderInvokerForToken = proxyFieldRef.invoke("setSessionTransportHeader");
        setSessionTransportHeaderInvokerForToken.arg(soaHeaderClass.staticRef("AUTH_TOKEN"));
        setSessionTransportHeaderInvokerForToken.arg(authTokenFieldRef);
		JBlock ifAuthTokenBlock = ifAuthTokenCheck._then();
		ifAuthTokenBlock.add(setSessionTransportHeaderInvokerForToken);


		//----------------------------------------------------------------------------
		// generates:
		//  if(m_cookies != null) {
		//      for (int i = 0; (i<m_cookies.length); i ++)
		//          service.setCookie(m_cookies[i]);
		//----------------------------------------------------------------------------


		JFieldRef authCookieFieldRef  = JExpr.ref(AUTH_COOKIE_FIELD_NAME);
		JConditional ifCookiesCheck   = setUserProvidedSecurityCredentialsMethodBody._if(authCookieFieldRef.ne(JExpr._null()));

		JBlock ifCookiesBlock  = ifCookiesCheck._then();
		JForLoop cookieForLoop = ifCookiesBlock._for();

		JVar initVar = cookieForLoop.init(jCodeModel._ref(int.class), "i", JExpr.lit(0));
		cookieForLoop.test(initVar.lt(authCookieFieldRef.ref("length")));
		cookieForLoop.update(initVar.incr());

		JBlock forLoopBlock = cookieForLoop.body();
		JInvocation setCookieInvoker= proxyFieldRef.invoke("setCookie");
		setCookieInvoker.arg(authCookieFieldRef.component(initVar));
		forLoopBlock.add(setCookieInvoker);

	}

	private void addSetSecurtiyCredentials(
			JDefinedClass testClientClass,
			JCodeModel jCodeModel, CodeGenContext codeGenContext
			) throws CodeGenFailedException {

		//---------------------------------------------------------------
		// generates:
		//  protected void setSecurtiyCredentials(String authUserID,String authPassword, String authToken){
		//  ...
		//  ...
		// }
		//---------------------------------------------------------------

		JMethod  setSecurtiyCredentialsMethod = null;
		int modifier;
		if(m_shouldUsePublicMethodsConsumer)
			modifier = JMod.PUBLIC;
		else
			modifier = JMod.PROTECTED;
		
		setSecurtiyCredentialsMethod = addMethod(testClientClass,
						  SET_SEC_CRE_SEC_METHOD_NAME,
						  modifier,
						  jCodeModel.VOID
						);


		setSecurtiyCredentialsMethod.javadoc().add("Use this method to set User Credentials (Token) ");
		JBlock addSetSecurtiyCredentialsMethodBody = setSecurtiyCredentialsMethod.body();

		JClass stringJClass = getJClass(String.class,jCodeModel);
		String paramAuthToken = "authToken";
		setSecurtiyCredentialsMethod.param(stringJClass, paramAuthToken);
		//setSecurtiyCredentialsMethod.javadoc().addParam(paramAuthToken + "  Authentication Token");

		//---------------------------------------------------------------
		// generates:
	     //		 m_authToken = authToken;
		// }
		//---------------------------------------------------------------
		addSetSecurtiyCredentialsMethodBody.assign(
				JExpr.ref(AUTH_TOKEN_FIELD_NAME),
				JExpr.ref(paramAuthToken)
				);

		/**
		 * Adds getter method for Token field. This would be triggered only if the sipp_version >= 1.2
		 */
		if (m_shouldUsePublicMethodsConsumer) {
			JMethod getSecurtiyCredentialsMethod = addMethod(testClientClass,
					GET_SEC_CRE_SEC_METHOD_NAME, JMod.PUBLIC, stringJClass);
			getSecurtiyCredentialsMethod.javadoc().add(
					"Use this method to get User Credentials (Token) ");

			JBlock getCookiesMethodBody = getSecurtiyCredentialsMethod.body();
			getCookiesMethodBody._return(JExpr.ref(AUTH_TOKEN_FIELD_NAME));
		}

	}

	private void addSetSvcLocationMethod(
			JDefinedClass testClientClass,
			JCodeModel jCodeModel, CodeGenContext codegenCtx) throws CodeGenFailedException {

		//---------------------------------------------------------------
		// generates:
		// public void setServiceLocation(String serviceLocation)
		//          throws MalformedURLException {
		//  ...
		//  ...
		// }
		//---------------------------------------------------------------
		JMethod setSvcLocMethod = null;
		int modifier;
		if(m_shouldUsePublicMethodsConsumer)
			modifier = JMod.PUBLIC;
		else
			modifier = JMod.PROTECTED;
		String serviceLocation = "location";
		String serviceLocationUrl = "serviceLocationUrl";
		setSvcLocMethod = addMethod(testClientClass,
						SET_SVC_LOC_METHOD_NAME,
						modifier,
						jCodeModel.VOID);

		setSvcLocMethod._throws(MalformedURLException.class);
		JClass stringJClass = getJClass("java.lang.String", jCodeModel);
		JVar serviceLocParam = setSvcLocMethod.param(stringJClass, "serviceLocation");

		JClass urlJClass = getJClass("java.net.URL", jCodeModel);
		JInvocation urlClassObjCreater = JExpr._new(urlJClass);
		urlClassObjCreater.arg(serviceLocParam);

		//---------------------------------------------------------------
		// generates:
		// m_serviceLocation = new URL(serviceLocation);
		//---------------------------------------------------------------
		JBlock setSvcLocMethodBody = setSvcLocMethod.body();
		JFieldRef serviceLocFieldRef = null;
		if(m_shouldUsePublicMethodsConsumer){
			serviceLocFieldRef = JExpr.ref(serviceLocationUrl);
			setSvcLocMethodBody.decl(urlJClass, serviceLocationUrl, urlClassObjCreater);
			
			JInvocation setTargetLocationComponentsInvoker = JExpr.invoke(SET_TARGET_LOCATION_COMPONENTS_NAME);
			setTargetLocationComponentsInvoker.arg(serviceLocFieldRef);
			setSvcLocMethodBody.add(setTargetLocationComponentsInvoker);
			
		} else {
			serviceLocFieldRef = JExpr.ref(SVC_LOCATION_FIELD_NAME);
			setSvcLocMethodBody.assign(serviceLocFieldRef, urlClassObjCreater);
		}

		
		//---------------------------------------------------------------
		//generates:
		//if(m_service!=null)
		//m_service.setServiceLocation(m_serviceLocation);
		//---------------------------------------------------------------
		
		JFieldRef svcInstanceRef = JExpr.ref(SVC_INSTANCE_NAME);
		JConditional ifServiceLocCondition = setSvcLocMethodBody._if(svcInstanceRef.ne(JExpr._null()));
		JInvocation setServiceLocationInvok = svcInstanceRef.invoke(SET_SVC_LOC_METHOD_NAME);
		setServiceLocationInvok.arg(serviceLocFieldRef);
		ifServiceLocCondition._then().add(setServiceLocationInvok);
		


		/**
		 * Adds getter method for svcLocation field. This would be triggered only if the sipp_version >= 1.2
		 */
		if (m_shouldUsePublicMethodsConsumer) {
			JMethod getSvcLocMethod = addMethod(testClientClass,
					GET_SVC_LOC_METHOD_NAME, JMod.PUBLIC, urlJClass);
			
			getSvcLocMethod._throws(MalformedURLException.class);
			JBlock getSvcLocMethodBody = getSvcLocMethod.body();
			getSvcLocMethodBody.decl(stringJClass, serviceLocation, JExpr.invoke(GET_LOCATION_FROM_COMPONENTS_NAME));
			JInvocation getUrlClassObjCreater = JExpr._new(urlJClass).arg(JExpr.ref(serviceLocation));
			//---------------------------------------------------------------
			// generates:
			// m_serviceLocation = new URL(serviceLocation);
			//---------------------------------------------------------------

			JFieldRef getServiceLocFieldRef = JExpr.ref(serviceLocationUrl);
			getSvcLocMethodBody.decl(urlJClass, serviceLocationUrl, getUrlClassObjCreater);
			

			getSvcLocMethodBody._return(getServiceLocFieldRef);
		}

		
	}


	private void addGetProxyMethod(
			JDefinedClass testClientClass,
			Class<?> interfaceClass,
			JCodeModel jCodeModel,
			CodeGenContext codeGenCtx) throws CodeGenFailedException {

		//---------------------------------------------------------------
		// generates:
		// private <Interface> getProxy() {}
		// --------------------------------------------------------------
		JMethod getProxyMethod =
				addMethod(testClientClass,
						GET_PROXY_METHOD_NAME,
						JMod.PROTECTED,
						getJClass(interfaceClass, jCodeModel));
		getProxyMethod._throws(ServiceException.class);



		//---------------------------------------------------------------
		// generates:
		// m_service = getService();
		// m_proxy = m_service.getProxy();
		//---------------------------------------------------------------
		
		JBlock proxyMethodBody = getProxyMethod.body();
		
		
		JFieldRef svcInstanceRef = JExpr.ref(SVC_INSTANCE_NAME);
		JInvocation getServiceInvocation = JExpr.invoke(GET_SERVICE_METHOD_NAME);
		proxyMethodBody.assign(svcInstanceRef, getServiceInvocation);
		
		
		JFieldRef proxyFieldRef = JExpr.ref(SVC_PROXY_FIELD_NAME);
		proxyMethodBody.assign(proxyFieldRef, svcInstanceRef.invoke("getProxy"));
		

		//------------------------------------------------
		// generates:
		// return m_proxy;
		//------------------------------------------------
		proxyMethodBody._return(proxyFieldRef);
	}


	private void addSetCookiesMethod(
			JDefinedClass testClientClass,
			JCodeModel jCodeModel, CodeGenContext codegenCtx) throws CodeGenFailedException {

		//---------------------------------------------------------------
		// generates:
		// protected void setCookies(Cookie[] cookies){
		//  ...
		//  ...
		// }
		//---------------------------------------------------------------
		JMethod setCookiesMethod = null;
		int modifier;
		if(m_shouldUsePublicMethodsConsumer)
			modifier = JMod.PUBLIC;
		else
			modifier = JMod.PROTECTED;
		
		setCookiesMethod = addMethod(testClientClass,
			  			SET_COOKIES_METHOD_NAME,
			  			modifier,
			  			jCodeModel.VOID);

		setCookiesMethod.javadoc().add("Use this method to set User Credentials (Cookie)");

		JClass cookieArrayJClass = getJClass("org.ebayopensource.turmeric.runtime.common.types.Cookie", jCodeModel).array();
		JVar   cookieArrayParam = setCookiesMethod.param(cookieArrayJClass,"cookies");

		//---------------------------------------------------------------
		// generates:
		// m_cookies = cookies;
		//---------------------------------------------------------------
		JBlock setCookiesMethodBody = setCookiesMethod.body();
		JFieldRef cookieArrayFieldRef = JExpr.ref(AUTH_COOKIE_FIELD_NAME);
		setCookiesMethodBody.assign(cookieArrayFieldRef, cookieArrayParam);
		/**
		 * Adds getter method for cookies field. This would be triggered only if the sipp_version >= 1.2
		 */
		if (m_shouldUsePublicMethodsConsumer) {
			JMethod getCookiesMethod = addMethod(testClientClass,
					GET_COOKIES_METHOD_NAME, JMod.PUBLIC, cookieArrayJClass);
			getCookiesMethod.javadoc().add(
					"Use this method to get User Credentials (Cookie)");

			JBlock getCookiesMethodBody = getCookiesMethod.body();
			getCookiesMethodBody._return(cookieArrayFieldRef);
		}
		
	}


	private void addGetSetHostNameMethod(
			JDefinedClass testClientClass,
			JCodeModel jCodeModel, CodeGenContext codegenCtx) throws CodeGenFailedException {

		//---------------------------------------------------------------
		// generates:
		// protected void setHostName(String hostName, String protocolScheme)
		//		throws MalformedURLException{
		//  ...
		//  ...
		// }
		//---------------------------------------------------------------
		String hostNameJavadoc = "@param hostName	Actual hostname of the end point location, " +
		"\n\t\t\tCan contain :<port> as well";
		String protocolSchemeJavadoc = "\n@param protocolScheme	specifies the transport protocol scheme";

		String hostNameConstant = "hostName";
		
		JFieldRef hostNameFieldRef = JExpr.ref(hostNameConstant);		
		JClass stringJClass = getJClass(String.class, jCodeModel);
		
		JMethod setHostNameThreeParameterMethod = addMethod(testClientClass,
	  			SET_HOST_NAME, JMod.PUBLIC, jCodeModel.VOID);
		
		setHostNameThreeParameterMethod._throws(MalformedURLException.class);
		setHostNameThreeParameterMethod.javadoc().add(hostNameJavadoc);
		setHostNameThreeParameterMethod.javadoc().add(protocolSchemeJavadoc);
		JFieldRef mhostNameFieldRef = JExpr.ref(HOST_NAME_FIELD_NAME);
		setHostNameThreeParameterMethod.param(stringJClass, hostNameConstant);
		
		JBlock setHostNameMethodThreeParameterBody = setHostNameThreeParameterMethod.body();
		JExpression ternCondition = JOp.cond(hostNameFieldRef.ne(JExpr._null()), hostNameFieldRef, mhostNameFieldRef);
		setHostNameMethodThreeParameterBody.assign(mhostNameFieldRef, ternCondition);
		JVar newURLVar = setHostNameMethodThreeParameterBody.decl(getJClass(String.class, jCodeModel), 
				"newURL", JExpr.invoke(GET_LOCATION_FROM_COMPONENTS_NAME));
		JInvocation setServiceLocationInvoker = JExpr.invoke(SET_SVC_LOC_METHOD_NAME).arg(newURLVar);
		setHostNameMethodThreeParameterBody.add(setServiceLocationInvoker);
		
		
		/**
		 * Adds getter method for host field.
		 */
		JMethod getHostNameMethod = addMethod(testClientClass, GET_HOST_NAME,
				JMod.PUBLIC, stringJClass);
		getHostNameMethod.javadoc().add(
				"Returns the host name of the active end-point(from the servicelocation)");
		getHostNameMethod._throws(MalformedURLException.class);
		JInvocation getServiceLocationInvoker = JExpr.invoke(GET_SVC_LOC_METHOD_NAME);

		JBlock getHostNameMethodBody = getHostNameMethod.body();
		JVar resultVar = getHostNameMethodBody.decl(getJClass(URL.class, jCodeModel), 
				"targetLocation", getServiceLocationInvoker);
		
		JExpression getTernCondition = JOp.cond(resultVar.eq(JExpr._null()), JExpr._null(), resultVar.invoke("getHost"));
		getHostNameMethodBody._return(getTernCondition);
		
	}


	private void addsetTargetLocationComponentsMethod(
			JDefinedClass sharedConsumerClass,
			JCodeModel jCodeModel, CodeGenContext codegenCtx) throws CodeGenFailedException {

//		---------------------------------------------------------------
//		 generates:
//		 private void setTargetLocationComponents(URL targetLocation) {
//      
//       	if (targetLocation != null) {
//          	 m_protocolScheme = targetLocation.getProtocol();            
//            	 m_hostName = targetLocation.getHost();          
//            	 m_urlPath = targetLocation.getPath();           
//            	 m_port = targetLocation.getPort();
//        	} 
//
//      	if(isEmptyString(m_protocolScheme))
//          	 m_protocolScheme = HTTP_NON_SECURE;
//      	if(isEmptyString(m_hostName))
//          	 m_hostName = "localhost";
//     		if(isEmptyString(m_urlPath))
//       	     m_urlPath = "/";
//      	if(m_port < 0)
//       	     m_port = 0;
//
//    		}
//		 }
//		---------------------------------------------------------------
		JMethod setTargetLocationComponentsMethod = addMethod(sharedConsumerClass,
			  			SET_TARGET_LOCATION_COMPONENTS_NAME,
			  			JMod.PRIVATE,
			  			jCodeModel.VOID);

		String targetLocationConstant = "targetLocation";
		
		JFieldRef targetLocationFieldRef = JExpr.ref(targetLocationConstant);
		
		JClass urlClass = getJClass(URL.class, jCodeModel);
		setTargetLocationComponentsMethod.param(urlClass, targetLocationConstant);

		JBlock setTargetLocationComponentsMethodBody = setTargetLocationComponentsMethod.body();
		
		JFieldRef protocolRef = JExpr.ref(PROTOCOL_SCHEME_FIELD_NAME);
		JFieldRef hostNameRef = JExpr.ref(HOST_NAME_FIELD_NAME);
		JFieldRef urlPathRef = JExpr.ref(URL_PATH_FIELD_NAME);
		JFieldRef portRef = JExpr.ref(PORT_FIELD_NAME);
		
		JConditional targetLocationCheck = setTargetLocationComponentsMethodBody._if(targetLocationFieldRef.ne(JExpr._null()));
		JBlock ifThenBlock =  targetLocationCheck._then();
		ifThenBlock.assign(protocolRef, JExpr.ref(targetLocationConstant).invoke("getProtocol"));
		ifThenBlock.assign(hostNameRef, JExpr.ref(targetLocationConstant).invoke("getHost"));
		ifThenBlock.assign(urlPathRef, JExpr.ref(targetLocationConstant).invoke("getPath"));
		ifThenBlock.assign(portRef, JExpr.ref(targetLocationConstant).invoke("getPort"));
		
		
		JConditional protocolCheck = setTargetLocationComponentsMethodBody._if(JExpr.invoke(IS_EMPTY_STRING_NAME).arg(protocolRef));
		JBlock protocolCheckBlock =  protocolCheck._then();
		protocolCheckBlock.assign(protocolRef, JExpr.ref(HTTP_NON_SECURE));
		
		JConditional hostNameCheck = setTargetLocationComponentsMethodBody._if(JExpr.invoke(IS_EMPTY_STRING_NAME).arg(hostNameRef));
		JBlock hostNameCheckBlock =  hostNameCheck._then();
		hostNameCheckBlock.assign(hostNameRef, JExpr.lit("localhost"));
		
		JConditional urlPathCheck = setTargetLocationComponentsMethodBody._if(JExpr.invoke(IS_EMPTY_STRING_NAME).arg(urlPathRef));
		JBlock urlPathCheckBlock =  urlPathCheck._then();
		urlPathCheckBlock.assign(urlPathRef, JExpr.lit(""));
		
		JConditional portCheck = setTargetLocationComponentsMethodBody._if(portRef.lt(JExpr.lit(0)));
		JBlock portCheckBlock =  portCheck._then();
		portCheckBlock.assign(portRef, JExpr.lit(0));
		
	}
	

	private void addisEmptyStringMethod(
			JDefinedClass sharedConsumerClass,
			JCodeModel jCodeModel, CodeGenContext codegenCtx) throws CodeGenFailedException {

//		---------------------------------------------------------------
//		 generates:
//    		private boolean isEmptyString(String givenString) {
//          	  return (givenString == null || givenString.trim().length() == 0);
//    		}
//		---------------------------------------------------------------
		JMethod isEmptyStringMethod = addMethod(sharedConsumerClass,
			  			IS_EMPTY_STRING_NAME,
			  			JMod.PRIVATE | JMod.STATIC ,
			  			jCodeModel.BOOLEAN);

		String givenStringConstant = "givenString";		
		JFieldRef givenStringFieldRef = JExpr.ref(givenStringConstant);
		
		JClass stringClass = getJClass(String.class, jCodeModel);
		isEmptyStringMethod.param(stringClass, givenStringConstant);

		JBlock isEmptyStringMethodBody = isEmptyStringMethod.body();
		
		JExpression exp1 = givenStringFieldRef.eq(JExpr._null());
		JExpression exp2 = givenStringFieldRef.invoke("trim").invoke("length").eq(JExpr.lit(0));
		isEmptyStringMethodBody._return(exp1.cor(exp2));
		
	}

	private void addgetLocationFromComponentsMethod(
			JDefinedClass sharedConsumerClass,
			JCodeModel jCodeModel, CodeGenContext codegenCtx) throws CodeGenFailedException {

//		---------------------------------------------------------------
//		 generates:
//    		private boolean isEmptyString(String givenString) {
//          	  return (givenString == null || givenString.trim().length() == 0);
//    		}
//		---------------------------------------------------------------
		JClass stringClass = getJClass(String.class, jCodeModel);
		JMethod isEmptyStringMethod = addMethod(sharedConsumerClass,
			  			GET_LOCATION_FROM_COMPONENTS_NAME,
			  			JMod.PRIVATE ,
			  			stringClass);

		String locationConstant = "location";		
		JFieldRef locationFieldRef = JExpr.ref(locationConstant);
		
		JBlock isEmptyStringMethodBody = isEmptyStringMethod.body();
		
		String derivedLocation = PROTOCOL_SCHEME_FIELD_NAME + " + \"://\" + " + HOST_NAME_FIELD_NAME + 
				" + ((" + PORT_FIELD_NAME + "> 0)? (\":\" + " + PORT_FIELD_NAME + ") : \"\" ) + " 
				+ URL_PATH_FIELD_NAME;
		
		isEmptyStringMethodBody.decl(stringClass, locationConstant, JExpr.direct(derivedLocation));
		isEmptyStringMethodBody._return(locationFieldRef);
		
	}

	private void addServiceMethods(
			Class<?> interfaceClass,
			JDefinedClass testClientClass,
			JCodeModel jCodeModel) throws CodeGenFailedException {

		List<Method> methods = IntrospectUtil.getMethods(interfaceClass);

		for (Method method : methods) {
			String methodName = method.getName();

			//---------------------------------------------------------------
			// For each interface method
			// generates:
			// public <ReturnType> test<InterfaceMethodName>() {
			// }
			//---------------------------------------------------------------
			JType returnJType = null;
			Type returnType = method.getGenericReturnType();
			if (returnType != null) {
				returnJType = getJType(returnType, jCodeModel);
			} else {
				returnJType = jCodeModel.VOID;
			}


			JMethod testMethod = addMethod(testClientClass, methodName, returnJType);

			Type[] paramTypes = method.getGenericParameterTypes();
			if (paramTypes != null && paramTypes.length > 0) {
				for(int i=0; i<paramTypes.length; i++){
				addParameter(testClientClass, testMethod, paramTypes[i], "param"+i);
				}
			}

			Type[] exceptionTypes = method.getGenericExceptionTypes();
			for (Type exceptionType : exceptionTypes) {
				testMethod._throws(getTypeClass(exceptionType));
					}

			JBlock testMethodBody = testMethod.body();
			//---------------------------------------------------------------
			// generates:
			// <ReturnType> result = <InitialValue>
			//---------------------------------------------------------------
			JVar resultVar = null;
			if (returnJType != null && returnJType != jCodeModel.VOID) {
				resultVar = testMethodBody.decl(returnJType, "result", getValueForType(returnJType));
			}

			//---------------------------------------------------------------
			// generates:
			// try {
			//      m_proxy = getProxy();
			//---------------------------------------------------------------
			JTryBlock tryBlock = testMethodBody._try();
			JBlock tryBlockBody = tryBlock.body();

			JFieldRef proxyFieldRef     = JExpr.ref(SVC_PROXY_FIELD_NAME);
			JInvocation getProxyInvoker = JExpr.invoke(GET_PROXY_METHOD_NAME);
			tryBlockBody.assign(proxyFieldRef, getProxyInvoker);



			//---------------------------------------------------------------
			// generates:
			// catch (ServiceException serviceException) {
            //     throw ServiceRuntimeException.wrap(serviceException);
            // }
			//---------------------------------------------------------------
			JCatchBlock jCatchBlock =
					tryBlock._catch(getJClass("org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException", jCodeModel));
			jCatchBlock.param("serviceException");
			JBlock catchBlockBody = jCatchBlock.body();

			JClass ServiceRuntimeException = getJClass(ServiceRuntimeException.class,jCodeModel);
			JInvocation serviceExceptionWrapInvoker = ServiceRuntimeException.staticInvoke("wrap");
			serviceExceptionWrapInvoker.arg(JExpr.ref("serviceException"));

			catchBlockBody._throw(serviceExceptionWrapInvoker);

     		//---------------------------------------------------------------
			// generates:
			// result = m_proxy.<InterfaceMethod>([<ParameterValue>]);
			//---------------------------------------------------------------

			JInvocation methodInvoker = proxyFieldRef.invoke(methodName);
			// Size should be atmost 1
			// we don't allow more than one parameter
			// for Service interface methods
			for(int i =0; i< paramTypes.length; i++){
				if (paramTypes != null && paramTypes.length > 0) {
					methodInvoker.arg(JExpr.ref("param"+i));
				}
			}

			if (returnJType != null && returnJType != jCodeModel.VOID) {
				testMethodBody.assign(resultVar, methodInvoker);
			} else {
				testMethodBody.add(methodInvoker);
			}



			if (resultVar != null) {
				testMethodBody._return(resultVar);
			} else {
				testMethodBody._return();
			}

		}

	}


	public String getFilePath(String serviceAdminName, String interfaceName) {
		return null;
	}

	/*
	private void addMainMethod(
			JDefinedClass testClientClass,
			JCodeModel jCodeModel) throws CodeGenFailedException {

		//---------------------------------------------------------------
		// generates:
		// public static void main(String[] args) {
		//  ...
		//  ...
		// }
		//---------------------------------------------------------------
		JMethod mainMethod =
				addMethod(testClientClass,
						"main",
						(JMod.PUBLIC | JMod.STATIC),
						jCodeModel.VOID);

		JClass stringJClass = getJClass("java.lang.String", jCodeModel);
		JClass strJClassArray = stringJClass.array();
		JVar argsArraysVar = mainMethod.param(strJClassArray, "args");

		mainMethod._throws(Exception.class);

		JBlock mainMethodBody = mainMethod.body();

		//---------------------------------------------------------------
		// <TestClientClassName> testClient = new <TestClientClassName>();
		//---------------------------------------------------------------
		JInvocation testClientInvoker = JExpr._new(testClientClass);
		JVar testClientVar = mainMethodBody.decl(testClientClass, "testClient", testClientInvoker);

		//---------------------------------------------------------------
		// if (args.length > 0) {
		//     testClient.setServiceLocation(args[0]);
		// }
		//---------------------------------------------------------------
		JConditional jCondition = mainMethodBody._if(argsArraysVar.ref("length").gt(JExpr.lit(0)));
		JBlock ifThenBlock = jCondition._then();
		JInvocation setSvcLocInvoker = testClientVar.invoke(SET_SVC_LOC_METHOD_NAME);
		setSvcLocInvoker.arg(JExpr.component(argsArraysVar, JExpr.lit(0)));
		ifThenBlock.add(setSvcLocInvoker);

		mainMethodBody.directStatement("// Invoke methods on 'testClient' for testing!!");

	}
	*/

}
