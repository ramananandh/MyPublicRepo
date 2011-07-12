package org.ebayopensource.turmeric.runtime.common.registration;

//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.runtime.sif.impl.internal.config.ClientConfigHolder;
import org.ebayopensource.turmeric.runtime.sif.impl.internal.config.ClientConfigManager;

//import org.osgi.framework.BundleContext;

/**
 * The one which holds all the registry entries meaning the client configs and
 * service metadata.
 * 
 * @author agrinenko
 * 
 */
public class ClassLoaderRegistry {

	private static final Logger s_logger = LogManager.getInstance(ClassLoaderRegistry.class);

	private static ClassLoaderRegistry REGISTRY = new ClassLoaderRegistry();
	private static Hashtable<String, ClassLoader> RESOURCES_CLASS_LOADERS = new Hashtable<String, ClassLoader>();
	private static Hashtable<String, ClassLoader> CLASSES_CLASS_LOADERS = new Hashtable<String, ClassLoader>();

//	private BufferedWriter out;

	public void writeToOut(String str) {
//		try {
//			out.write(str); out.flush();
//		} catch (Exception e){
//			e.printStackTrace();
//		}
        if (s_logger.isLoggable(Level.INFO)) {
        	s_logger.log(Level.INFO, str);
        }
	}

	private ClassLoaderRegistry() {
	}

	public static ClassLoaderRegistry instanceOf() {
//		if (REGISTRY == null) {
//			synchronized (ClassLoaderRegistry.class) {
//				if (REGISTRY == null) {
//					REGISTRY = new ClassLoaderRegistry();
////					try {
////						REGISTRY.out = new BufferedWriter(new FileWriter(
////								"ClassLoaderRegistry"+System.currentTimeMillis()));
////					} catch (IOException e) {e.printStackTrace();}
//				}
//			}
//		}
		return REGISTRY;
	}

	public ClassLoader getClassLoaderForClass(String fullClassName) {
		return CLASSES_CLASS_LOADERS.get(fullClassName);
	}

	public ClassLoader getClassLoaderForFile(String filePath) {
		return RESOURCES_CLASS_LOADERS.get(filePath);
	}

	/**
	 * A registration of file, to which a relative path should be provided, like
	 * META-INF/soa/services/config/WebUtilityService/SecurityPolicy.xml
	 * Most of the files are supposed to be registered automatically by using 
	 * a method registerServiceClient 
	 * 
	 * @param resourceFilePath
	 * @param classLoader
	 */
	public void registerResource(String resourceFilePath, ClassLoader classLoader)
	{
        if (!RESOURCES_CLASS_LOADERS.containsKey(resourceFilePath)) {
			RESOURCES_CLASS_LOADERS.put(resourceFilePath, classLoader);
		}
	}

	/**
	 * A registration of a java package.
	 * Most of the packages are supposed to be registered automatically by using 
	 * a method registerServiceClient. 
	 * 
	 * @param packageName
	 * @param classLoader
	 */
	public void registerPackage (String packageName, ClassLoader classLoader)
	{
        if (!CLASSES_CLASS_LOADERS.containsKey(packageName)) {
			CLASSES_CLASS_LOADERS.put(packageName, classLoader);
		}
	}

	/**
	 * A simplified registration of a java package, when the full class name is available.
	 * Most of the packages are supposed to be registered automatically by using 
	 * a method registerServiceClient. 
	 * 
	 * @param fullClassName
	 * @param classLoader
	 */
	public void registerPackageForClass (String fullClassName, ClassLoader classLoader)
	{
		String packageName = getPackageName(fullClassName);
		if (!CLASSES_CLASS_LOADERS.containsKey(packageName)) {
			CLASSES_CLASS_LOADERS.put(packageName, classLoader);
		}
	}

	public int getCount() {
		return CLASSES_CLASS_LOADERS.size() + RESOURCES_CLASS_LOADERS.size();
	}

	public void unregisterClassLoader(ClassLoader classLoader) throws ServiceException 
	{
	}

	/**
	 * Method for the backward compartibility with CodeGen Shared Service classes,
	 * which are using a version of this method without a "useDefaultClientConfig" parameter.
	 */
	public void registerServiceClient (String clientName, String environment, 
			String serviceAdminName, Class<?> serviceClass, Class<?> clientClass) 
					throws ServiceException
	{
		registerServiceClient(clientName, environment, serviceAdminName, serviceClass, clientClass, false);
	}

	/**
	 * This is a main method for an automatic registration of resources (classes and files) 
	 * of a SOA Service bundle. Than, SOA Runtime would be able to use them.
	 * The registration assumes, that all request/response classes, interface files
	 * (like TypeMappings.xml and service_metadata.properties) are located either 
	 * inside an interface("shared") bundle or in the attached to it (as a fragment)
	 * - client bundle. ClassLoaderRegistry will try to use a ClassLoaderof a provided
	 * "serviceClass" to load all those resources. A file ClientConfig.xml can be loacted 
	 * in another bundle, but then one a special constructors of a Shared Service class 
	 * should be called, then one which has a "clientClass" parameter. ClassLoaderRegistry
	 * will try to use a ClassLoader of that ClassLoader to load ClientConfig.xml  
	 * 
	 * If a file path to the ClientConfig.xml of the service looks like
	 * META-INF/soa/client/config/SomeServiceClient/staging/SomeService/ClientConfig.xml
	 * , then those parameter should be passed: 
	 * 		clientName="SomeServiceClient", 
	 * 		environment="staging"
	 *		serviceAdminName="SomeService".
	 * If the path to ClientConfig.xml is 
	 * META-INF/soa/client/config/SomeService/ClientConfig.xml
	 * , then empty or null value of environment variable should be passed. 
	 * 
	 * Additional resource can be registered as well, by using direct calls to
	 * methods registerResource, registerPackage and registerPackageForClass.  
	 * 
	 * @param clientName
	 * @param environment
	 * @param serviceAdminName
	 * @param serviceClass
	 * @param clientClass
	 * @param useDefaultClientConfig 
	 * @throws ServiceException
	 */
	public void registerServiceClient (String clientName, String environment, 
			String serviceAdminName, Class<?> serviceClass, Class<?> clientClass, boolean useDefaultClientConfig) 
					throws ServiceException
	{
		ClassLoader serviceClassLoader = serviceClass.getClassLoader();
		ClassLoader clientClassLoader = null;
		if (clientClass != null) {
			clientClassLoader = clientClass.getClassLoader();
		} else {
			clientClassLoader = serviceClassLoader;
		}

		// Register resources that are from the caller bundle
		if (environment != null && environment.trim().length() > 0) 
		{
			registerResource("META-INF/soa/client/config/" + clientName + "/" + environment + "/"  
					+ serviceAdminName + "/ClientConfig.xml",
					clientClassLoader);
		} else {
			registerResource("META-INF/soa/client/config/" + clientName + "/ClientConfig.xml",
					clientClassLoader);
		}

		// Register resources that are from service interface bundle
    	registerResource("META-INF/soa/common/config/" + serviceAdminName + "/service_metadata.properties",
				serviceClassLoader);
		registerResource("META-INF/soa/common/config/" + serviceAdminName + "/TypeMappings.xml",
				serviceClassLoader);
		registerResource("META-INF/soa/services/wsdl/" + serviceAdminName + "/" + serviceAdminName + ".wsdl", serviceClassLoader);


		ClientConfigHolder config = null;
		if (environment != null && environment.trim().length() > 0) 
		{
			config = ClientConfigManager.getInstance()
					.readClientConfigMap(serviceAdminName, clientName, environment, false, false, useDefaultClientConfig)
					.get(serviceAdminName);
		} else {
			config = ClientConfigManager.getInstance()
					.readClientConfigMap(serviceAdminName, clientName, null, false, false, useDefaultClientConfig)
					.get(serviceAdminName);
		}
		
		for(String className : config.getTypeMappings().getJavaTypes()) {
			registerPackageForClass(className, serviceClassLoader);
		}
		
		String interfaceClass = config.getServiceInterfaceClassName();
		registerPackageForClass(interfaceClass, serviceClassLoader);
		registerPackage(getPackageName(interfaceClass) + ".gen", serviceClassLoader);
    }

	public final static String DEFAULT_PACKAGE = "."; //$NON-NLS-1$

	public final static String getResourcePackageName(String name) {
		if (name != null) {
			/* check for leading slash*/
			int begin = ((name.length() > 1) && (name.charAt(0) == '/' || name.charAt(0) == '\\')) ? 1 : 0;
			int end = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')); /* index of last slash */
			if (end > begin)
				return name.substring(begin, end).replace('/', '.').replace('\\', '.');
		}
		return DEFAULT_PACKAGE;
	}

	public final static String getPackageName(String name) {
		if (name != null) {
			int index = name.lastIndexOf('.'); /* find last period in class name */
			if (index > 0)
				return name.substring(0, index);
		}
		return DEFAULT_PACKAGE;
	}

}
