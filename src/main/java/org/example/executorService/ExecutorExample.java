package org.example.executorService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ExecutorExample {
    public static void main(final String[] args) {
        final LocalDateTime startTime = LocalDateTime.now();
        try (final ExecutorService pool = Executors.newFixedThreadPool(200)) {
            Integer count = 0;

            IntStream.range(0, 1000).forEach(ignored -> {
                pool.execute(new Counter(count));
            });

            pool.shutdown();
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        final LocalDateTime endTime = LocalDateTime.now();

        final long totalTime = ChronoUnit.SECONDS.between(startTime, endTime);
        System.out.println("Took " + totalTime + " seconds");
    }
}
