package mmson;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * Created by Mateusz on 19.04.2017.
 */
public class FieldToJsonMapper {

    private static final Class [] NUMERIC_PRIMITIVES = {
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class
    };

    private static final Class [] NUMERIC_WRAPPERS = {
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class
    };

    public static boolean isClassNumeric(Class c){
        return ArrayUtils.contains(NUMERIC_PRIMITIVES, c) || ArrayUtils.contains(NUMERIC_WRAPPERS, c);
    }

    private static boolean isClassArray(Class c){
        return c.isArray();
    }

    private static boolean isClassCollection(Class c){
        return Collection.class.isAssignableFrom(c);
    }

    public static boolean isClassStringOrChar(Class c){
        return c.equals(String.class) || c.equals(char.class) || c.equals(Character.class);
    }

    public static boolean isClassBoolean(Class c){
        return c.equals(boolean.class) || c.equals(Boolean.class);
    }
    private static String getField(Field f){
        String name = f.getName();
        if(Modifier.isPublic(f.getModifiers())){
            return "o." + name;
        } else {
            try {

                String methodName = f.getType().equals(boolean.class) ? "is" : "get";
                methodName += name.substring(0,1).toUpperCase() + name.substring(1);
                f.getDeclaringClass().getMethod(methodName, (Class<?>[]) null);
                return "o." + methodName + "()";
            } catch (NoSuchMethodException | SecurityException e) {
                return null;
            }
        }
    }
    static String fieldToJson(Field f){
        Class fieldClass = f.getType();
        String code;

        if (FieldToJsonMapper.isClassNumeric(fieldClass) || FieldToJsonMapper.isClassBoolean(fieldClass)){
            code = String.format("sb.append(\"\\\"%s\\\": \"+ %s)", f.getName(), getField(f));
        } else if (FieldToJsonMapper.isClassStringOrChar(fieldClass)){
            code = String.format("sb.append(\"\\\"%s\\\": %s \")", f.getName(), getField(f) == null ? null : "\\\"\" + " + getField(f) + " + \"\\\"");
        } else if (FieldToJsonMapper.isClassCollection(fieldClass)){

            code =    String.format("sb.append(\"\\\"%s\\\": \");", f.getName())
                    + String.format("if(%s != null) {  ", getField(f))
                    + "sb.append(\"[\");"
                    + String.format("for(int i = 0; i < %s.size(); ++i) { ", getField(f))
                    + String.format(" Object object = %s.get(i);", getField(f))
                    + " if (mmson.FieldToJsonMapper.isClassStringOrChar(object.getClass())) { "
                    + " sb.append(\"\\\"\" + object.toString()  + \"\\\"\");"
                    + " } else if (mmson.FieldToJsonMapper.isClassNumeric(object.getClass()) || "
                    + " mmson.FieldToJsonMapper.isClassBoolean(object.getClass())) { "
                    + " sb.append(object.toString());"
                    + " } else { "
                    + " sb.append(mmson.MMSonConverter.getConverter(object.getClass()).toJson(object));"
                    + " } "
                    + String.format("if (i != (%s.size() - 1)) { sb.append(\", \"); } }", getField(f))
                    + "sb.append(\"]\");"
                    + " } else { sb.append(\"null\"); } ;";

        } else if (FieldToJsonMapper.isClassArray(fieldClass)){
            // TODO: handle non-primitive array items
            Class itemClass = fieldClass.getComponentType();
            code =    String.format("sb.append(\"\\\"%s\\\": \");", f.getName())
                    + String.format("if(%s != null) {  ", getField(f))
                    + "sb.append(\"[\");"
                    + String.format("for (int i = 0; i < %s.length; ++i) { ", getField(f));

            if(FieldToJsonMapper.isClassStringOrChar(itemClass)){
                code += String.format("sb.append(\"\\\"\" + %s[i]  + \"\\\"\");", getField(f));
            }
            else if (FieldToJsonMapper.isClassNumeric(itemClass) || FieldToJsonMapper.isClassBoolean(itemClass)){
                code += String.format("sb.append(%s[i]);", getField(f));
            } else {
                code += String.format("sb.append( mmson.MMSonConverter.getConverter(%s.class).toJson(%s[i]));", f.getType().getComponentType().getCanonicalName(), getField(f));
            }
            code += String.format("if (i != (%s.length - 1)) { sb.append(\", \"); }", getField(f))
                    + "};"
                    + "sb.append(\"]\");"
                    + " } else { sb.append(\"null\"); } ;";


        } else {
            code =    String.format("sb.append(\"\\\"%s\\\":  \");", f.getName())
                    + String.format("if(%s != null) {  ", getField(f))
                    + String.format("sb.append(mmson.MMSonConverter.getConverter(%s.class).toJson(%s)); ", f.getType().getCanonicalName(), getField(f))
                    + " } else { sb.append(\"null\"); } ;";
        }

        return code;
    }
}
