/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.runtime.spf.pipeline;

import java.io.IOException;
import java.nio.charset.Charset;

import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.HTTPCommonUtils;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
import org.ebayopensource.turmeric.runtime.errorlibrary.ErrorConstants;
import org.ebayopensource.turmeric.runtime.spf.impl.internal.service.ServerServiceDesc;

/**
 * Implements the ?wsdl pseudo-operation.  This class is shared across threads and does not contain any state information.
 * @author rmurphy
 */
public class QueryWsdl implements PseudoOperation {
	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.runtime.spf.pipeline.PseudoOperation#preinvoke(org.ebayopensource.turmeric.runtime.spf.impl.internal.service.ServerServiceDesc, org.ebayopensource.turmeric.runtime.spf.pipeline.RequestMetaContext, org.ebayopensource.turmeric.runtime.spf.pipeline.ResponseMetaContext)
	 */
	@Override
	public void preinvoke(ServerServiceDesc serviceDesc, RequestMetaContext reqMetaCtx, ResponseMetaContext respMetaCtx) throws ServiceException {
		String contentType = HTTPCommonUtils.formatContentType(SOAConstants.MIME_XML, Charset.defaultCharset());
		respMetaCtx.setContentType(contentType);
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.runtime.spf.pipeline.PseudoOperation#invoke(org.ebayopensource.turmeric.runtime.spf.impl.internal.service.ServerServiceDesc, org.ebayopensource.turmeric.runtime.spf.pipeline.RequestMetaContext, org.ebayopensource.turmeric.runtime.spf.pipeline.ResponseMetaContext)
	 */
	@Override
	public void invoke(ServerServiceDesc serviceDesc, RequestMetaContext reqMetaCtx, ResponseMetaContext respMetaCtx) throws ServiceException {
		String adminName = serviceDesc.getAdminName();
		String wsdlFileName = "META-INF/soa/services/wsdl/" + adminName + "/" + adminName + "_public.wsdl";

		try {
			if (!PseudoOperationUtil.streamResource(wsdlFileName, respMetaCtx.getOutputStream(), serviceDesc.getClassLoader())) {
				wsdlFileName = "META-INF/soa/services/wsdl/" + adminName + "/" + adminName + ".wsdl";
				if(!PseudoOperationUtil.streamResource(wsdlFileName, respMetaCtx.getOutputStream(), serviceDesc.getClassLoader())) {
					throw new ServiceException(ErrorDataFactory.createErrorData(
							ErrorConstants.SVC_RT_NO_WSDL, ErrorConstants.ERRORDOMAIN, new Object[] { adminName }));
				}
			}
		} catch (IOException e) {
			throw new ServiceException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_TRANSPORT_OUTBOUND_IO_EXCEPTION,
					ErrorConstants.ERRORDOMAIN, new Object[] { adminName, e.toString() }), e);
		}

	}
}
