package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProtobufMetadataFileReader implements ProtobufMetadataReader
{
    @Override
    public Map<String, Integer> read(InputStream inputStream)
    {
        List<String> readFile = FileHelper.readFile(inputStream);
        int metadataStartIndex = FileHelper.getMetadataStartIndex(readFile);
        if (metadataStartIndex == -1)
        {
            return Collections.emptyMap();
        }
        int size = readFile.size();
        Map<String, Integer> internalData = new HashMap<String, Integer>();
        metadataStartIndex++;
        for (int counter = metadataStartIndex; counter < size; counter++)
        {
            String line = readFile.get(counter);
            if (line.contains(ProtobufMetadataConstants.S_PMD_END))
            {
                break;
            }
            if (!line.contains(ProtobufMetadataConstants.S_COMMENT))
            {
                throw new RuntimeException("PMD metadata content is not continuous. The .proto file is corrupted.");
            }
            line = line.replaceAll(ProtobufMetadataConstants.S_COMMENT, "");
            String[] split = line.split("=");
            String key = split[0];
            Integer value = new Integer(split[1]);
            if (internalData.containsKey(key))
            {
                throw new RuntimeException("Proto tag Metadata information contains duplicate tag assignments. Key: "
                        + key);
            }
            internalData.put(key, value);
        }
        return internalData;
    }
}
