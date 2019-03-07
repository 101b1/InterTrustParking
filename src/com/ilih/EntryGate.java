package com.ilih;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EntryGate extends Gate<Car> {


    public EntryGate(ConcurrentLinkedQueue<Car> queue, int sleepTime, Parking parking) {
        super(queue, sleepTime, parking);
    }

    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                if (!queue.isEmpty()) {
//                    synchronized (parking){
//                        System.out.printf("Parked car %s to a place %d\n", newCar.getName(), parking.parkCar(newCar));
//                    }
                    try {
                        Thread.sleep(sleepTime * 1000);
                    } catch (InterruptedException e) {
                        System.out.println("EntryGate interrupted");
                    }
                    Car newCar = queue.poll();
                    parking.parkCar(newCar);
                }
            }

        }
    }
}
