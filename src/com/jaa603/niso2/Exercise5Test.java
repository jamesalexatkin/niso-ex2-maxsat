package com.jaa603.niso2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringJoiner;

import static com.jaa603.niso2.Main.readWdimacsFile;

public class Exercise5Test {

    public static void main(String[] args) throws IOException {
        // Read file
        String wdimacsFilepath = args[1];
        Wdimacs wdimacsFile = readWdimacsFile(wdimacsFilepath);
        ArrayList<Clause> clauses = wdimacsFile.getClauses();

        int numVariables = wdimacsFile.getNumVariables();
        final int TIME_BUDGET = Integer.parseInt(args[3]);
        int repetitions = Integer.parseInt(args[5]);

        // Parameters for algorithm
//        final int POP_MIN = 10;
//        final int POP_STEP = 10;
//        final int POP_ITER = 10;
//        final float ELITISM_MIN = 0.0f;
//        final float ELITISM_STEP = 0.2f;
//        final int ELITISM_ITER = 5;
//        final float NORM_MIN = 0.1f;
//        final float NORM_STEP = 0.2f;
//        final int NORM_ITER = 5;
        final int POP_MIN = 10;
        final int POP_STEP = 10;
        final int POP_ITER = 1;
        final float ELITISM_MIN = 0.0f;
        final float ELITISM_STEP = 0.2f;
        final int ELITISM_ITER = 1;
        final float NORM_MIN = 0.1f;
        final float NORM_STEP = 0.2f;
        final int NORM_ITER = 3;

        int popSize;
        float elitismProp;
        float normFactor;

        // Set up CSV writer
        FileWriter csvWriter = new FileWriter("test.csv");
        csvWriter.append("Population size,Elitism proportion,Normalising factor,Number of clauses satisfied\n");


        for (popSize = POP_MIN; popSize < POP_MIN + (POP_STEP * POP_ITER); popSize += POP_STEP) {

            for (elitismProp = ELITISM_MIN; elitismProp < ELITISM_MIN + (ELITISM_STEP * ELITISM_ITER); elitismProp += ELITISM_STEP) {

                for (normFactor = NORM_MIN; normFactor < NORM_MIN + (NORM_STEP * NORM_ITER); normFactor += NORM_STEP) {

                    int cumulativeTotal = 0;

                    // Perform repetitions of genetic algorithm
                    for (int i = 0; i < repetitions; i++) {

                        GeneticAlgorithmRunnable runnable = new GeneticAlgorithmRunnable(clauses, numVariables, popSize, elitismProp, normFactor);
                        Thread thread = new Thread(runnable);

                        thread.start();

                        //Getting the current date
                        long startTime = new Date().getTime();
                        long currentTime = startTime;
                        long endTime = startTime + (TIME_BUDGET * 1000);

                        // Keep going until we reach time limit
                        while (currentTime < endTime) {
                            currentTime = new Date().getTime();
                        }

                        // Send end signal and return result
                        String result = runnable.endAndReturn();

                        System.out.println(result);

                        String[] tokens = result.split("\t");
                        int namClausesSatisfied = Integer.parseInt(tokens[1]);
                        cumulativeTotal += namClausesSatisfied;
                    }

                    // Produce row and write to CSV
                    int average = cumulativeTotal / repetitions;
                    StringJoiner joiner = new StringJoiner(",");
                    joiner.add(Integer.toString(popSize)).add(Float.toString(elitismProp)).add(Float.toString(normFactor)).add(Integer.toString(average));
                    csvWriter.append(joiner.toString());
                    csvWriter.append("\n");

                    System.out.println("Pop: " + popSize + ", Elite: " + elitismProp + ", Norm: " + normFactor + ", Avg: " + average);
                }
            }
        }

        csvWriter.flush();
        csvWriter.close();
    }
}

