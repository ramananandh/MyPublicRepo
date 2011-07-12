package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.xml.sax.InputSource;

public class DefaultProtobufTagGenerator implements ProtobufTagGenerator
{
    private ProtobufMetadataReader metadataReader = new ProtobufMetadataFileReader();

    private QnameMapBuilder qnameMapBuilder = null;
    private Map<QName, SchemaType> schemaMap = null;
    private Map<QName, SchemaType> elementMap = null;
    private Map<String, Integer> tagKeyMap = new HashMap<String, Integer>();
    private Map<String, Integer> tagRepeatedTypeNameMap = new HashMap<String, Integer>();
    private Map<String, Integer> tagSingleTypeNameMap = new HashMap<String, Integer>();
    private Definition definition;

    public DefaultProtobufTagGenerator(File wsdlFileLocation, File protofileLocation)
    {
        try
        {
            Map<String, Integer> read = null;
            if (protofileLocation!=null && protofileLocation.exists())
            {
                FileInputStream fileInputStream = new FileInputStream(protofileLocation);
                read = metadataReader.read(fileInputStream);
                fileInputStream.close();
            }
            else
            {
                read = new HashMap<String, Integer>();
            }
            ProtobufMetadataHelper helper = new ProtobufMetadataHelper(read);
            Map<String, Integer> compoundKeys = helper.getCompoundKeys();
            Map<String, Integer> repeatedKeys = helper.getRepeatedKeys();
            Map<String, Integer> singleKeys = helper.getSingleKeys();

            tagKeyMap.putAll(compoundKeys);
            tagRepeatedTypeNameMap.putAll(repeatedKeys);
            tagSingleTypeNameMap.putAll(singleKeys);

            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            InputSource inputSource = new InputSource(new FileInputStream(wsdlFileLocation));
            definition = reader.readWSDL(null, inputSource);
            qnameMapBuilder = new QnameMapBuilder(definition);
            schemaMap = qnameMapBuilder.getSchemaMap();
            elementMap = qnameMapBuilder.getElementMap();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Integer> getTagsForType(QName qName)
    {
        Map<String, Integer> result = new TreeMap<String, Integer>();
        SchemaTypeResolver resolver = new SchemaTypeResolver(qnameMapBuilder);
        // TODO reserved values. Avoid them.
        SchemaType schemaType = schemaMap.get(qName);
        schemaType = schemaType==null ?elementMap.get(qName):schemaType;
        if(schemaType==null)
        {
            return Collections.emptyMap();
        }
        List<ProtoMessageTag> elements = resolver.getElements(schemaType);
        for (ProtoMessageTag tag : elements)
        {
            String elementName = tag.getElementName();
            String tagKey = tag.getKey();
            String typeName = tag.getTypeName();
            if(elementName==null)
            {
                elementName = tag.getElementType();
            }
            Integer tagNumber = tagKeyMap.get(tagKey);
            if(result.containsKey(elementName))
            {
                throw new RuntimeException("Internal Exception. Tag generation contains duplicate keys: " + elementName);
            }
            if (tagNumber == null)
            {
                Map<String, Integer> mapToUse = null;
                Integer lastUsedNumber = null;
                if (tag.isRepeating() || tag.isRequired())
                {
                    mapToUse = tagRepeatedTypeNameMap;
                    lastUsedNumber = mapToUse.get(typeName);
                    if (lastUsedNumber == null)
                    {
                        lastUsedNumber = ProtobufTagConstants.S_PROTO_REQUIRED_TAG_START_NUMBER-1;
                    }
                    if (lastUsedNumber > ProtobufTagConstants.S_PROTO_OPTIONAL_TAG_START_NUMBER-2)
                    {
                        mapToUse = tagSingleTypeNameMap;
                        lastUsedNumber = mapToUse.get(typeName);
                        if (lastUsedNumber == null)
                        {
                            lastUsedNumber = ProtobufTagConstants.S_PROTO_OPTIONAL_TAG_START_NUMBER-1;
                        }
                    }
                }
                else
                {
                    mapToUse = tagSingleTypeNameMap;
                    lastUsedNumber = mapToUse.get(typeName);
                    if (lastUsedNumber == null)
                    {
                        lastUsedNumber = ProtobufTagConstants.S_PROTO_OPTIONAL_TAG_START_NUMBER-1;
                    }
                }
                lastUsedNumber = lastUsedNumber + 1;
                mapToUse.put(typeName, lastUsedNumber);
                tagKeyMap.put(tagKey, lastUsedNumber);
                result.put(elementName, lastUsedNumber);
            }
            else
            {
                result.put(elementName, tagNumber);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, Integer> getTagsToPersist()
    {
        Map<String, Integer> toWrite = new TreeMap<String, Integer>();
        toWrite.putAll(tagKeyMap);
        Set<Entry<String, Integer>> entrySet = tagRepeatedTypeNameMap.entrySet();
        for (Entry<String, Integer> entry : entrySet)
        {
            String key = entry.getKey();
            Integer value = entry.getValue();
            key = key + ProtobufMetadataConstants.S_REPEATED_TAG_KEY;
            toWrite.put(key, value);
        }
        entrySet = tagSingleTypeNameMap.entrySet();
        for (Entry<String, Integer> entry : entrySet)
        {
            String key = entry.getKey();
            Integer value = entry.getValue();
            key = key + ProtobufMetadataConstants.S_SINGLE_TAG_KEY;
            toWrite.put(key, value);
        }
        return toWrite;
    }
}
