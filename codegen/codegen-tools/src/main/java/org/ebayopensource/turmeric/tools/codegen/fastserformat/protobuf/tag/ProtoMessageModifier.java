package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

public enum ProtoMessageModifier
{
    REQUIRED("REQUIRED"), OPTIONAL("OPTIONAL"), REPEATED("REPEATED"), DO_NOT_ASSIGN("DO_NOT_ASSIGN");
    
    private String type;
    
    private ProtoMessageModifier(String type)
    {
        this.type = type;
    }
    
    public static ProtoMessageModifier fromBounds(int minOccurs, int maxOccurs)
    {
        if(minOccurs==-1 && maxOccurs==-1 )
        {
            return REQUIRED;
        }
        if(minOccurs<=0)
        {
            if(maxOccurs==0)
            {
                return DO_NOT_ASSIGN;
            }
            if(maxOccurs<=1)
            {
                return OPTIONAL;    
            }
            return REPEATED;
        }
        if(minOccurs>=1)
        {
            if(maxOccurs==1 || maxOccurs==-1)
            {
                return REQUIRED;
            }
            if(maxOccurs>1)
            {
                return REPEATED;
            }
        }
        return null;
    }

    public String getType()
    {
        return type;
    }

}
