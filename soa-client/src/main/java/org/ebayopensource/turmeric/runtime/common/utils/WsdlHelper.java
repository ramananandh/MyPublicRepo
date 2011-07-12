package org.ebayopensource.turmeric.runtime.common.utils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.xml.sax.InputSource;

public class WsdlHelper {

	/**
	 * Returns the service location path specified in the WSDL For ex., if the
	 * URI is "http://svcs.ebay.com:9090/ServiceName/v1", the method wll return
	 * "/ServiceName/v1"
	 * 
	 * @throws ServiceException
	 */
	public static String getServiceLocationPathInfo(String adminName) throws ServiceException {

		String wsdlPath = getWsdlPath(adminName);
		Definition wsdl = getWsdlDefinition(wsdlPath);

		String serviceLocation = null;
		if (wsdl != null && wsdl.getServices().isEmpty() == false) {
			for (Object obj : ((javax.wsdl.Service) (wsdl.getServices().values().toArray()[0])).getPorts().values()) {
				javax.wsdl.Port port = (javax.wsdl.Port) obj;
				if (port.getExtensibilityElements().size() > 0) {
					Object elem = port.getExtensibilityElements().get(0);
					if (elem instanceof javax.wsdl.extensions.http.HTTPAddress) {
						serviceLocation = ((javax.wsdl.extensions.http.HTTPAddress) elem).getLocationURI();
						break;
					} else if (elem instanceof javax.wsdl.extensions.soap.SOAPAddress) {
						serviceLocation = ((javax.wsdl.extensions.soap.SOAPAddress) elem).getLocationURI();
						break;
					} else {
						try {
							final Method method = elem.getClass().getMethod("getLocationURI");
							final Object result = method.invoke(elem);
							if (result != null) {
								serviceLocation = result.toString();
								// although we have found the service location, but we would still prefer the http and soap
								// addresses, thus we will not break from the loop.
							}
						} catch (Exception e) {
							throw new ServiceException("getLocationURI reflective call throw exception", e);
						}
					}
				}
			}
		}
		if (serviceLocation == null) {
			throw new ServiceException("Unable to get service location from wsdl for " + adminName + " in " + wsdlPath);
		}
		URL serviceLocalUrl;
		try {
			serviceLocalUrl = new URL(serviceLocation);
		} catch (MalformedURLException e) {
			throw new ServiceException("Not a valid URL", e);
		}
		return serviceLocalUrl.getPath();
	}

	public static Definition getWsdlDefinition(String wsdlPath) throws ServiceException {

		try {
			WSDLFactory factory = WSDLFactory.newInstance();
			WSDLReader reader = factory.newWSDLReader();

			// Consult with Alex for getting the resource as stream.
			InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(wsdlPath);

			if (inputStream == null) {
				String msg = "InputStream could not be created for WSDL @ " + wsdlPath;
				throw new ServiceException(msg);
			}

			InputSource wsdlInputSource = new InputSource(inputStream);
			return reader.readWSDL(null, wsdlInputSource);

		} catch (WSDLException e) {
			String errMsg = "Exception while trying to create WSDL Definition : " + e.getMessage();
			throw new ServiceException(errMsg,e);
		}

	}

	private static String getWsdlPath(String adminName) {
		return "META-INF/soa/services/wsdl/" + adminName + "/" + adminName + ".wsdl";
	}

}
