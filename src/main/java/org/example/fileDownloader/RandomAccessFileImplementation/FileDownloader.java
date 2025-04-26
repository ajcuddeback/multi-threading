package org.example.fileDownloader.RandomAccessFileImplementation;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * This File Downloader uses RandomAccessFile as a first iteration... It's very slow though.
 * Each thread must create a new instance of RandomAccessFile twice in order to read and write.
 * This is causing many threads to just sit waiting. It takes minutes to copy a 300MB file.
 *
 * I previously attempted to use a single instance of RandomAccessFile and share them, but each thread would end up overwriting the pointer of the other
 *
 * Going to now refactor to use a FileChannel. A FileChannel is thread safe and doesn't need to talk to disk on each thread creation
 */
public class FileDownloader {
    private static final int threadCount = 8;
    private static final int bufferSize = 8192;
    public static void main(final String[] args) throws IOException {
        final String filePath = "D:/Testing-Java.txt";
        final File file = new File(filePath);
        final String pathToCopy = "C:\\\\Users\\\\ajcud\\\\Downloads\\\\New.txt";
        final File outputFile = new File(pathToCopy);
        try(
                final RandomAccessFile inputRandomAccessFile = new RandomAccessFile(file, "r");
                final ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        ) {
            final long fileLength = inputRandomAccessFile.length();
            if (fileLength <= bufferSize) {
                pool.execute(new DownloaderThread(0, (int) fileLength, new RandomAccessFile(file, "r"), new RandomAccessFile(outputFile, "rw")));
            } else {
                final int iterations = (int)((fileLength / bufferSize));
                System.out.println(iterations);
                IntStream.range(0, iterations).forEach(iteration -> {
                    final int offset = bufferSize * iteration;
                    try {
                        // TODO: Instead of creating a new instance of RandomAccessFile each time, look at what a FileChannel can do...
                        pool.execute(new DownloaderThread(offset, bufferSize, new RandomAccessFile(file, "r"), new RandomAccessFile(outputFile, "rw")));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });

                // Handle remaining bytes
                int remaining = (int)(fileLength % bufferSize);
                if (remaining > 0) {
                    int offset = iterations * bufferSize;
                    pool.execute(new DownloaderThread(offset, remaining,
                            new RandomAccessFile(file, "r"),
                            new RandomAccessFile(outputFile, "rw")));
                }
            }


            pool.shutdown();
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
