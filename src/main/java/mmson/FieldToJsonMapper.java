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
            return "\"\\\"" + f.getName() + "\\\": \"+o." + f.getName();
        }

        if (isClassStringOrChar(fieldClass)){
            return "\"\\\"" + f.getName() + "\\\": \\\"\" +o." + f.getName() + " + \"\\\"\"";
        }

        if (isClassCollection(fieldClass)){
            // TODO: return string that will generate collection JSON
            // TODO: below is some implementation that sadly does not work due to javassist lambda issues
            // TODO: see https://github.com/jboss-javassist/javassist/issues/44
            // String body = "\"\\\"" + f.getName() + "\\\": [\" + o." + f.getName() + ".stream().map(i -> i.toString()).reduce(\"\", String::concat) + \"]\"";
            // System.out.println(body);
            // return body;

            return "\"\\\"" + f.getName() + "\\\": \\\"\" +o." + f.getName() + " + \"\\\"\"";
        }

        if (isClassArray(fieldClass)){
            // TODO: return string that will generate array JSON
            String preamble = "\"\\\"" + f.getName() + "\": [ Arrays.stream(o." + f.getName() + ").";
            System.out.println(preamble);
            return "\"\\\"" + f.getName() + "\\\": \\\"\" +o." + f.getName() + " + \"\\\"\"";
        }

        throw new NotImplementedException("Field class not supported by converter.");
    }
}
