package org.ebayopensource.turmeric.runtime.spf.pipeline;


public interface HttpErrorMapper {
	public HttpError getHttpError(Throwable e);
}
