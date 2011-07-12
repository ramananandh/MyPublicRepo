package org.ebayopensource.turmeric.runtime.common.impl.binding.protobuf;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.ebayopensource.turmeric.runtime.binding.ISerializationContext;
import org.ebayopensource.turmeric.runtime.binding.ISerializer;
import org.ebayopensource.turmeric.runtime.common.binding.IProtobufSerializer;
import org.ebayopensource.turmeric.runtime.common.binding.SerializerFactory;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.common.pipeline.OutboundMessage;

public class ProtobufSerializerFactory implements SerializerFactory {

	private final static String payloadType = "PROTOBUF";
	private final static String uoexMsg = "Protobuf Serializer Factory may not return XML Stream Reader";
	

    @Override
    public IProtobufSerializer getSerializer() {
        return new ProtobufSerializer();
    }

    @Override
    public XMLStreamWriter getXMLStreamWriter(OutboundMessage msg, List<Class> paramTypes, OutputStream out)
            throws ServiceException {
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

    public static class ProtobufSerializer implements IProtobufSerializer {

    	private static final String uoexStr = "Protobuf Serializer does not impolement this method";
    	
        @Override
        public Class<?> getBoundType() {
        	throw new UnsupportedOperationException(uoexStr);
        }

        @Override
        public void serialize(OutboundMessage msg, Object in, QName xmlName, Class<?> clazz, XMLStreamWriter out)
                throws ServiceException {
        	throw new UnsupportedOperationException(uoexStr);
        }

        @Override
        public void serialize(ISerializationContext ctx, Object in, Class<?> type, OutputStream out) throws ServiceException {
            try { 
            	// Admin Name is needed to get the class name for Proto object
				if (!(ctx instanceof MessageContext)) {
					throw new ServiceException("Expected an instance of MessageContext. But, got " + ctx.getClass());
				}
				MessageContext mctx = (MessageContext) ctx;
				String adminName = mctx.getAdminName();
				String eName = ProtobufUtil.getExtendedClassName(type);
				Class<?> pClass = Class.forName(eName);
				Method newInstance = pClass.getMethod("newInstance", in.getClass());
				Object obj = newInstance.invoke(null, in);
				String protoClassName = ProtobufUtil.getProtoClassName(type, adminName);
				Class<?> protoClass = Class.forName(protoClassName);
				Method writeTo = protoClass.getMethod("writeTo", OutputStream.class);
				writeTo.invoke(obj, out);
			}
            catch (Exception e) {
            	throw new ServiceException("Unable to serialize to protobuf stream", e);
            }
        }

       
    }

	@Override
	public void init(org.ebayopensource.turmeric.runtime.binding.ISerializerFactory.InitContext ctx)
			throws ServiceException {
		// Nothing to initialize for now
		
	}
}
