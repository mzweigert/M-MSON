package mmson;

public class ConverterWrapper {

    private JsonConverter converter;

    public ConverterWrapper() {
    }

    public JsonConverter getConverter() {
        return converter;
    }

    public void setConverter(JsonConverter converter) {
        this.converter = converter;
    }
}
