package org.ebayopensource.turmeric.runtime.common.binding;

import java.io.OutputStream;

import org.ebayopensource.turmeric.runtime.binding.ISerializationContext;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;

public interface IProtobufSerializer extends Serializer {

	/**
	 * It takes a java content tree, serialize it 
	 * into an encoded data and output it to the given 
	 * output stream. It throws ServiceException
	 * when there is an error during the process.
	 * 
	 * @param ctx
	 *            the serialization context
	 * @param in
	 *            the object to serialize
	 * @param type
	 *            the type of the object
	 * @param out
	 *            the stream to which the passed in object 
	 *            has to be serialized
	 * @throws ServiceException
	 */
	public void serialize(ISerializationContext ctx, 
			              Object in, 
			              Class<?> type,
			              OutputStream out) 
	throws ServiceException;
}
