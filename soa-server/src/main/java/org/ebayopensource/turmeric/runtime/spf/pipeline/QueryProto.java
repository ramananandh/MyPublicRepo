package org.ebayopensource.turmeric.runtime.spf.pipeline;

import java.io.IOException;
import java.nio.charset.Charset;

import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.HTTPCommonUtils;
import org.ebayopensource.turmeric.runtime.errorlibrary.ErrorConstants;
import org.ebayopensource.turmeric.runtime.spf.impl.internal.service.ServerServiceDesc;

public class QueryProto implements PseudoOperation {

	private static final String CONTENT_TYPE = "text/plain";

	/**
	 * Streams the Proto file to the Client
	 * 
	 * @throws 
	 * 		ServiceException, if the service does not support Protocol Buffers. 
	 */
	@Override
	public void invoke(ServerServiceDesc serviceDesc, RequestMetaContext reqMetaCtx, 
			ResponseMetaContext respMetaCtx) throws ServiceException {
		
		String adminName = serviceDesc.getAdminName();
		String resName = "META-INF/soa/services/proto/" + adminName + "/" + adminName + ".proto";
		try {
			if (!PseudoOperationUtil.streamResource(resName, respMetaCtx.getOutputStream(), serviceDesc.getClassLoader())) {
				throw new ServiceException(ErrorDataFactory.createErrorData(
						ErrorConstants.SVC_RT_NO_PROTO, 
						ErrorConstants.ERRORDOMAIN, new Object[] { adminName }));
			}
		} catch (IOException e) {
			throw new ServiceException(ErrorDataFactory.createErrorData(
					ErrorConstants.SVC_TRANSPORT_OUTBOUND_IO_EXCEPTION,
					ErrorConstants.ERRORDOMAIN, new Object[] { adminName, e.toString() }), e);
		}
	}

	@Override
	public void preinvoke(ServerServiceDesc serviceDesc, RequestMetaContext reqMetaCtx, ResponseMetaContext respMetaCtx)
	        throws ServiceException {
		String contentType = HTTPCommonUtils.formatContentType(CONTENT_TYPE, Charset.defaultCharset());
		respMetaCtx.setContentType(contentType);
	}

}
