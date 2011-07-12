package org.ebayopensource.turmeric.runtime.spf.impl.transport.http;

public enum HTTPSupportedVerbs {
	GET, POST, PUT, DELETE;
	
	public static String getAllValues(){
		return "GET,POST,PUT,DELETE";
	}
}
