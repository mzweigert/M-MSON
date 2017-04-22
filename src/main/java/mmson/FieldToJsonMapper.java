package mmson;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Created by Mateusz on 19.04.2017.
 */
class FieldToJsonMapper {

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

    private static boolean isClassNumeric(Class c){
        return ArrayUtils.contains(NUMERIC_PRIMITIVES, c) || ArrayUtils.contains(NUMERIC_WRAPPERS, c);
    }

    private static boolean isClassArray(Class c){
        return c.isArray();
    }

    private static boolean isClassCollection(Class c){
        return Collection.class.isAssignableFrom(c);
    }

    private static boolean isClassStringOrChar(Class c){
        return c.equals(String.class) || c.equals(char.class) || c.equals(Character.class);
    }

    private static boolean isClassBoolean(Class c){
        return c.equals(boolean.class) || c.equals(Boolean.class);
    }

    static String fieldToJson(Field f){
        Class fieldClass = f.getType();

        if (isClassNumeric(fieldClass) || isClassBoolean(fieldClass)){
            return String.format("sb.append(\"\\\"%s\\\": \"+o.%s)", f.getName(), f.getName());
        }

        if (isClassStringOrChar(fieldClass)){
            return String.format("sb.append(\"\\\"%s\\\": \\\"\" +o.%s + \"\\\"\")", f.getName(), f.getName());
        }

        if (isClassCollection(fieldClass)){
            // TODO: detect collection item class and handle non primitive fields
            return String.format("sb.append(\"\\\"%s\\\": \");", f.getName())
                    + "sb.append(\"[\");"
                    + String.format("for (int i = 0; i < o.%s.size(); ++i) { ", f.getName())
                    + String.format("sb.append(\"\\\"\" + o.%s.get(i).toString()  + \"\\\"\");", f.getName())
                    + String.format("if (i != (o.%s.size() - 1)) { sb.append(\", \"); }", f.getName())
                    + "};"
                    + "sb.append(\"]\");";
        }

        if (isClassArray(fieldClass)){
            // TODO: handle non-primitive array items
            Class itemClass = fieldClass.getComponentType();
            if(isClassStringOrChar(itemClass)){
                return String.format("sb.append(\"\\\"%s\\\": \");", f.getName())
                        + "sb.append(\"[\");"
                        + String.format("for (int i = 0; i < o.%s.length; ++i) { ", f.getName())
                        + String.format("sb.append(\"\\\"\" + o.%s[i]  + \"\\\"\");", f.getName())
                        + String.format("if (i != (o.%s.length - 1)) { sb.append(\", \"); }", f.getName())
                        + "};"
                        + "sb.append(\"]\");";
            }
            else if (isClassNumeric(itemClass) || isClassBoolean(itemClass)){
                return String.format("sb.append(\"\\\"%s\\\": \");", f.getName())
                        + "sb.append(\"[\");"
                        + String.format("for (int i = 0; i < o.%s.length; ++i) { ", f.getName())
                        + String.format("sb.append(o.%s[i]);", f.getName())
                        + String.format("if (i != (o.%s.length - 1)) { sb.append(\", \"); }", f.getName())
                        + "};"
                        + "sb.append(\"]\");";
            }
            throw new NotImplementedException("Array item class not supported by mapper.");

        }

        throw new NotImplementedException("Field class not supported by mapper.");
    }
}
