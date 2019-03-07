package com.ilih;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Gate<T> implements Runnable {

    protected final ConcurrentLinkedQueue<T> queue;
    protected int sleepTime;
    protected final Parking parking;

    public Gate(ConcurrentLinkedQueue<T> queue, int sleepTime, Parking parking) {
        this.queue = queue;
        this.sleepTime = sleepTime;
        this.parking = parking;
    }

}
