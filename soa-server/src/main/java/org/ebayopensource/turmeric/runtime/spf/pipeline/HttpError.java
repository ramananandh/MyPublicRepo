package org.ebayopensource.turmeric.runtime.spf.pipeline;

public class HttpError{

	private static final long serialVersionUID = 1L;
	
	private final int code;
	private final String message;
	
	public HttpError(int code, String message){
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}


	@Override
	public String toString() {
		return String.format("Code=%d,Message=%s", new Integer(code),
				message);
	}	
}
