/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.dotproto;

import java.io.PrintWriter;

/**
 * @author rkulandaivel
 *
 * This is the formatter class for formatting the contents of dot proto file.
 * This class takes care of indentation, space.
 * On print event, this class directly writes to the underlying print writer.
 * 
 */
public class DotProtoFormatter {

	private static final char SPACE = ' ';
	private static final String STRING_SPACE = String.valueOf( SPACE );
    private static final String NEW_LINE = String.valueOf( '\n' );
    private static final String RETURN = String.valueOf( '\r' );
    private static final String RETURN_AND_NEW_LINE = "\r\n";
    private static final int LINE_LENGTH_LIMIT = 80;

    /**
     * Current number of indentation strings to print
     */
    private int indentLevel;

    /**
     * String to be used for each indentation.
     * Defaults to four spaces.
     */
    private final String indentSpace;

    private boolean atBeginningOfLine = true;

    /**
     * Stream associated with this JFormatter
     */
    private final PrintWriter pw;

    /**
     * Creates a DotProtoFormatter.
     *
     * @param s
     *        PrintWriter to DotProtoFormatter to use.
     *
     * @param space
     *        Incremental indentation string, similar to tab value.
     */
    public DotProtoFormatter(PrintWriter s, String space) {
        pw = s;
        indentSpace = space;
    }

    public DotProtoFormatter(PrintWriter s) {
        this(s, "    ");
    }
    
    /**
     * Closes this formatter.
     */
    public void close() {
    	pw.flush();
        pw.close();
    }
    
    void write(ProtobufSchemaWriter writer) {
    	writer.write(this);
    }

    /**
     * Increase the indent level.
     */
    void indent(){
    	indentLevel++;
    }

    /**
     * decrease the indent level.
     */
    void outdent(){
    	indentLevel--;
    }

    /**
     * Provide the space.
     * If the control is in beginning of line, provide indentation based on indent level.
     * else provide space.
     */
    private void space() {
        if (atBeginningOfLine) {
            for (int i = 0; i < indentLevel; i++)
                pw.print(indentSpace);
            atBeginningOfLine = false;
        } else {
            pw.print( SPACE );
        }
    }

    /**
     * Print a new line into the stream.
     * After creating the new line, initiates the flag, atBeginningOfLine
     * 
     */
    public DotProtoFormatter newLine() {
        pw.println();
        atBeginningOfLine = true;
        return this;
    }

    /**
     * Print a String into the stream
     * Provide the space and then print the steam.
     * To provide space, method space is used.
     *
     * @param s the String
     */
    public DotProtoFormatter print(String s) {
        space(  );
        pw.print(s);
        return this;
    }

    private void printCommentLine(StringBuilder line) {
		newLine();
		print( "//" );
		print( line.toString() );
    }

    /**
     * Does not prints the comments as it is.
     * It removes unwanted spaces at beginning of comment, between words, and at end of comment.
     * It prints only 80 chars for a line.
     * 
     * @param s
     * @return
     */
    public DotProtoFormatter printComment(String s) {
    	if( s == null || "".equals(s) ){
    		return this;
    	}

    	s = trimComments( s );

		if( "".equals( s ) ){
			return this;
		}

    	String[] words = s.split( STRING_SPACE );

    	//consider initial space left for indentaion
    	//in addition add 2 for '//'
    	int indentSpace = (indentLevel*4) + 2;
    	printComment( words, indentSpace );

    	
        return this;
    }

    /**
     * Prints the comments by 80 chars a line.
     * For each line it adds a character '//' at beginning
     * 
     * @param words
     * @param indentSpace
     */
    private void printComment( String[] words, int indentSpace){
    	StringBuilder line = new StringBuilder();
    	for( int i = 0; i< words.length; i++ ){
    		String word = words[ i ];

    		int sizeLater = line.length() + word.length() + indentSpace;
    		if( sizeLater > LINE_LENGTH_LIMIT ) {
    			printCommentLine( line );
    			line.setLength(0);
    		}
    		
    		line.append(word);
    		line.append(' ');
    		
    		if( ( (i+1) == words.length) ){
    			printCommentLine( line );
    			line.setLength(0);
    		}
    	}
    }
    /**
     * Trims the string. It also trims unwanted spaces available between words to
     * a single space.
     * 
     * @param s
     * @return
     */
    private String trimComments(String s){

    	s = s.trim();
    	String seperator = getLineSperator( s );

    	String[] comments = s.split( seperator );
    	StringBuilder buf = new StringBuilder();

    	for( String comment : comments ){
    		comment = comment.trim();
    		if( "".equals( comment ) ){
    			continue;
    		}
    		
    		buf.append(comment);
    		buf.append( SPACE );

    	}
    	
    	return buf.toString();
    }


    /**
     * This method returns the line separator to be used.
     * This does not use System property line.separator.
     * It identifies by using contains operator.
     * 
     * @param s
     * @return
     */
    private String getLineSperator( String s ){

    	if( s.contains( RETURN_AND_NEW_LINE ) ){
    		return RETURN_AND_NEW_LINE;
    	}else if( s.contains( RETURN ) ){
    		return RETURN;
    	}

    	return NEW_LINE;
    }
}
