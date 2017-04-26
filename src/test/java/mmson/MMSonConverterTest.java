package mmson;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test_objects.Student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class MMSonConverterTest {

    private final static int CONCURRENT_THREADS = Runtime.getRuntime().availableProcessors();
    private final static int CONCURRENT_TIMEOUT = 10;

    private MMSonConverter mmson = new MMSonConverter();
    private Gson gson = new Gson();

    @Test
    void toJson() {
        Student s = Student.createRandomStudent(false);
        String json = mmson.toJson(s);
        System.out.println(json);

        Assertions.assertEquals(gson.fromJson(json, Student.class), s);
    }

    @Test
    void toJsonMultiThread() throws InterruptedException{
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            Runnable task = () -> {toJson();};
            tasks.add(task);
        }
        AssertConcurrent.assertConcurrent(tasks, CONCURRENT_TIMEOUT);
    }

}