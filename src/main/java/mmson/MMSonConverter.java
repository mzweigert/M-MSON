package mmson;

import javassist.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MMSonConverter implements JsonConverter {

    private static final String converterInterfaceFQN = "mmson.JsonConverter";

    private static ConcurrentHashMap<Class, ConverterWrapper> cache;
    private static ClassPool pool;

    public MMSonConverter(){
        cache = new ConcurrentHashMap<>();
        pool = ClassPool.getDefault();
    }

    public String toJson(Object o) {
        return getConverter(o.getClass()).toJson(o);
    }

    public static JsonConverter getConverter(Class c){
        if(!cache.containsKey(c)){
            try {
                ConverterWrapper wrapper = new ConverterWrapper();
                wrapper.setConverter(createConverter(c));
                cache.put(c, wrapper);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cache.get(c).getConverter();
    }

    private static JsonConverter createConverter(Class c) throws CannotCompileException, NotFoundException,
            IllegalAccessException, InstantiationException {
        CtClass converterClass = pool.makeClass(UUID.randomUUID().toString());
        converterClass.addMethod(CtNewMethod.make(getConversionMethodBody(c), converterClass));
        converterClass.addMethod(CtNewMethod.make(getConversionInterfaceMethodSignature(c), converterClass));
        converterClass.setInterfaces(new CtClass[] { pool.get(converterInterfaceFQN) });

        JsonConverter result = (JsonConverter) pool.toClass(converterClass).newInstance();

        converterClass.detach();

        return result;
    }

    private static String getConversionMethodSignature(Class c){
        return "public String toJson(" + c.getName() + " o) { StringBuilder sb = new StringBuilder();";
    }

    private static String getConversionInterfaceMethodSignature(Class c) {
        return "public String toJson(Object o){return toJson((" + c.getName() + ")o);}";
    }

    private static String getFieldJson(Field f){
        return FieldToJsonMapper.fieldToJson(f);
    }

    private static String getConversionMethodBody(Class c){
        String methodBody = new StringBuilder()
                .append(getConversionMethodSignature(c))
                .append("sb.append(\"{\");")
                .append(Arrays.stream(c.getDeclaredFields()).map(MMSonConverter::getFieldJson)
                        .collect(Collectors.joining(";sb.append(\", \");")))
                .append(";sb.append(\"}\");")
                .append("return sb.toString(); }")
                .toString();

        System.out.println(methodBody);

        return methodBody;
    }

    }
