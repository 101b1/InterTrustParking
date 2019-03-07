package com.ilih;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Parking {

    public static int MAX_PLACES;
    private int numOfPlaces;
    private ConcurrentLinkedQueue<Ticket> availableTicketsQueue;
    private ConcurrentHashMap<Integer, Car> carsParkedMap;

    public Parking(int places) {
        MAX_PLACES = places;
        numOfPlaces = places;
        availableTicketsQueue = new ConcurrentLinkedQueue<>();
        carsParkedMap = new ConcurrentHashMap<>();
        for (int i = 0; i < MAX_PLACES; i++) {
            availableTicketsQueue.offer(new Ticket(i));
        }

    }

    public synchronized Ticket parkCar(Car carToPark) {
        if (numOfPlaces > 0) {
            Ticket ticket = availableTicketsQueue.poll();
            carsParkedMap.put(ticket.getId(), carToPark);
            numOfPlaces--;
            return ticket;
        } else System.out.println("Parking is full!");
        return null;
    }

    public synchronized Car unparkCar(Ticket unparkTicket) {
        if (carsParkedMap.containsKey(unparkTicket.getId())) {
            availableTicketsQueue.offer(unparkTicket);
            numOfPlaces++;
            return carsParkedMap.remove(unparkTicket.getId());
        } else System.out.printf("No cars with ticket %d\n", unparkTicket.getId());
        return null;
    }

    public synchronized void printCars() {
        System.out.printf("Cars parked %d:\n", MAX_PLACES - numOfPlaces);
        for (Map.Entry<Integer, Car> entry : carsParkedMap.entrySet()) {
            System.out.printf("Car %s at place %d\n", entry.getValue().getName(), entry.getKey());
        }
    }

    public synchronized int getNumOfPlaces() {
        return numOfPlaces;
    }
}
