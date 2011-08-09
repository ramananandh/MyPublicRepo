package org.ebayopensource.turmeric.runtime.tests.common.sif.error;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.axis2.transport.http.HTTPConstants;

public class BaseErrorResponseTest {
	// HTTP prefix
	protected static final byte[] HTTP_10 = "HTTP/1.0 ".getBytes();
	protected static final byte[] HTTP_11 = "HTTP/1.1 ".getBytes();

	// HTTP status codes
	protected static final byte[] OK = "200 OK".getBytes();

	protected static final byte[] UNAUTH = "401 Unauthorized".getBytes();

	protected static final byte[] ISE = "500 Internal Server Error".getBytes();

	protected static final byte[] SU = "503 Service Unavailable".getBytes();

	protected static final byte[] FILE_NOT_FOUND = "404 Blah".getBytes();
	protected static final byte[] FILE_NOT_FOUND_MSG = ("<html><body>File not found</body></html>")
			.getBytes();

	// Standard MIME headers for XML payload
	protected static final byte[] XML_MIME_STUFF = ("\r\nContent-Type: text/xml; charset=utf-8")
			.getBytes();

	// Standard MIME headers for HTML payload
	protected static final byte[] HTML_MIME_STUFF = ("\r\nContent-Type: text/html; charset=utf-8")
			.getBytes();
	protected static final byte[] JNLP_MIME_STUFF = ("\r\nContent-Type: application/x-java-jnlp-file")
			.getBytes();
	protected static final byte[] JAR_MIME_STUFF = ("\r\nContent-Type: application/java-archive")
			.getBytes();

	protected static final String CONTENT_TYPE = "\r\n"
			+ HTTPConstants.HEADER_CONTENT_TYPE + ": ";

	protected static final byte[] CONTENT_LENGTH = ("\r\n"
			+ HTTPConstants.HEADER_CONTENT_LENGTH + ": ").getBytes();

	protected static final byte[] CONNECTION_CLOSE = "\r\nConnection: close"
			.getBytes();

	protected static final byte[] TRANSFER_ENCODING_CHUNKED = "\r\nTransfer-Encoding: chunked"
			.getBytes();

	protected static final byte[] SERVER_FAULT = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>A server error occured while processing the request.</faultstring></soap:Fault></soap:Body></soap:Envelope>"
			.getBytes();

	// Mime/Content separator
	protected static final byte[] SEPARATOR = "\r\n\r\n".getBytes();

	// ASCII character mapping to lower case
	protected static final byte[] toLower = new byte[256];

	static {
		for (int i = 0; i < 256; i++) {
			toLower[i] = (byte) i;
		}

		for (int lc = 'a'; lc <= 'z'; lc++) {
			toLower[(lc + 'A') - 'a'] = (byte) lc;
		}
	}

	// mime header for content length
	protected static final byte[] LENGTH_HEADER = "content-length: ".getBytes();

	// mime header for content type
	protected static final byte[] CONTENT_TYPE_HEADER = "content-type: "
			.getBytes();

	// mime header for content location
	protected static final byte[] CONTENT_LOCATION_HEADER = "content-location: "
			.getBytes();

	// mime header for soap action
	protected static final byte[] ACTION_HEADER = "soapaction: ".getBytes();

	// mime header for connection
	protected static final byte[] CONNECTION_HEADER = "connection: ".getBytes();

	// mime header for GET
	protected static final byte[] GET_HEADER = "GET".getBytes();

	// mime header for HEAD
	protected static final byte[] HEAD_HEADER = "HEAD".getBytes();

	// mime header for POST
	protected static final byte[] POST_HEADER = "POST".getBytes();

	// header ender
	protected static final byte[] HEADER_ENDER = ": ".getBytes();

	// transfer-encoding type
	protected static final byte[] ENCODING_HEADER = "transfer-encoding: "
			.getBytes();

	// transfer-encoding type chunked
	protected static final byte[] CHUNKED = "chunked".getBytes();

	// connection closed
	protected static final byte[] CLOSE = "close".getBytes();

	protected static final byte[] HTTP_BASE_VERSION = "http/".getBytes();

	protected static void writeResponse(OutputStream out, byte[] status,
			byte[] contentType, byte[] message) {
		try {
			if (message == null) {
				out.write(createHeaderReply(status, contentType, 0));
			} else {
				out
						.write(createHeaderReply(status, contentType,
								message.length));
				out.write(message);
			}
			out.flush();
		} catch (IOException ee) {

		}
	}

	protected static byte[] createHeaderReply(byte[] code, byte[] contentType,
			long length) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(HTTP_11);
		out.write(code);
		if (contentType != null) {
			out.write(contentType);
		}
		if (length < 0) {
			out.write(TRANSFER_ENCODING_CHUNKED);
		} else {
			out.write(CONTENT_LENGTH);
			out.write(String.valueOf(length).getBytes());
		}
		out.write(CONNECTION_CLOSE);

		out.write(SEPARATOR);
		return out.toByteArray();
	}


}