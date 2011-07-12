package org.ebayopensource.turmeric.runtime.spf.pipeline;

public class DefaultHttpErrorMapper implements HttpErrorMapper {

	@Override
	public HttpError getHttpError(Throwable e) {
		return new HttpError(500,"Interanl Service Error");
	}
}
