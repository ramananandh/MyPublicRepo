package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProtobufMetadataHelper
{
    private Map<String,Integer> compoundKeys = new HashMap<String, Integer>();
    private Map<String,Integer> repeatedKeys = new HashMap<String, Integer>();
    private Map<String,Integer> singleKeys = new HashMap<String, Integer>();

    public ProtobufMetadataHelper(Map<String,Integer> data)
    {
        Map<String,Integer> internalData = new HashMap<String, Integer>();
        internalData.putAll(data);
        Set<String> keysToRemove = new HashSet<String>();
        Set<Entry<String, Integer>> entrySet = internalData.entrySet();
        for(Entry<String, Integer> entry : entrySet)
        {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if(key.contains(ProtobufMetadataConstants.S_REPEATED_TAG_KEY))
            {
                keysToRemove.add(key);
                key = key.replaceAll(ProtobufMetadataConstants.S_REPEATED_TAG_KEY, "");
                repeatedKeys.put(key, value);
            }
            else if(key.contains(ProtobufMetadataConstants.S_SINGLE_TAG_KEY))
            {
                keysToRemove.add(key);
                key = key.replaceAll(ProtobufMetadataConstants.S_SINGLE_TAG_KEY,"");
                singleKeys.put(key, value);   
            }
            else
            {
                compoundKeys.put(key, value);
            }
        }
    }
    
    

    public Map<String, Integer> getCompoundKeys()
    {
        return compoundKeys;
    }

    public Map<String, Integer> getRepeatedKeys()
    {
        return repeatedKeys;
    }

    public Map<String, Integer> getSingleKeys()
    {
        return singleKeys;
    }   
}