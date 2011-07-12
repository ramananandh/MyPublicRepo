package org.ebayopensource.turmeric.runtime.spf.pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;

public final class PseudoOperationUtil {

	public static boolean streamResource(String resourceName, OutputStream outputStream, ClassLoader classLoader)
            throws IOException, ServiceException {
    	InputStream input = classLoader.getResourceAsStream(resourceName);
    	if (input == null) {
    		input = classLoader.getResourceAsStream(resourceName);
    		if (input == null) {
    			return false;
    		}
    	}
    	byte[] buf = new byte[8192];
    	int numRead = 0;
    	while ((numRead = input.read(buf)) != -1) {
    		outputStream.write(buf, 0, numRead);
    	}
    	return true;
    }

}
