package com.ilih;

import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static final int COMMAND_INCORRECT = -1;
    static final int COMMAND_PARK = 1;
    static final int COMMAND_UNPARK_SINGLE = 2;
    static final int COMMAND_UNPARK_MULTI = 3;
    static final int COMMAND_LIST = 4;
    static final int COMMAND_COUNT = 5;
    static final int COMMAND_EXIT = 6;
    static final int COMMAND_HELP = 7;

    static Pattern[] patterns;
    static Scanner scanner;

    public static void main(String[] args) {

        int[] params = new int[2];
        params[0] = 5;
        params[1] = 100;
        patterns = new Pattern[7];
        patterns[0] = Pattern.compile("^(p|park):[0-9]+");
        patterns[1] = Pattern.compile("^(u|unpark):[0-9]+");
        patterns[2] = Pattern.compile("^(u|unpark):\\[.+]");
        patterns[3] = Pattern.compile("^(l|list)");
        patterns[4] = Pattern.compile("^(c|count)");
        patterns[5] = Pattern.compile("^(e|exit)");
        patterns[6] = Pattern.compile("^(h|help)");
        scanner = new Scanner(System.in);
        while (!(parseArgs(args, params) && checkArgs(params))) {
            System.out.println("Please enter entry time and parking size separated with space below:");
            args = scanner.nextLine().split(" ");
        }

        ConcurrentLinkedQueue<Car> entryQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Ticket> exitQueue = new ConcurrentLinkedQueue<>();
        Parking parking = new Parking(params[1]);
        Thread entryGate1 = new Thread(new EntryGate(entryQueue, params[0], parking));
        Thread entryGate2 = new Thread(new EntryGate(entryQueue, params[0], parking));
        Thread exitGate1 = new Thread(new ExitGate(exitQueue, params[0], parking));
        Thread exitGate2 = new Thread(new ExitGate(exitQueue, params[0], parking));
        entryGate1.start();
        entryGate2.start();
        exitGate1.start();
        exitGate2.start();

        int commandStatus;
        String command;

        while (true) {
            System.out.println("Enter your command below. For help, type h.");
            command = scanner.nextLine();
            if ((commandStatus = parseCommand(command)) != COMMAND_INCORRECT) {
                try {
                    String commandValue;
                    String[] commandValueArray;
                    switch (commandStatus) {
                        case COMMAND_PARK:
                            commandValue = command.substring(2);
                            addCarsInQueue(Integer.valueOf(commandValue), entryQueue);
                            break;
                        case COMMAND_UNPARK_SINGLE:
                            commandValue = command.substring(2);
                            unparkSingleCar(new Ticket(Integer.valueOf(commandValue)), exitQueue);
                            break;
                        case COMMAND_UNPARK_MULTI:
                            commandValueArray = command.substring(3, command.length() - 1).split(",");
                            Ticket[] tickets = new Ticket[commandValueArray.length];
                            for (int i = 0; i < commandValueArray.length; i++) {
                                tickets[i] = new Ticket(Integer.valueOf(commandValueArray[i]));
                            }
                            unparkMultipleCars(tickets, exitQueue);
                            break;
                        case COMMAND_LIST:
                            printParkedCars(parking);
                            break;
                        case COMMAND_COUNT:
                            printPlacesLeft(parking);
                            break;
                        case COMMAND_EXIT:
                            scanner.close();
                            exitGate1.interrupt();
                            exitGate2.interrupt();
                            entryGate1.interrupt();
                            entryGate2.interrupt();
                            System.exit(0);
                            break;
                        case COMMAND_HELP:
                            printHelp();
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Wrong command value! See help.");
                }
            } else System.out.println("No such command!");
        }

    }

    private static void printHelp() {
        StringBuilder helpStr = new StringBuilder();
        helpStr.append("ИНСТРУКЦИЯ\n");
        helpStr.append("p:N - (park) припарковать машину, N - количество машин на въез\n");
        helpStr.append("u:N - (unpark) выехать с парковки. N - номер парковочного билета\n");
        helpStr.append("u:[1..n] - (unpark) выехать с парковки нескольким машинам, где в квадратных скобках, через запятую передаются номера парковочных билетов\n");
        helpStr.append("l - (list) список машин, находящихся на парковке\n");
        helpStr.append("c - (count) количество оставшихся мест на парковке\n");
        helpStr.append("e - (exit) выход из приложения\n");
        System.out.println(helpStr.toString());
    }

    private static void printPlacesLeft(Parking parking) {
        System.out.printf("%d places left at parking\n", parking.getNumOfPlaces());
    }

    private static void printParkedCars(Parking parking) {
        parking.printCars();
    }

    private static void unparkMultipleCars(Ticket[] tickets, ConcurrentLinkedQueue<Ticket> exitQueue) {
        for (int i = 0; i < tickets.length; i++) {
            unparkSingleCar(tickets[i], exitQueue);
        }
    }

    private static void unparkSingleCar(Ticket ticket, ConcurrentLinkedQueue<Ticket> queue) {
        if (!queue.contains(ticket)) queue.offer(ticket);
        else System.out.printf("Ticket %d is already in an exit queue!", ticket.getId());
    }

    private static void addCarsInQueue(Integer numOfCars, ConcurrentLinkedQueue<Car> queue) {
        for (int i = 0; i < numOfCars; i++) {
            queue.offer(new Car(scanner.nextLine()));
        }
    }

    private static int parseCommand(String command) {
        for (int i = 0; i < patterns.length; i++) {
            Matcher matcher = patterns[i].matcher(command);
            if (matcher.matches()) return i + 1;
        }
        return COMMAND_INCORRECT;
    }

    private static boolean parseArgs(String[] args, int[] params) {
        if (args.length == 0) {
            System.out.println("Arguments are empty, using default values.");
            return true;
        } else {
            try {
                params[0] = Integer.valueOf(args[0]);
                if (args.length == 2) params[1] = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Arguments must be decimal numbers!");
                return false;
            }
            return true;
        }
    }

    private static boolean checkArgs(int[] params) {
        int sleepTime = params[0];
        int placesAmount = params[1];
        if (sleepTime < 1 || sleepTime > 5) {
            System.out.println("Entry time must be in range 1-5!");
            return false;
        }
        if (placesAmount < 1) {
            System.out.println("Parking size must be bigger than 0!");
            return false;
        }
        return true;
    }
}
