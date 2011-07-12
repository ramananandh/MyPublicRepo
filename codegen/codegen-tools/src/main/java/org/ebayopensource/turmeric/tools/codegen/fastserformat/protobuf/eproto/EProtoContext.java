package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.eproto;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EProtoContext {
	
	protected String fullyQualifiedPOJOName;
	
	protected String complexTypeName;

	protected String fullyQualifiedEProtoName;
	
	protected String fullyQualifiedJProtoMessageName;
	
	protected String builderClass;

	protected boolean isRootType;
	
	protected List<Field> declaredFields;
	
	protected List<Method> methods;
	
	protected Map<String, String> getterToField;
	
	public String getFullyQualifiedName() {
		return fullyQualifiedPOJOName;
	}

	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedPOJOName = fullyQualifiedName;
	}

	public Map<String, String> getGetterToFields() {
		if(getterToField == null)
			getterToField = new HashMap<String, String>();
		return getterToField;
	}

	public List<Field> getDeclaredFields() {
		if(declaredFields == null)
			declaredFields = new ArrayList<Field>();
		return declaredFields;
	
	}

	public List<Method> getMethods() {
		if(methods == null)
			methods = new ArrayList<Method>();
		return methods;
	}

	public String getFullyQualifiedEProtoName() {
		return fullyQualifiedEProtoName;
	}

	public void setFullyQualifiedEProtoName(String fullyQualifiedEProtoName) {
		this.fullyQualifiedEProtoName = fullyQualifiedEProtoName;
	}

	public String getBuilderClass() {
		return builderClass;
	}

	public void setBuilderClass(String builderClass) {
		this.builderClass = builderClass;
	}

	public boolean isRootType() {
		return isRootType;
	}

	public void setRootType(boolean isRootType) {
		this.isRootType = isRootType;
	}

	public String getComplexTypeName() {
		return complexTypeName;
	}

	public void setComplexTypeName(String complexTypeName) {
		this.complexTypeName = complexTypeName;
	}

	public String getFullyQualifiedJProtoMessageName() {
		return fullyQualifiedJProtoMessageName;
	}

	public void setFullyQualifiedJProtoMessageName(
			String fullyQualifiedJProtoMessageName) {
		this.fullyQualifiedJProtoMessageName = fullyQualifiedJProtoMessageName;
	}

}
