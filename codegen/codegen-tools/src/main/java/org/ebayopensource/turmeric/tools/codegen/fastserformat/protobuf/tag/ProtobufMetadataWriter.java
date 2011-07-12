package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.io.OutputStream;
import java.util.Map;

public interface ProtobufMetadataWriter
{
    /**
     * Write Proto Metadata internal information into an outputStream.
     * @param values
     * @param outputStream
     */
    public void write(Map<String, Integer> values,OutputStream outputStream);
}
