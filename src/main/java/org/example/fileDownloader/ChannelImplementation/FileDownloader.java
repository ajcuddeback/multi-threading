package org.example.fileDownloader.ChannelImplementation;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * This implementation is sooo much better. Because we open a channel, and a channel is thread safe...
 * It takes less than a second to copy over a file that's around 300MB in size with only 8 threads.
 * This heavily reduces IO overhead because we don't have to open a new connection to the file for each thread to get around race conditions
 * But, it could be even better. Look at notes in FileDownloader of this package.
 */
public class FileDownloader {
    private static final int threadCount = 100;
    private static final int bufferSize = 8192;
    public static void main(final String[] args) throws IOException {
        final String filePath = "D:/Testing-Java.txt";
        final File file = new File(filePath);
        final String pathToCopy = "C:\\\\Users\\\\ajcud\\\\Downloads\\\\New.txt";
        final File outputFile = new File(pathToCopy);
        try(
                final RandomAccessFile inputRandomAccessFile = new RandomAccessFile(file, "r");
                final RandomAccessFile outputRandomAccessFile = new RandomAccessFile(outputFile, "rw");
                final ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        ) {
            final FileChannel inputChannel = inputRandomAccessFile.getChannel();
            final FileChannel outputChannel = outputRandomAccessFile.getChannel();
            final long fileLength = inputRandomAccessFile.length();
            if (fileLength <= bufferSize) {
                pool.execute(new DownloaderThread(0, (int) fileLength, inputChannel, outputChannel));
            } else {
                final int iterations = (int)((fileLength / bufferSize));
                System.out.println(iterations);
                IntStream.range(0, iterations).forEach(iteration -> {
                    final int offset = bufferSize * iteration;
                    pool.execute(new DownloaderThread(offset, bufferSize, inputChannel, outputChannel));
                });

                // Handle remaining bytes
                int remaining = (int)(fileLength % bufferSize);
                if (remaining > 0) {
                    int offset = iterations * bufferSize;
                    pool.execute(new DownloaderThread(offset, remaining,
                            inputChannel,
                            outputChannel));
                }
            }


            pool.shutdown();
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
