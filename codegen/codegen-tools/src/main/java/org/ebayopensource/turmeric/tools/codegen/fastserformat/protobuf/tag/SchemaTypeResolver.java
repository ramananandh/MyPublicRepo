package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Attribute;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Attribute.AttributeUse;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroup;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.AttributeGroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Choice;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ComplexContent;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ComplexType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.ElementType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Extension;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Group;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.GroupType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Restriction;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.RestrictionEnumeration;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaAll;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SchemaType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Sequence;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.Sequence.SequenceEntry;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SequenceElement;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleContent;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleContentExtension;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleType;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleTypeList;
import org.ebayopensource.turmeric.tools.codegen.external.wsdl.parser.schema.SimpleTypeRestriction;

//TODO this class has become extremely bloated and complex. Its responsibility needs to be split

/**
 * Resolves the elements / attributes under a Schema type by flattening them
 * recursively.
 */
public class SchemaTypeResolver
{
    private static List<ProtoMessageTag> S_EMPTY_LIST = Collections.emptyList();
    private QName currentType = null;
    private ElementType currentElementType = null;
    private Map<QName, SchemaType> elementMap = null;
    private Map<QName, SchemaType> groupMap = null;
    private Map<QName, SchemaType> attributeMap = null;
    private Map<QName, SchemaType> attributeGroupMap = null;
    private Map<QName, SchemaType> schemaMap = null;

    private final Stack<Integer> choiceStack = new Stack<Integer>();
    private int currentAllOccurs = -1;
    private int refMinOccurs = Integer.MIN_VALUE;
    private int refMaxOccurs = Integer.MAX_VALUE;
    private int nestedLevel = 0;

    public SchemaTypeResolver(QnameMapBuilder mapBuilder)
    {
        elementMap = mapBuilder.getElementMap();
        groupMap = mapBuilder.getGroupMap();
        schemaMap = mapBuilder.getSchemaMap();
        attributeMap = mapBuilder.getAttributeMap();
        attributeGroupMap = mapBuilder.getAttributeGroupMap();
    }

    /**
     * For a given schema type, get all the proto messages for that type and its parent.
     * Types are one of the following - Complex types / Simple types / Elements with an anon type
     * 
     * @param schemaType
     * @return - A list of proto fields that need to be assigned for this type. 
     */
    public List<ProtoMessageTag> getElements(SchemaType schemaType)
    {
        try
        {
            List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
            SchemaType next = schemaType;
            currentType = schemaType.getTypeName();
            List<ProtoMessageTag> handleSchemaType = handleSchemaType(next);
            result.addAll(handleSchemaType);
            return result;
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "There was an internal error while processing the WSDL for types. Tag assignment has failed", e);
        }
    }

    private List<ProtoMessageTag> handleSchemaType(SchemaType next)
    {
        nestedLevel++;
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        if (next == null)
        {
            nestedLevel--;
            return S_EMPTY_LIST;
        }
        
        else if (next.isComplex())
        {
            ComplexType complexType = (ComplexType) next;
            List<ProtoMessageTag> handleComplexType = handleComplexType(complexType);
            result.addAll(handleComplexType);
        }
        else if (next.isSimple())
        {
            SimpleType simpleType = (SimpleType) next;
            List<ProtoMessageTag> handleSimpleType = handleSimpleType(simpleType);
            result.addAll(handleSimpleType);
        }
        else if (next instanceof GroupType)
        {
            GroupType groupType = (GroupType) next;
            List<ProtoMessageTag> handleGroup = handleGroup(groupType);
            result.addAll(handleGroup);
        }
        else if (next instanceof AttributeGroupType)
        {
            AttributeGroupType attributeGroupType = (AttributeGroupType) next;
            List<ProtoMessageTag> handleAttributeGroup = handleAttributeGroup(attributeGroupType);
            result.addAll(handleAttributeGroup);
        }
        else if (next instanceof Attribute)
        {
            Attribute attribute = (Attribute) next;
            List<Attribute> attributes = new ArrayList<Attribute>();
            attributes.add(attribute);
            List<ProtoMessageTag> handleAttributes = handleAttributes(attributes);
            result.addAll(handleAttributes);
        }
        else if (next.isElement())
        {
            ElementType elementType = (ElementType) next;
            List<ProtoMessageTag> handleElement = handleElement(elementType);
            result.addAll(handleElement);
        }
        nestedLevel--;
        return result;
    }

    private List<ProtoMessageTag> handleAttributeGroup(AttributeGroupType attributeGroupType)
    {
        if (attributeGroupType == null)
        {
            return S_EMPTY_LIST;
        }
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        List<AttributeGroup> attributeGroups = attributeGroupType.getAttributeGroups();
        for (AttributeGroup group : attributeGroups)
        {
            QName groupRef = group.getGroupRef();
            SchemaType schemaType = groupMap.get(groupRef);
            List<ProtoMessageTag> handleSchemaType = handleSchemaType(schemaType);
            result.addAll(handleSchemaType);
        }
        List<Attribute> attributes = attributeGroupType.getAttributes();
        List<ProtoMessageTag> handleAttributes = handleAttributes(attributes);
        result.addAll(handleAttributes);
        return result;
    }

    private List<ProtoMessageTag> handleAttributes(List<Attribute> attributes)
    {
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        for (Attribute attribute : attributes)
        {
            QName attributeRef = attribute.getAttributeRef();
            if (attributeRef != null)
            {
                SchemaType schemaType = attributeMap.get(attributeRef);
                AttributeUse use = attribute.getUse();
                if (use == AttributeUse.REQUIRED)
                {
                    refMinOccurs = 1;
                }
                else if (use == AttributeUse.OPTIONAL)
                {
                    refMinOccurs = 0;
                }
                else if (use == AttributeUse.PROHIBHITED)
                {
                    return result;
                }
                List<ProtoMessageTag> handleSchemaType = handleSchemaType(schemaType);
                result.addAll(handleSchemaType);
            }
            else
            {
                String nameToUse = attribute.getValueType().getLocalPart();
                String attributeName = attribute.getAttributeName();
                String currentTypeName = currentType.toString();
                AttributeUse use = attribute.getUse();
                if (use == AttributeUse.PROHIBHITED)
                {
                    continue;
                }
                if (refMinOccurs >= 1)
                {
                    use = AttributeUse.REQUIRED;
                    refMinOccurs = Integer.MIN_VALUE;
                }

                ProtoMessageTag messageTag = new ProtoMessageTag(currentTypeName, "@" + attributeName, nameToUse);
                QName valueType = attribute.getValueType();
                if (valueType.getLocalPart().equals("NMTOKENS"))
                {
                    messageTag.setRequired(true);
                }
                if (use == AttributeUse.REQUIRED)
                {
                    messageTag.setRequired(true);
                }
                SchemaType schemaType = schemaMap.get(valueType);
                if(schemaType instanceof SimpleType)
                {
                    SimpleType simpleType = (SimpleType)schemaType;
                    boolean simpleTypeList = isSimpleTypeList(simpleType);
                    if(simpleTypeList)
                    {
                        messageTag.setRepeating(true);    
                    }
                }
                result.add(messageTag);
            }
        }
        return result;
    }

    private List<ProtoMessageTag> handleGroup(GroupType groupType)
    {
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        Choice choice = groupType.getChoice();
        Sequence sequence = groupType.getSequence();
        SchemaAll all = groupType.getAll();

        List<ProtoMessageTag> handleChoice = handleChoice(choice);
        List<ProtoMessageTag> handleSequence = handleSequence(sequence);
        List<ProtoMessageTag> handleAll = handleAll(all);

        result.addAll(handleChoice);
        result.addAll(handleSequence);
        result.addAll(handleAll);
        return result;
    }

    private List<ProtoMessageTag> handleElement(ElementType elementType)
    {
        currentElementType = elementType;
        QName ref = elementType.getRef();
        QName typeNameToUse = elementType.getElementType();

        if (ref != null)
        {
            SchemaType schemaType = elementMap.get(ref);
            typeNameToUse = schemaType.getTypeName();
        }
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        ComplexType complexType = elementType.getComplexType();
        SimpleType simpleType = elementType.getSimpleType();
        List<ProtoMessageTag> handleComplexType = handleComplexType(complexType);
        List<ProtoMessageTag> handleSimpleType = handleSimpleType(simpleType);
        result.addAll(handleSimpleType);
        result.addAll(handleComplexType);

        if (typeNameToUse == null)
        {
            typeNameToUse = currentElementType.getTypeName();
            if (typeNameToUse == null)
            {
                typeNameToUse = currentType;
            }
        }
        String nameToUse = typeNameToUse.getLocalPart();

        QName typeName = elementType.getTypeName();
        String currentTypeQName = currentType.toString();

        if (typeName == null)
        {
            typeName = typeNameToUse;
        }

        String localPart = typeName.getLocalPart();
        ProtoMessageTag messageTag = null;
        
        boolean isTopLevel = (nestedLevel==1);
        if (isTopLevel)
        {
            return result;
        }

        messageTag = new ProtoMessageTag(currentTypeQName, localPart, nameToUse);
        int minOccurs = elementType.getMinOccurs();
        int maxOccurs = elementType.getMaxOccurs();
        minOccurs = refMinOccurs == Integer.MIN_VALUE ? minOccurs : refMinOccurs;
        maxOccurs = refMaxOccurs == Integer.MAX_VALUE ? maxOccurs : refMaxOccurs;
        ProtoMessageModifier messageModifier = ProtoMessageModifier.fromBounds(minOccurs, maxOccurs);
        if (!choiceStack.empty())
        {
            switch (messageModifier)
            {
            case REPEATED:
            {
                messageTag.setRepeating(true);
                break;
            }
            }
        }
        else
        {
            switch (messageModifier)
            {
            case REPEATED:
            {
                messageTag.setRepeating(true);
                break;
            }
            case REQUIRED:
            {
                messageTag.setRequired(true);
                break;
            }
            }
        }
        String localPart2 = typeNameToUse.getLocalPart();
        if (localPart2.equals("NMTOKENS"))
        {
            messageTag.setRepeating(true);
        }
        if (isSimpleTypeList(simpleType))
        {
            messageTag.setRepeating(true);
        }
        SchemaType schemaType = schemaMap.get(typeNameToUse);
        if (schemaType instanceof SimpleType)
        {
            SimpleType type = (SimpleType) schemaType;
            if (isSimpleTypeList(type))
            {
                messageTag.setRepeating(true);
            }
        }
        if (currentAllOccurs == 0)
        {
            messageTag.setRepeating(false);
            messageTag.setRequired(false);
        }
        if (!(messageModifier == ProtoMessageModifier.DO_NOT_ASSIGN))
        {
            result.add(messageTag);
        }
        return result;
    }

    private List<ProtoMessageTag> handleSimpleType(SimpleType simpleType)
    {
        if (simpleType == null)
        {
            return S_EMPTY_LIST;
        }
        List<ProtoMessageTag> list = new ArrayList<ProtoMessageTag>();
        // TODO duplicated logic
        QName typeName = simpleType.getTypeName();
        if (typeName == null)
        {
            typeName = currentType;
        }

        SimpleTypeList list2 = simpleType.getList();
        if (list2 != null)
        {
            return S_EMPTY_LIST;
        }

        SimpleTypeRestriction restriction = simpleType.getRestriction();
        List<ProtoMessageTag> handleSimpleRestriction = handleSimpleRestriction(restriction);
        list.addAll(handleSimpleRestriction);
        return list;
    }

    private List<ProtoMessageTag> handleComplexType(ComplexType complexType)
    {
        if (complexType == null)
        {
            return S_EMPTY_LIST;
        }
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();

        Sequence sequence = complexType.getSequence();
        Choice choice = complexType.getChoice();
        List<AttributeGroup> attributeGroup = complexType.getAttributeGroup();
        List<Attribute> attributes = complexType.getAttributes();
        Group group2 = complexType.getGroup();
        SchemaAll complexAll = complexType.getAll();
        List<ProtoMessageTag> complexHandleAll = handleAll(complexAll);
        result.addAll(complexHandleAll);

        List<ProtoMessageTag> handleGroup = handleGroup(group2);
        result.addAll(handleGroup);

        for (AttributeGroup attGroup : attributeGroup)
        {
            QName attGroupRef = attGroup.getGroupRef();
            SchemaType schemaType = attributeGroupMap.get(attGroupRef);
            List<ProtoMessageTag> handleSchemaType = handleSchemaType(schemaType);
            result.addAll(handleSchemaType);
        }

        List<ProtoMessageTag> handleSequence = handleSequence(sequence);
        List<ProtoMessageTag> handleChoice = handleChoice(choice);
        List<ProtoMessageTag> handleAttributes = handleAttributes(attributes);
        result.addAll(handleAttributes);
        result.addAll(handleSequence);
        result.addAll(handleChoice);

        ComplexContent complexContent = complexType.getComplexContent();
        if (complexContent != null)
        {
            Extension extension = complexContent.getExtension();
            if (extension != null)
            {
                List<ProtoMessageTag> handleExtension = handleExtension(extension);
                result.addAll(handleExtension);
            }
            Restriction restriction = complexContent.getRestriction();

            if (restriction != null)
            {
                QName base = restriction.getBase();
                SchemaType schemaType = schemaMap.get(base);
                List<ProtoMessageTag> handleSchemaType = handleSchemaType(schemaType);
                result.addAll(handleSchemaType);
                SchemaAll all = restriction.getAll();
                List<ProtoMessageTag> handleAll = handleAll(all);
                result.addAll(handleAll);
            }

        }
        SimpleContent simpleContent = complexType.getSimpleContent();
        if (simpleContent != null)
        {
            SimpleContentExtension extension = simpleContent.getExtension();
            SimpleTypeRestriction restriction = simpleContent.getRestriction();

            // TODO: Duplicate code here ?
            if (extension != null)
            {
                QName base = extension.getBase();

                // Skip handling simple type enumerations. They are not part of
                // the message
                SchemaType schemaType2 = schemaMap.get(base);
                boolean skipHandling = false;
                if (schemaType2 instanceof SimpleType)
                {
                    SimpleType testType = (SimpleType) schemaType2;
                    SimpleTypeRestriction restriction2 = testType.getRestriction();
                    if (restriction2 != null)
                    {
                        List<RestrictionEnumeration> enumerations = restriction2.getEnumerations();
                        if (enumerations != null)
                        {
                            skipHandling = true;
                        }
                    }
                }
                if (!skipHandling)
                {
                    List<ProtoMessageTag> handleSchemaType2 = handleSchemaType(schemaType2);
                    result.addAll(handleSchemaType2);
                }

                String parent = getParent(base);
                if (parent != null)
                {
                    ProtoMessageTag valueTag = getValueTag(currentType.toString(), parent);
                    if (!result.contains(valueTag))
                    {
                        result.add(valueTag);
                    }
                }
                List<AttributeGroup> attributeGroups = extension.getAttributeGroups();

                for (AttributeGroup group : attributeGroups)
                {
                    QName groupRef = group.getGroupRef();
                    SchemaType schemaType = attributeGroupMap.get(groupRef);
                    List<ProtoMessageTag> handleSchemaType = handleSchemaType(schemaType);
                    result.addAll(handleSchemaType);
                }
                List<Attribute> attributes2 = extension.getAttributes();
                List<ProtoMessageTag> handleSimpleAttributes = handleAttributes(attributes2);
                result.addAll(handleSimpleAttributes);
            }
            if (restriction != null)
            {
                List<ProtoMessageTag> handleSimpleRestriction = handleSimpleRestriction(restriction);
                result.addAll(handleSimpleRestriction);
            }
        }
        return result;
    }

    private List<ProtoMessageTag> handleExtension(Extension extension)
    {
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();

        QName base = extension.getBase();
        SchemaType schemaType2 = schemaMap.get(base);
        List<ProtoMessageTag> handleSchemaType2 = handleSchemaType(schemaType2);
        result.addAll(handleSchemaType2);

        Sequence extensionSequence = extension.getSequence();
        Group extensionGroup = extension.getGroup();
        Choice choice = extension.getChoice();

        List<AttributeGroup> attributeGroups = extension.getAttributeGroup();
        List<Attribute> attributeList = extension.getAttributeList();
        SchemaAll all = extension.getAll();

        for (AttributeGroup attributeGroup : attributeGroups)
        {
            QName groupRef = attributeGroup.getGroupRef();
            SchemaType schemaType = attributeGroupMap.get(groupRef);
            List<ProtoMessageTag> handleSchemaType = handleSchemaType(schemaType);
            result.addAll(handleSchemaType);
        }
        List<ProtoMessageTag> handleAttributes = handleAttributes(attributeList);
        List<ProtoMessageTag> handleSequence = handleSequence(extensionSequence);
        List<ProtoMessageTag> handleChoice = handleChoice(choice);
        List<ProtoMessageTag> handleGroup = handleGroup(extensionGroup);
        List<ProtoMessageTag> handleAll = handleAll(all);

        result.addAll(handleAttributes);
        result.addAll(handleChoice);
        result.addAll(handleSequence);
        result.addAll(handleGroup);
        result.addAll(handleAll);

        return result;
    }

    private List<ProtoMessageTag> handleSequence(Sequence sequence)
    {
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        if (sequence != null)
        {
            List<SequenceEntry> entries = sequence.getEntries();

            for (SequenceEntry entry : entries)
            {
                if (entry.isElement())
                {
                    SequenceElement element = entry.getElement();
                    List<ProtoMessageTag> handleSchemaType = handleSchemaType(element);
                    result.addAll(handleSchemaType);
                }
                else if (entry.isChoice())
                {
                    Choice choice = entry.getChoice();
                    List<ProtoMessageTag> handleChoice = handleChoice(choice);
                    result.addAll(handleChoice);
                }
                else if (entry.isGroup())
                {
                    Group group = entry.getGroup();
                    List<ProtoMessageTag> handleGroup = handleGroup(group);
                    result.addAll(handleGroup);
                }
                else if (entry.isSequence())
                {
                    Sequence internalSequence = entry.getSequence();
                    List<ProtoMessageTag> handleSequence = handleSequence(internalSequence);
                    result.addAll(handleSequence);
                }
            }
        }
        return result;
    }

    private List<ProtoMessageTag> handleChoice(Choice choice)
    {
        if (choice == null)
        {
            return S_EMPTY_LIST;
        }
        choiceStack.push(1);
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        List<SequenceElement> elements = choice.getElements();
        for (SequenceElement element : elements)
        {
            List<ProtoMessageTag> handleSchemaType = handleSchemaType(element);
            result.addAll(handleSchemaType);
        }
        List<Group> groups = choice.getGroups();
        List<Sequence> sequences = choice.getSequences();
        List<Choice> choices = choice.getChoices();
        for (Group group : groups)
        {
            List<ProtoMessageTag> handleGroup = handleGroup(group);
            result.addAll(handleGroup);
        }
        for (Sequence sequence : sequences)
        {
            List<ProtoMessageTag> handleSequence = handleSequence(sequence);
            result.addAll(handleSequence);
        }
        for (Choice internalChoice : choices)
        {
            List<ProtoMessageTag> handleChoice = handleChoice(internalChoice);
            result.addAll(handleChoice);
        }
        choiceStack.pop();
        return result;
    }

    private List<ProtoMessageTag> handleGroup(Group group)
    {
        if (group == null)
        {
            return S_EMPTY_LIST;
        }
        QName groupRef = group.getGroupRef();
        SchemaType schemaType = groupMap.get(groupRef);
        List<ProtoMessageTag> result = handleSchemaType(schemaType);
        return result;
    }

    private List<ProtoMessageTag> handleSimpleRestriction(SimpleTypeRestriction restriction)
    {
        if (restriction == null)
        {
            return S_EMPTY_LIST;
        }
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        QName base = restriction.getBase();
        String localPart = base.getLocalPart();

        SchemaType schemaType = schemaMap.get(base);
        List<ProtoMessageTag> handleSchemaType = handleSchemaType(schemaType);
        result.addAll(handleSchemaType);

        List<RestrictionEnumeration> enumerations = restriction.getEnumerations();
        boolean isRestriction = enumerations.size() > 0;

        for (RestrictionEnumeration restrictionEnumeration : enumerations)
        {
            String enumValue = restrictionEnumeration.getEnumValue();
            ProtoMessageTag messageTag = new ProtoMessageTag(currentType.toString(), enumValue, localPart);
            result.add(messageTag);
        }
        if (!isRestriction)
        {
            String parent = getParent(base);
            if (parent != null)
            {
                ProtoMessageTag valueTag = getValueTag(currentType.toString(), parent);
                if (!result.contains(valueTag))
                {
                    result.add(valueTag);
                }
            }
            // TODO duplicate code
        }
        return result;
    }

    private ProtoMessageTag getValueTag(String typeName, String elementType)
    {
        ProtoMessageTag messageTag = new ProtoMessageTag(typeName, "value", elementType);
        messageTag.setSystemType(true);
        return messageTag;
    }

    private String getParent(QName type)
    {
        String result = type.getLocalPart();
        SchemaType schemaType = schemaMap.get(type);
        while (schemaType != null)
        {
            if (schemaType instanceof SimpleType)
            {
                SimpleType simpleType = (SimpleType) schemaType;
                SimpleTypeRestriction restriction = simpleType.getRestriction();
                if (restriction != null)
                {
                    // Enum restrictions should not allow further drill downs
                    List<RestrictionEnumeration> enumerations = restriction.getEnumerations();
                    if (enumerations != null && enumerations.size() > 0)
                    {
                        return simpleType.getTypeName().getLocalPart();
                    }
                    QName base = restriction.getBase();
                    result = base.getLocalPart();
                    schemaType = schemaMap.get(base);
                }
                else
                {
                    result = schemaType.getTypeName().getLocalPart();
                    schemaType = null;
                }
            }
            else
            {
                schemaType = null;
                result = null;
            }
        }
        return result;
    }

    private List<ProtoMessageTag> handleAll(SchemaAll all)
    {
        if (all == null)
        {
            return S_EMPTY_LIST;
        }
        int minOccurs = all.getMinOccurs();
        currentAllOccurs = minOccurs;
        List<ProtoMessageTag> result = new ArrayList<ProtoMessageTag>();
        List<SequenceElement> elements = all.getElements();
        for (SequenceElement element : elements)
        {
            List<ProtoMessageTag> handleSchemaType = handleSchemaType(element);
            result.addAll(handleSchemaType);
        }
        currentAllOccurs = -1;
        return result;
    }

    private boolean isSimpleTypeList(SimpleType simpleType)
    {
        if (simpleType != null)
        {
            SimpleTypeList list = simpleType.getList();
            if (list != null)
            {
                return true;
            }
        }
        return false;
    }
}