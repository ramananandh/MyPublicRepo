package org.ebayopensource.turmeric.tools.codegen.proto;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufTagConstants.S_PROTO_TAG_MAX_VALUE;
import static org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufTagConstants.S_PROTO_TAG_MIN_VALUE;
import static org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufTagConstants.S_PROTO_TAG_RESERVED_MAX_VALUE;
import static org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufTagConstants.S_PROTO_TAG_RESERVED_MIN_VALUE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.TestResourceUtil;
import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.DefaultProtobufTagGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufMetadataFileWriter;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufMetadataWriter;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.ProtobufTagGenerator;
import org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag.QnameMapBuilder;
import org.junit.Test;
import org.xml.sax.InputSource;

public class ProtobufTagGeneratorTest extends AbstractServiceGeneratorTestCase
{
 
	
    private final File protoFile = getProtobufRelatedInput(".MasterProto");
    private final File corruptedProtoFile = getProtobufRelatedInput(".CorruptedMasterProto");
    private final File corruptedProtoWithSpaceFile = getProtobufRelatedInput(".CorruptedMasterProto_2.proto");
    private final File wsdlFile = getProtobufRelatedInput("ProtoTagMaster.wsdl");
    
    private final File junkProtoFile = new File("lib/JUNKPROTO");
    private final File junkWsdlFile = new File("lib/JUNKWSDL");
    
    public File getProtobufRelatedInput(String name) {
		return TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/proto/"
				+ name);
	}

    @Test
    public void testDefaultProtobufTagGenerator_positive()
    {
        ProtobufTagGenerator generator = new DefaultProtobufTagGenerator(wsdlFile,protoFile);
        assertNotNull(generator);
    }
    
    @Test
    public void testDefaultProtobufTagGenerator_negative()
    {
        ProtobufTagGenerator generator=null;
        boolean pass = false;
        try
        {
           generator = new DefaultProtobufTagGenerator(wsdlFile,corruptedProtoFile);
        }
        catch (Exception e)
        {
            pass = true;
        }
        if(!pass)
        {
            fail("Corrupted proto file instantiated as expected");
        }
        
        pass = false;
        try
        {
            generator = new DefaultProtobufTagGenerator(null,null);
        }
        catch (Exception e)
        {
            pass = true;
        }
        if(!pass)
        {
            fail("Corrupted proto file instantiated as expected");
        }   
        
        pass = false;
        try
        {
            generator = new DefaultProtobufTagGenerator(junkWsdlFile,junkProtoFile);
        }
        catch (Exception e)
        {
            pass = true;
        }
        if(!pass)
        {
            fail("Corrupted proto file instantiated as expected");
        }  
        
        pass = false;
        try
        {
            generator = new DefaultProtobufTagGenerator(junkWsdlFile,corruptedProtoWithSpaceFile);
        }
        catch (Exception e)
        {
            pass = true;
        }
        if(!pass)
        {
            fail("Corrupted proto file instantiated as expected");
        } 
        
        assertNull(generator);
    }

    @Test
    public void testGetTagsForType_testGetTagsToPersist_positive()
    {
        try
        {
            ProtobufTagGenerator generator = new DefaultProtobufTagGenerator(wsdlFile,protoFile);
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            InputSource inputSource = new InputSource(new FileInputStream(wsdlFile));
            Definition definition = reader.readWSDL(null, inputSource);
            
            QnameMapBuilder builder = new QnameMapBuilder(definition);
            Map<QName, SchemaType> elementMap = builder.getElementMap();
            Map<QName, SchemaType> schemaMap = builder.getSchemaMap();
            Map<QName, SchemaType> qnameMap = new HashMap<QName, SchemaType>();
            qnameMap.putAll(schemaMap);
            qnameMap.putAll(elementMap);
            
            Set<Entry<QName, SchemaType>> entrySet = qnameMap.entrySet();
            for(Entry<QName, SchemaType> entry : entrySet)
            {
                QName key = entry.getKey();
                Map<String, Integer> tagsForType = generator.getTagsForType(key);
                Set<Entry<String, Integer>> entrySet2 = tagsForType.entrySet();
                Set<String> valueSet = new HashSet<String>();
                Set<Integer> numberSet = new HashSet<Integer>();
                for(Entry<String, Integer> entry2 : entrySet2)
                {
                    String key2 = entry2.getKey();
                    Integer value = entry2.getValue();
                    boolean add = valueSet.add(key2);
                    if(!add)
                    {
                        fail("Duplicate tag ?" + key2);
                    }
                    boolean hasUniqueNumber = numberSet.add(value);
                    if(!hasUniqueNumber)
                    {
                        fail("Duplicate number ?" + key2);
                    }
                    assertNotNull(key2);
                    assertNotNull(value);
                    assertTrue(value>=S_PROTO_TAG_MIN_VALUE && value<=S_PROTO_TAG_MAX_VALUE);
                    assertFalse(value>=S_PROTO_TAG_RESERVED_MIN_VALUE  && value<=S_PROTO_TAG_RESERVED_MAX_VALUE);
                }
                assertNotNull(tagsForType);
            }
            Map<String, Integer> tagsToPersist = generator.getTagsToPersist();
            assertNotNull(tagsToPersist);
            
            ProtobufMetadataWriter metadataWriter = new ProtobufMetadataFileWriter();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            metadataWriter.write(tagsToPersist, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            assertNotNull(byteArray);
            String result = new String(byteArray);
            assertNotNull(result);
            assertNotNull(byteArray);
            assertTrue(result.length()>0);
            
            
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testGetTagsForType_testGetTagsToPersist_negative()
    {
        ProtobufTagGenerator generator = new DefaultProtobufTagGenerator(wsdlFile,protoFile);
        QName fakeName = new QName("http://nowhere/", "courage");
        Map<String, Integer> tagsForType = generator.getTagsForType(fakeName);
        assertTrue(tagsForType.size()==0);
    }

}
