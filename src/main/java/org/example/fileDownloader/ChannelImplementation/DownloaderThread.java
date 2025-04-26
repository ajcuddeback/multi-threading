package org.example.fileDownloader.ChannelImplementation;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This implementation is great and quick. But we still have to store the bytes in memory
 * I could change this to use the `transferTo` method. This directly moves bytes from one file to another, as I am just copying
 * Only issue, transferTo doesn't guaruntee all bytes will be written. So I need to fetch bytesread from transferTO.
 * If the byte count doesn't match up, move the current position and remaining based on bytes read and continue transfer.
 */

public class DownloaderThread implements Runnable{
    int offset = 0;
    int bufferSize = 0;
    FileChannel inputChannel;
    FileChannel outputChannel;

    public DownloaderThread(final int offset, final int bufferSize, final FileChannel inputChannel, final FileChannel outputChannel) {
        this.offset = offset;
        this.bufferSize = bufferSize;
        this.inputChannel = inputChannel;
        this.outputChannel = outputChannel;
    }

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        try {
            inputChannel.read(buffer, offset);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            outputChannel.write(buffer, offset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
