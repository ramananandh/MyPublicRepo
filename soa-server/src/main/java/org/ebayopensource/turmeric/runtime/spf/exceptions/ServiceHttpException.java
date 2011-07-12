package org.ebayopensource.turmeric.runtime.spf.exceptions;

import java.util.List;

import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;

/**
 * This Exception is thrown from the Service Implementation if it needs to be
 * propagated as HTTP error code when using SOA Rest invocation.
 * 
 * @author cpenkar
 */
public class ServiceHttpException extends ServiceRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceHttpException(CommonErrorData errorData, Throwable cause) {
		super(errorData, cause);
	}

	public ServiceHttpException(CommonErrorData errorData) {
		super(errorData);
	}

	public ServiceHttpException(List<CommonErrorData> errorData, Throwable cause) {
		super(errorData, cause);
	}

	public ServiceHttpException(List<CommonErrorData> errorData) {
		super(errorData);
	}

	public ServiceHttpException(ErrorMessage errorMessage,
			String defMessage, Throwable cause) {
		super(errorMessage, defMessage, cause);
	}

	public ServiceHttpException(ErrorMessage errorMessage,
			Throwable cause) {
		super(errorMessage, cause);
	}

}
