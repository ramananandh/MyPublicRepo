/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.exception;

/**
 * @author rkulandaivel
 *
 */
public class ProtobufModelGenerationFailedException extends Exception {
	private static final long serialVersionUID = 1L;


	/**
	 * @param message
	 */
	public ProtobufModelGenerationFailedException(String message) {
		super(message);
	}


	/**
	 * @param message
	 * @param cause
	 */
	public ProtobufModelGenerationFailedException(String message,
			Throwable cause) {
		super(message, cause);
	}

}
