package mmson;

import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mateusz on 27.04.2017.
 */
public class AssertConcurrent {
    // http://www.planetgeek.ch/2009/08/25/how-to-find-a-concurrency-bug-with-java/
    public static void assertConcurrent(final List<? extends Runnable> runnables, final int maxTimeoutSeconds) throws InterruptedException {
        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        try {
            final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
            final CountDownLatch afterInitBlocker = new CountDownLatch(1);
            final CountDownLatch allDone = new CountDownLatch(numThreads);
            for (final Runnable submittedTestRunnable : runnables) {
                threadPool.submit(new Runnable() {
                    public void run() {
                        allExecutorThreadsReady.countDown();
                        try {
                            afterInitBlocker.await();
                            submittedTestRunnable.run();
                        } catch (final Throwable e) {
                            exceptions.add(e);
                        } finally {
                            allDone.countDown();
                        }
                    }
                });
            }
            // wait until all threads are ready
            Assertions.assertTrue(allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
            // start all test runners
            afterInitBlocker.countDown();
            Assertions.assertTrue(allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
        } finally {
            threadPool.shutdownNow();
        }

        for (Throwable e: exceptions){
            e.printStackTrace();
        }

        Assertions.assertTrue(exceptions.isEmpty());
    }
}
