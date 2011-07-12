package org.ebayopensource.turmeric.runtime.common.impl.binding.protobuf;

public class ProtobufUtil {
	 public static String getProtoClassName(Class<?> type, String adminName) {
         StringBuilder protoClassBuilder = new StringBuilder();
         protoClassBuilder.append(type.getPackage().getName());
         protoClassBuilder.append('.');
         protoClassBuilder.append("proto.");
         protoClassBuilder.append(adminName);
         protoClassBuilder.append('$');
         protoClassBuilder.append(type.getSimpleName());
         return protoClassBuilder.toString();
     }

     public static String getExtendedClassName(Class<?> type) {         
         String canonicalName = type.getCanonicalName();
         int startOfClassName = canonicalName.lastIndexOf(".");
         StringBuilder extendedClassName = new StringBuilder();
         extendedClassName.append(canonicalName.substring(0, startOfClassName));
         extendedClassName.append(".proto.extended.E");
         extendedClassName.append(canonicalName.substring(startOfClassName + 1));
         return extendedClassName.toString();
     }
}
