package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.io.InputStream;
import java.util.Map;

public interface ProtobufMetadataReader
{
    /**
     * Read Proto Metadata internal information from an inpustream.
     * @param inputStream
     * @return
     */
    public Map<String,Integer> read(InputStream inputStream);
}
