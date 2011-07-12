package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

/**
 * Internal representation of a proto field meant for tagging 
 */
public class ProtoMessageTag
{
    private final String typeName;
    private final String elementName;
    private final String elementType;
    
    private boolean isRepeating;
    private boolean isRequired;
    private boolean isSystemType;
    
    public ProtoMessageTag(String typeName, String elementName, String elementType)
    {
        super();
        this.typeName = typeName;
        this.elementName = elementName;
        this.elementType = elementType;
    }
    
    public boolean isSystemType()
    {
        return isSystemType;
    }

    public void setSystemType(boolean isSystemType)
    {
        this.isSystemType = isSystemType;
    }



    public String getElementName()
    {
        return elementName;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public String getElementType()
    {
        return elementType;
    }

    public String getKey()
    {
        String key;
        if(elementName==null)
        {
            key = typeName + "/" + elementType;
        }
        else
        {
            key = typeName + "/" + elementName + "/" + elementType;
        }
        return key;
    }
    
    
    public boolean isRepeating()
    {
        return isRepeating;
    }
    public void setRepeating(boolean isRepeating)
    {
        this.isRepeating = isRepeating;
    }
    
    public boolean isRequired()
    {
        return isRequired;
    }

    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }
    
    

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
        result = prime * result + ((elementType == null) ? 0 : elementType.hashCode());
        result = prime * result + (isRepeating ? 1231 : 1237);
        result = prime * result + (isRequired ? 1231 : 1237);
        result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProtoMessageTag other = (ProtoMessageTag) obj;
        if (elementName == null)
        {
            if (other.elementName != null)
                return false;
        }
        else if (!elementName.equals(other.elementName))
            return false;
        if (elementType == null)
        {
            if (other.elementType != null)
                return false;
        }
        else if (!elementType.equals(other.elementType))
            return false;
        if (isRepeating != other.isRepeating)
            return false;
        if (isRequired != other.isRequired)
            return false;
        if (typeName == null)
        {
            if (other.typeName != null)
                return false;
        }
        else if (!typeName.equals(other.typeName))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "ProtoMessageTag [typeName=" + typeName + ", elementName=" + elementName + ", elementType="
                + elementType + ", isRepeating=" + isRepeating + ", isRequired=" + isRequired + "]";
    }
}
