package org.example.executorService;

import static java.lang.Thread.sleep;

public class Counter implements Runnable {
    private Integer instanceCount;
    public Counter(Integer count) {
        this.instanceCount = count;
    }


    @Override
    public void run() {
        instanceCount++;
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Count is happening now! " + instanceCount);
    }
}
