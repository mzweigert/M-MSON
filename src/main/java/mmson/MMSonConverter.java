package mmson;

import javassist.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MMSonConverter implements JsonConverter {

    private static final String converterInterfaceFQN = "mmson.JsonConverter";

    private ConcurrentHashMap<Class, JsonConverter> cache;
    private ClassPool pool;

    public MMSonConverter(){
        cache = new ConcurrentHashMap<Class, JsonConverter>();
        pool = ClassPool.getDefault();
    }

    public String toJson(Object o) {
        return getConverter(o.getClass()).toJson(o);
    }

    public JsonConverter getConverter(Class c){
        if(!cache.containsKey(c)){
            try {
                cache.put(c, createConverter(c));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cache.get(c);
    }

    private JsonConverter createConverter(Class c) throws CannotCompileException, NotFoundException,
            IllegalAccessException, InstantiationException {
        CtClass converterClass = pool.makeClass(UUID.randomUUID().toString());
        converterClass.addMethod(CtNewMethod.make(getConversionMethodBody(c), converterClass));
        converterClass.addMethod(CtNewMethod.make(getConversionInterfaceMethodSignature(c), converterClass));
        converterClass.setInterfaces(new CtClass[] { pool.get(converterInterfaceFQN) });

        JsonConverter result = (JsonConverter) pool.toClass(converterClass).newInstance();

        converterClass.detach();

        return result;
    }

    private String getConversionMethodSignature(Class c){
        return "public String toJson(" + c.getName() + " o) { StringBuilder sb = new StringBuilder();";
    }

    private String getConversionInterfaceMethodSignature(Class c) {
        return "public String toJson(Object o){return toJson((" + c.getName() + ")o);}";
    }

    private static String getFieldJson(Field f){
        return FieldToJsonMapper.fieldToJson(f);
    }

    private String getConversionMethodBody(Class c){
        String methodBody = new StringBuilder()
                .append(getConversionMethodSignature(c))
                .append("sb.append(\"{\");")
                .append(Arrays.stream(c.getDeclaredFields()).map(MMSonConverter::getFieldJson)
                        .collect(Collectors.joining(";sb.append(\", \");")))
                .append("sb.append(\"}\");")
                .append("return sb.toString(); }")
                .toString();

        System.out.println(methodBody);

        return methodBody;
    }
}
