package com.ilih;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ExitGate extends Gate<Ticket> {

    public ExitGate(ConcurrentLinkedQueue<Ticket> queue, int sleepTime, Parking parking) {
        super(queue, sleepTime, parking);
    }

    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                if (!queue.isEmpty()) {
//                    synchronized (parking){
//                        System.out.printf("Unparked car with # %s and ticket %d\n", parking.unparkCar(i).getName(), i);
//                    }
                    Ticket i = queue.poll();
                    parking.unparkCar(i);
                }
            }
        }
    }
}
