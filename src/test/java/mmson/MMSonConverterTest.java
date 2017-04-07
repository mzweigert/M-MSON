package mmson;

import org.junit.jupiter.api.Test;
import test_objects.Student;


class MMSonConverterTest {
    @Test
    void toJson() {
        MMSonConverter mmson = new MMSonConverter();
        Student s = new Student();
        System.out.println(mmson.toJson(s));
    }

}