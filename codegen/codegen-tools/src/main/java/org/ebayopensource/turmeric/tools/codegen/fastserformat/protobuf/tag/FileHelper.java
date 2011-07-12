package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileHelper
{
    private static final String S_NEWLINE = System.getProperty("line.separator");
    
    public static List<String> readFile(InputStream inputStream)
    {
        List<String> contents = new ArrayList<String>();
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
            String temp = "";
            while ((temp = bufferedReader.readLine()) != null)
            {
                contents.add(temp);
            }
            return contents;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }        
    }
    
    public static void writeFile(OutputStream file, List<String> data)
    {
        BufferedWriter bufferedWriter = null;
        try
        {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(file, Charset.defaultCharset()));
            for(String line : data)
            {
                bufferedWriter.write(line);
                bufferedWriter.write(S_NEWLINE);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if(bufferedWriter!=null)
                {
                    bufferedWriter.flush();
                    bufferedWriter.close();    
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static int getMetadataStartIndex(List<String> data)
    {
        int startIndex=-1;
        int counter = 0;
        for(String line : data)
        {
            if(line.contains(ProtobufMetadataConstants.S_PMD_START))
            {
                startIndex = counter;
                break;
            }
            counter++;
        }
        return startIndex;
    }
}
