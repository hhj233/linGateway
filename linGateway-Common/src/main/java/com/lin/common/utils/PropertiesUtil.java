package com.lin.common.utils;

import java.lang.reflect.Method;
import java.util.Properties;

public class PropertiesUtil {
    public static void properties2Object(final Properties p, final Object object, String prefix) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                try {
                    String tmp = methodName.substring(4);
                    String first = methodName.substring(3,4);
                    String key = prefix + first.toLowerCase() + tmp;
                    String property = p.getProperty(key);
                    if (property != null) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes != null && parameterTypes.length > 0) {
                            String simpleName = parameterTypes[0].getSimpleName();
                            Object arg = null;
                            if (simpleName.equals("int") || simpleName.equals("Integer")) {
                                arg = Integer.parseInt(property);
                            } else if (simpleName.equals("long") || simpleName.equals("Long")) {
                                arg = Long.parseLong(property);
                            } else if (simpleName.equals("double") || simpleName.equals("Double")) {
                                arg = Double.parseDouble(property);
                            } else if (simpleName.equals("boolean") || simpleName.equals("Boolean")) {
                                arg = Boolean.parseBoolean(property);
                            } else if (simpleName.equals("float") || simpleName.equals("Float")) {
                                arg = Float.parseFloat(property);
                            } else if (simpleName.equals("String")) {
                                arg = property;
                            } else {
                                continue;
                            }
                            method.invoke(object, arg);
                        }
                    }
                }catch (Exception ignore) {

                }
            }
        }
    }

    public static void properties2Object(final Properties p, final Object object) {
        properties2Object(p, object, "");
    }
}
