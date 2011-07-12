package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProtobufMetadataFileWriter implements ProtobufMetadataWriter
{
    //TODO
    
    
    @Override
    public void write(Map<String, Integer> values, OutputStream outputStream)
    {
        List<String> result = new ArrayList<String>();
        
        /*List<String> temp = readFile(file);
        int startIndex = getMetadataStartIndex(temp);
        readFile.addAll(temp);

        if(startIndex!=-1)
        {
            readFile = readFile.subList(0, startIndex);
        }
*/       
        Set<Entry<String, Integer>> entrySet = values.entrySet();
        result.add(ProtobufMetadataConstants.S_PMD_START);
        for(Entry<String, Integer> entry : entrySet)
        {
            String key = entry.getKey();
            Integer value = entry.getValue();
            String valueToWrite = ProtobufMetadataConstants.S_COMMENT + key + "=" + value;
            result.add(valueToWrite);
        }
        result.add(ProtobufMetadataConstants.S_PMD_END);
        FileHelper.writeFile(outputStream, result);
    }
}
