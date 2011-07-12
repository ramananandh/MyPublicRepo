package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.eproto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.ebayopensource.turmeric.tools.codegen.builders.BaseCodeGenerator;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.util.ContextClassLoaderUtil;

public abstract class BaseEProtoGenerator extends BaseCodeGenerator{
	
	/**
	 * Populates the initial context based on the information obatined from the Proto Model generated 
	 * in the previous step. This is the carrier of data across theEProto generation
	 * @throws CodeGenFailedException 
	 */
	public  EProtoContext getPojoInfo(
					String fullyQualifiedEProtoName, 
					String complexTypeName,
					String fullyQualifiedJProtoName,
					String fullyQualifiedName) throws CodeGenFailedException{
		
		EProtoContext info = new EProtoContext();
		info.setFullyQualifiedName(fullyQualifiedName);

		
		info.setFullyQualifiedEProtoName(fullyQualifiedEProtoName);
		info.setComplexTypeName(complexTypeName);
		info.setBuilderClass(getBuilderClassName(complexTypeName, fullyQualifiedJProtoName));
		info.setFullyQualifiedJProtoMessageName(fullyQualifiedJProtoName);
		Class<?> baseClass = ContextClassLoaderUtil.loadOptionalClass(fullyQualifiedName);
		
		getFields(baseClass, info);
		getMethods(baseClass, info);
		Class<?> superClass = baseClass.getSuperclass();
		
		while(superClass != null && superClass.getName() != "java.lang.Object"){
			getFields(superClass, info);
			getMethods(superClass, info);
			superClass = superClass.getSuperclass();
		}
		
		return info;		
	}
	
	/**
	 * Gets all the Fields(java.lang.reflect.Field) defined in the base POJO class
	 */
	protected  void getFields(Class<?> fullyQualifiedClass, EProtoContext info){
		Field[] fields = fullyQualifiedClass.getDeclaredFields();
		info.getDeclaredFields().addAll(Arrays.asList(fields));
	}
	
	/**
	 * Gets all the methods(java.lang.reflect.Method) defined in the base POJO class. And also helps in maintaining 
	 * a map of getters to fields (useful in making use of Google's FieldDesciptors APIs.
	 */
	protected  void getMethods(Class<?> fullyQualifiedClass, EProtoContext info){
		
		Method[] methods = fullyQualifiedClass.getDeclaredMethods();
		for (Method singleMethod : methods) {
			String methodName = singleMethod.getName().toLowerCase();
//			HACK  just let MarketPlace extension types (xs:any) to go through(just ignore) and workaround
//			for field names using JAVA keywords
			for(Field fieldName : info.getDeclaredFields()){
				String actualFieldName = fieldName.getName().toLowerCase();
				if(actualFieldName.startsWith("_"))
					actualFieldName = actualFieldName.replace("_", "");
				if(singleMethod != null && singleMethod.getParameterTypes().length == 0 
						&& (singleMethod.getName().equalsIgnoreCase("get" + actualFieldName) 
								|| singleMethod.getName().equalsIgnoreCase("is" + actualFieldName))
						&& !("getAny".equals(singleMethod.getName()))){
					info.getGetterToFields().put(singleMethod.getName(), fieldName.getName());
					info.getMethods().add(singleMethod);
					break;	
				}
			}
			
		}
	}	
	
	protected static String getBuilderClassName(String complexTypename, String fullyQualifiedJProtoName){
		StringBuilder builder = new StringBuilder( fullyQualifiedJProtoName);
//		StringBuilder builder = new StringBuilder(getJProtoMessageName(complexTypename, fullyQualifiedJProtoName));
		builder.append("$Builder");
		return builder.toString();
	}
	

	/**
	 * Mapping class to maintain mapping between a message and its corresponding names in 
	 * different technologies. This includes
	 * 		1) Fully Qualified EProto Class Name
	 * 		2) Complex Type name(message name)
	 * 		3) Fully Qualified JProto Class Name
	 * 		4) Fully Qualified POJO Class Name
	 */
	static class JAXB2EJProtoMapping{
		
		String eprotoName;
		String complexTypeName;
		String jprotoName;
		String jaxbName;
		boolean isRootType;
				
		public String getJaxbName() {
			return jaxbName;
		}

		public void setJaxbName(String jaxbName) {
			this.jaxbName = jaxbName;
		}

		public String getComplexTypeName() {
			return complexTypeName;
		}

		public void setComplexTypeName(String complexTypeName) {
			this.complexTypeName = complexTypeName;
		}

		public String getJprotoName() {
			return jprotoName;
		}
		
		public void setJprotoName(String jprotoName) {
			this.jprotoName = jprotoName;
		}
		
		public String getEprotoName() {
			return eprotoName;
		}
		
		public void setEprotoName(String eprotoName) {
			this.eprotoName = eprotoName;
		}

		public boolean isRootType() {
			return isRootType;
		}

		public void setRootType(boolean isRootType) {
			this.isRootType = isRootType;
		}

		@Override
		public String toString() {

			return "JAXB2EJProtoMapping [complexTypeName=" + complexTypeName
			+ ", eprotoName=" + eprotoName + ", isRootType="
			+ isRootType + ", jaxbName=" + jaxbName + ", jprotoName="
			+ jprotoName + "]";

		}		
		
	}

}
