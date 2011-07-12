/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.dotproto;

import java.util.List;

import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufEnumEntry;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufEnumMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufField;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufMessage;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufOption;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufOptionType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model.ProtobufSchema;

/**
 * @author rkulandaivel
 *
 */
public class ProtobufSchemaWriter {

	//Key words Constants
	public static final String PACKAGE = "package";
	public static final String END_LINE = ";";
	public static final String OPTION = "option";
	public static final String EQUAL_SIGN = "=";
	public static final String MESSAGE = "message";
	public static final String ENUM = "enum";
	public static final String START_BLOCK = "{";
	public static final String END_BLOCK = "}";

	private ProtobufSchema m_schema = null;
	public ProtobufSchemaWriter( ProtobufSchema schema ){
		m_schema = schema;
	}

	private ProtobufSchema getSchema(){
		return m_schema;
	}

	/**
	 * Writes the contents of the file.
	 * Creates the file with following structure.
	 * 
	 * <Package>
	 * 
	 * <Compiler Options for JAVA>
	 * 
	 * <Messages>
	 * 
	 * @param formatter
	 */
	public void write( DotProtoFormatter formatter ){

		//write package
		writePackage( formatter, getSchema() );

		//Leave a line before options
		formatter.newLine();

		//Write all java options
		writeOptions( formatter );

		//Leave a line for messages
		formatter.newLine();

		//Write all messages
		for(ProtobufMessage message : m_schema.getMessages() ){
			writeMessage( formatter, message );
			formatter.newLine();
		}

		//write the metadata information
		formatter.newLine();
		formatter.print( new String( m_schema.getMetadataBytes() ) );
	}

	/**
	 * Writes the package.
	 * Writes as 
	 * package <Package value>;
	 * 
	 * @param formatter
	 * @param schema
	 */
	private void writePackage( DotProtoFormatter formatter , ProtobufSchema schema ){
		formatter.print( PACKAGE );
		formatter.print( schema.getDotprotoFilePackage() );
		formatter.print( END_LINE );
	}

	/**
	 * Writes all the java options.
	 * The format is 
	 * option <option_type> = "value";
	 * 
	 * Only for option type "optimize_for", the double quotes would not be generated.
	 *  
	 * @param formatter
	 */
	private void writeOptions( DotProtoFormatter formatter ){
		List<ProtobufOption> options = m_schema.getDotprotoOptions();
		for( ProtobufOption option : options ){

			//new line for each option
			formatter.newLine();

			//print key word
			formatter.print( OPTION );
			
			//print option type
			formatter.print( option.getOptionType().value() );
			
			//equal sign
			formatter.print( EQUAL_SIGN );

			//value
			//Only for optimize_for option surrounding single quotes is not necessary 
			if( option.getOptionType() == ProtobufOptionType.OPTIMIZE_FOR ){
				formatter.print( option.getOptionValue());

			}else{
				formatter.print( "\""+option.getOptionValue() +"\"");	
			}
			
			//semi colan
			formatter.print( END_LINE );
		}
	}

	/**
	 * Writes the message.
	 * The structure is
	 * 
	 * 			//Comment line  (one or more lines)
	 * 			message <message_name>{
	 * 
	 * 				//field comment (one or more line)
	 * 				<modifier> <fieldtype> <fieldname> = <sequencenumber>; (one or more fields)
	 * 			}
	 *  
	 * @param formatter
	 * @param message
	 */
	private void writeMessage( DotProtoFormatter formatter , ProtobufMessage message){

		//new line brefore starting new message
		formatter.newLine();

		//print comments of message
		String comments = message.getMessageComments();
		formatter.printComment(comments);
		
		//new line before starting message
		formatter.newLine();
		formatter.print( MESSAGE );

		//enum type handled separately
		if( !message.isEnumType() ){
			//message name
			formatter.print( message.getMessageName() );
			formatter.print( START_BLOCK );

			for( ProtobufField field : message.getFields() ){
				writeField( formatter, field );
			}
		}else{
			ProtobufEnumMessage enumMessage = (ProtobufEnumMessage)message ;
			formatter.print( enumMessage.getEnumMessageName() );
			formatter.print( START_BLOCK );
			writeEnumMessage( formatter, (ProtobufEnumMessage)message );
		}
		
		formatter.newLine();
		formatter.print( END_BLOCK );
	}
	
	/**
	 * Writes the enum block.
	 * The method indents one level deeper for each message.
	 * The structure is
	 *  
	 *  enum <enum_name>{
	 *   <enum_value> = <seq_no>; (one or more entries)
	 *  }
	 *  
	 * @param formatter
	 * @param enumMessage
	 */
	public void writeEnumMessage( DotProtoFormatter formatter , ProtobufEnumMessage enumMessage){
		formatter.newLine();

		formatter.indent();

		formatter.print( ENUM );

		formatter.print( enumMessage.getMessageName() );

		formatter.print( START_BLOCK );
		
		for( ProtobufEnumEntry entry : enumMessage.getEnumEntries() ){
			writeEnumEntry( formatter, entry );
		}
		
		formatter.newLine();
		formatter.print( END_BLOCK );
		
		formatter.outdent();
	}

	/**
	 * Writes the enum entry.
	 * The method indents one level deep for each entry.
	 * 
	 * The structure is
	 *  
	 * <fieldname> = <sequencenumber>;
	 * 
	 * @param formatter
	 * @param field
	 */
	private void writeEnumEntry( DotProtoFormatter formatter , ProtobufEnumEntry field){
		//Create new line
		formatter.newLine();

		//Increase Indentation
		formatter.indent();

		//print the comments
		String comments = field.getFieldComments();
		formatter.printComment(comments);

		//Create new line
		formatter.newLine();

		//Print the field name
		formatter.print( field.getEnumValue() );

		//Print the equals '=' sign
		formatter.print( EQUAL_SIGN );

		//Print the sequence number
		formatter.print( field.getSequenceNumber() +"");

		//Print the semi colan
		formatter.print( END_LINE );
		
		//Decrease Indentation
		formatter.outdent();
	}
	
	/**
	 * Writes the field.
	 * The method indents one level deep for each field.
	 * Structure is
	 * 
	 * <modifier> <fieldtype> <fieldname> = <sequencenumber>;
	 * 
	 * @param formatter
	 * @param field
	 */
	private void writeField( DotProtoFormatter formatter , ProtobufField field){
		//Create new line
		formatter.newLine();

		//Increase Indentation
		formatter.indent();

		//print the comments
		String comments = field.getFieldComments();
		formatter.printComment(comments);

		//Create new line
		formatter.newLine();

		//Print the modifier
		formatter.print( field.getFieldModifier().value() );

		//Print the type name
		formatter.print( field.getProtobufTypeName() );

		//Print the field name
		formatter.print( field.getConvertedFieldName() );

		//Print the equals '=' sign
		formatter.print( EQUAL_SIGN );

		//Print the sequence number
		formatter.print( field.getSequenceTagNumber() +"");

		//Print the semi colan
		formatter.print( END_LINE );
		
		//Decrease Indentation
		formatter.outdent();
	}
}
