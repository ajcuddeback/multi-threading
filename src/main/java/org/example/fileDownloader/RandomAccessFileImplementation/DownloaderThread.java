package org.example.fileDownloader.RandomAccessFileImplementation;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DownloaderThread implements Runnable{
    int offset = 0;
    int bufferSize = 0;
    RandomAccessFile inputFile;
    RandomAccessFile outputFile;

    public DownloaderThread(final int offset, final int bufferSize, final RandomAccessFile inputFile, final RandomAccessFile outputFile) {
        this.offset = offset;
        this.bufferSize = bufferSize;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    @Override
    public void run() {
        byte[] bytes = new byte[bufferSize];

        try {
            inputFile.seek(offset);
            int bytesRead = 0;
            while (bytesRead < bufferSize) {
                int result = inputFile.read(bytes, bytesRead, bufferSize - bytesRead);
                if (result == -1) {
                    break;  // End of file reached
                }
                bytesRead += result;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            outputFile.seek(offset);
            outputFile.write(bytes, 0, bufferSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
