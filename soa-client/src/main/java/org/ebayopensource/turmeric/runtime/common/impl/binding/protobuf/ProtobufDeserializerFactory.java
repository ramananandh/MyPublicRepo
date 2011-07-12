package org.ebayopensource.turmeric.runtime.common.impl.binding.protobuf;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.ebayopensource.turmeric.runtime.binding.IDeserializationContext;
import org.ebayopensource.turmeric.runtime.binding.IDeserializerFactory.InitContext;
import org.ebayopensource.turmeric.runtime.binding.objectnode.ObjectNode;
import org.ebayopensource.turmeric.runtime.common.binding.DeserializerFactory;
import org.ebayopensource.turmeric.runtime.common.binding.IProtobufDeserializer;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.pipeline.InboundMessage;

public class ProtobufDeserializerFactory implements DeserializerFactory {

	private final static String payloadType = "PROTOBUF";
	private final static String uoexMsg = "Protobuf Deserializer Factory may not return XML Stream Reader";
	
	@Override
	public void init(InitContext ctx) throws ServiceException {
		// Nothing to initialize for now.
	}

	@Override
	public IProtobufDeserializer getDeserializer() {
		return new ProtobufDeserializer();
	}

	@Override
	public XMLStreamReader getXMLStreamReader(InboundMessage msg,
			ObjectNode objNode) throws ServiceException {
		throw new UnsupportedOperationException(uoexMsg);
	}

	@Override
	public XMLStreamReader getXMLStreamReader(InboundMessage msg,
			List<Class> paramTypes, InputStream in) throws ServiceException {
		throw new UnsupportedOperationException(uoexMsg);
	}

	@Override
	public String getPayloadType() {	
		return payloadType;
	}

	@Override
	public Map<String, String> getOptions() {
		return Collections.emptyMap();
	}
	
	public static class ProtobufDeserializer implements IProtobufDeserializer {

		private static final String uoexStr = "ProtobufDeserializer does not impolement this method";
		
		@Override
		public Object deserialize(InboundMessage msg, Class<?> clazz)
				throws ServiceException {
			throw new UnsupportedOperationException(uoexStr);
		}

		@Override
		public Object deserialize(InboundMessage msg, Class<?> clazz,
				XMLStreamReader reader) throws ServiceException {
			throw new UnsupportedOperationException(uoexStr);
		}

		@Override
		public Class<?> getBoundType() {			
			throw new UnsupportedOperationException(uoexStr);
		}

		@Override
		public Object deserialize(IDeserializationContext ctx, Class<?> type,
				InputStream inputStream) throws ServiceException {
			try {
				String extendedClassName = ProtobufUtil.getExtendedClassName(type);
				Class<?> eClass = Class.forName(extendedClassName);
				Method parseFrom = eClass.getMethod("parseFrom", InputStream.class);
				return parseFrom.invoke(null, inputStream);				
			} catch (Exception e) {				
				throw new ServiceException("Unable to deserialize the protobuf stream", e);
			}				
		}
	}
}
