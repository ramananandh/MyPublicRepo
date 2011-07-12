package org.ebayopensource.turmeric.runtime.common.binding;

import java.io.InputStream;

import org.ebayopensource.turmeric.runtime.binding.IDeserializationContext;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;

public interface IProtobufDeserializer extends Deserializer {

	/**
	 * 
	 * Deserialize the specified payload 
	 * in the InputStream.
	 * 
	 * @param type
	 *            the runtime type of the object to be 
	 *            deserialized
	 * @param inputStream
	 *            the input stream of bytes
	 * @param context
	 *            Deserialization context
	 * @return the deserialized Object
	 * @throws ServiceException
	 * 
	 */
	public Object deserialize(IDeserializationContext ctxt, 
			                  Class<?> type, 
			                  InputStream inputStream) 
	throws  ServiceException;
}
