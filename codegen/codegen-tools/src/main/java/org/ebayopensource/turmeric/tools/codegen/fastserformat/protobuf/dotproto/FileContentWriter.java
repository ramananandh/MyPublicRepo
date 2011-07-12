/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.dotproto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import com.sun.codemodel.util.EncoderFactory;

/**
 * @author rkulandaivel
 *
 */
public class FileContentWriter {

    /** The target directory to put source code. */
    private final File target;

    public FileContentWriter( File target) throws IOException {
        this.target = target;
        if(!target.exists() || !target.isDirectory())
            throw new IOException(target + ": non-existent directory");
    }


	/**
     * Returns the File out Stream for the given file name.
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    public OutputStream openBinary(String fileName) throws IOException {
        return new FileOutputStream(getFile(fileName));
    }
    
    /**
     * Returns the file object for the given file name.
     * Uses the target directory as the base folder.
     * If it encounters an existing file with the same name, it will try to delete.
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    protected File getFile(String fileName ) throws IOException {
        File fn = new File(target, fileName);
        
        if (fn.exists()) {
            if (!fn.delete())
                throw new IOException(fn + ": Can't delete previous version");
        }

        return fn;
    }

    /**
     * This method returns the unicode escape writer
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    public Writer openSource(  String fileName ) throws IOException {
        final OutputStreamWriter bw = new OutputStreamWriter(openBinary(fileName), Charset.defaultCharset());

        // create writer
        try {
            return new UnicodeEscapeWriter(bw);
        } catch( Throwable t ) {
            return new com.sun.codemodel.util.UnicodeEscapeWriter(bw);
        }
    }
    
    private static class UnicodeEscapeWriter extends com.sun.codemodel.util.UnicodeEscapeWriter{
    	// can't change this signature to Encoder because
        // we can't have Encoder in method signature
    	//hack for findbugs
        private Object encoder = null;
        private UnicodeEscapeWriter(OutputStreamWriter bw){
    		super( bw );
    		setEncoder( EncoderFactory.createEncoder(bw.getEncoding()) );
    	}
    	
        protected boolean requireEscaping(int ch) {
            // control characters
            if( ch<0x20 && " \t\r\n".indexOf(ch)==-1 )  return true;
            // check ASCII chars, for better performance
            if( ch<0x80 )       return false;

            return !getEncoder().canEncode((char)ch);
        }

		public CharsetEncoder getEncoder() {
			return (CharsetEncoder)encoder;
		}

		public void setEncoder(CharsetEncoder encoder) {
			this.encoder = encoder;
		}
        
    }
}
