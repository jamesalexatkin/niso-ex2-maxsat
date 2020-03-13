package com.jaa603.niso2;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Main class used to run Exercises 1, 2 & 3
 */
public class Main {

    public static void main(String[] args) {
        if (args[0].equals("-question")) {
            switch (Integer.parseInt(args[1])) {
                case 1: {
                    String clause = args[3];
                    String assignment = args[5];
                    Clause c = new Clause(clause);
                    System.out.println(c.evaluate(assignment));
                    break;
                }
                case 2: {
                    // Read file
                    String wdimacsFilepath = args[3];
                    Wdimacs wdimacsFile = readWdimacsFile(wdimacsFilepath);
                    ArrayList<Clause> clauses = wdimacsFile.getClauses();

                    String assignment = args[5];
                    int numClausesSatisfied = testClauses(clauses, assignment);
                    System.out.println(numClausesSatisfied);

                    break;
                }
                case 3: {
                    // Read file
                    String wdimacsFilepath = args[3];
                    Wdimacs wdimacsFile = readWdimacsFile(wdimacsFilepath);
                    ArrayList<Clause> clauses = wdimacsFile.getClauses();

                    int numVariables = wdimacsFile.getNumVariables();
                    final int TIME_BUDGET = Integer.parseInt(args[5]);
                    int repetitions = Integer.parseInt(args[7]);

                    // Parameters for algorithm
                    final int MAX_GENERATIONS = 10;
                    final int POP_SIZE = 5;
                    final float ELITISM_PROP = 0.3f;
                    final float NORM_FACTOR = 0.5f;

                    // Perform repetitions of genetic algorithm
                    for (int i = 0; i < repetitions; i++) {

                        GeneticAlgorithmRunnable runnable = new GeneticAlgorithmRunnable(clauses, numVariables, MAX_GENERATIONS, POP_SIZE, ELITISM_PROP, NORM_FACTOR);
                        Thread thread = new Thread(runnable);

                        thread.start();

                        //Getting the current date
                        long startTime = new Date().getTime();
                        long currentTime = startTime;
                        long endTime = startTime + (TIME_BUDGET * 1000);

                        while (currentTime < endTime && !runnable.isDone()) {
                            currentTime = new Date().getTime();
                        }

                        String result = runnable.endAndReturn();
                        System.out.println(result);
                    }
                    break;
                }
                default:
                    System.out.println("Unknown question number");
                    break;
            }
        } else {
            System.out.println("Malformed input");
        }

    }


    public static int testClauses(ArrayList<Clause> clauses, String assignment) {
        int numClausesSatisfied = 0;
        for (Clause clause : clauses) {
            numClausesSatisfied += clause.evaluate(assignment);
        }
        return numClausesSatisfied;
    }

    static Wdimacs readWdimacsFile(String wdimacsFilepath) {
        ArrayList<Clause> clauses = new ArrayList<>();
        int numVariables = 0;
        try {
            File f = new File(wdimacsFilepath);
            Scanner s = new Scanner(f);

            while (s.hasNextLine()) {
                String line = s.nextLine();
                switch (line.charAt(0)) {
                    // Comment
                    case 'c':
                        break;
                    // Parameters
                    case 'p':
                        String[] tokens = line.split(" ");
                        numVariables = Integer.parseInt(tokens[2]);
                        break;
                    // Clause
                    default:
                        if (Character.isDigit(line.charAt(0))) {
                            Clause clauseFromLine = new Clause(line);
                            clauses.add(clauseFromLine);
                        }
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file " + wdimacsFilepath);
            e.printStackTrace();
        }
        return new Wdimacs(clauses, numVariables);
    }
}

