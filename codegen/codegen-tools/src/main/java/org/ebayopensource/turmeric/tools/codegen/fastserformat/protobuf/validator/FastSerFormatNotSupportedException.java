/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator;

import java.util.List;

import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatValidationError;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * This exception is thrown by FastSerFormatValidationHandler.
 * This is thrown only if the wsdl or xsd uses violates any validation rules.
 * The exception has details about the line number, file name etc.
 * 
 * @author rkulandaivel
 *
 */
public class FastSerFormatNotSupportedException extends CodeGenFailedException {

	private static final long serialVersionUID = 1L;

	private List<FastSerFormatValidationError> errors = null;
	private String message = null;
	/**
	 * @param msg
	 * @param cause
	 */
	@SuppressWarnings("unchecked")
	public FastSerFormatNotSupportedException(String msg, List<FastSerFormatValidationError> errors) {
		super(msg);
		this.errors = Collections.unmodifiableList( errors );
	}

	public List<FastSerFormatValidationError> getErrors() {
		return errors;
	}


	@Override
	public String getMessage() {
		if( message == null ){
			String msg = super.getMessage();
			msg = msg + "," + formatMessage();
			message = msg;
		}
		return message;
	}

	private String formatMessage(){
		StringBuilder buffer = new StringBuilder();
		int count = errors.size();

		for(FastSerFormatValidationError error : errors ){
			buffer.append("Error:"+ error.getError().value());
			buffer.append(", ");
			buffer.append("Line No.:"+ error.getLineNumber());
			buffer.append(", ");
			buffer.append("Column No.:"+ error.getColumnNumber());
			buffer.append(", ");
			buffer.append("Description:"+ error.getDescription());
			buffer.append(", ");
			buffer.append("File Name:"+ error.getFileName());

			if( --count > 0 ){
				buffer.append(";");
			}
		}
		
		return buffer.toString();
	}
}
