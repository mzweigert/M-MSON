package mmson;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test_objects.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mateusz on 26.04.2017.
 */
public class MMsonPerformanceTest {

    private final static int CONCURRENT_THREADS = Runtime.getRuntime().availableProcessors();
    private final static int CONCURRENT_TIMEOUT = 10;

    private MMSonConverter mmson = new MMSonConverter();
    private Gson gson = new Gson();

    @BeforeEach
    void generateMMsonCode(){
        Student s = Student.createRandomStudent(false);
        String json = mmson.toJson(s);
        System.out.println(json);
    }

    @Test
    void testSingleThreadPerformance() {
        Student s = Student.createRandomStudent(false);
        long startTime = System.nanoTime();
        String mmsonJson = mmson.toJson(s);
        long stopTime = System.nanoTime();
        long mmsonElapsedTime = stopTime - startTime;

        Assertions.assertEquals(gson.fromJson(mmsonJson, Student.class), s);

        startTime = System.nanoTime();
        gson.toJson(s);
        stopTime = System.nanoTime();
        long gsonElapsedTime = stopTime - startTime;

        System.out.println("MMson time: " + mmsonElapsedTime);
        System.out.println("Gson time: " + gsonElapsedTime);

        Assertions.assertTrue(mmsonElapsedTime < gsonElapsedTime);
    }

    @Test
    void testMultiThreadedPerformance() throws InterruptedException{
        Student s = Student.createRandomStudent(false);

        final ExecutorService threadPool = Executors.newFixedThreadPool(CONCURRENT_THREADS);

        List<Callable<String>> mmsonTasks = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            Callable<String> task = () -> {return mmson.toJson(s);};
            mmsonTasks.add(task);
        }
        long startTime = System.nanoTime();
        threadPool.invokeAll(mmsonTasks);
        long mmsonElapsedTime = System.nanoTime() - startTime;

        List<Callable<String>> gsonTasks = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            Callable<String> task = () -> {return gson.toJson(s);};
            gsonTasks.add(task);
        }
        startTime = System.nanoTime();
        threadPool.invokeAll(gsonTasks);
        long gsonElapsedTime = System.nanoTime() - startTime;

        System.out.println("MMson time: " + mmsonElapsedTime);
        System.out.println("Gson time: " + gsonElapsedTime);

        Assertions.assertTrue(mmsonElapsedTime < gsonElapsedTime);
    }
}
