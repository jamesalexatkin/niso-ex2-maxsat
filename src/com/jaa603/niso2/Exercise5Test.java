package com.jaa603.niso2;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringJoiner;
import java.util.TimeZone;

import static com.jaa603.niso2.Main.readWdimacsFile;

public class Exercise5Test {

    public static void main(String[] args) throws IOException {
        // Read file
        final String WDIMACS_FILEPATH = args[1];
        Wdimacs wdimacsFile = readWdimacsFile(WDIMACS_FILEPATH);
        ArrayList<Clause> clauses = wdimacsFile.getClauses();

        final int NUM_VARIABLES = wdimacsFile.getNumVariables();
        final int TIME_BUDGET = Integer.parseInt(args[3]);
        final int REPETITIONS = Integer.parseInt(args[5]);
        final String OUTPUT_FILE = args[7];

        // Parameters for algorithm
        final int MAX_GENERATIONS = 10;
        final int POP_MIN = 500;
        final int POP_STEP = 50;
        final int POP_ITER = 1;
        final float ELITISM_MIN = 0.5f;
        final float ELITISM_STEP = 0.2f;
        final int ELITISM_ITER = 1;
        final float NORM_MIN = 0.2f;
        final float NORM_STEP = 0.2f;
        final int NORM_ITER = 5;

        int popSize;
        float elitismProp;
        float normFactor;

        // Set up CSV writer
        FileWriter csvWriter = new FileWriter(OUTPUT_FILE);
        csvWriter.append("Population size,Elitism proportion,Normalising factor,Number of clauses satisfied\n");

        int progress = 0;
        int END_PROGRESS = POP_ITER * ELITISM_ITER * NORM_ITER * REPETITIONS;
        long TOTAL_TIME = END_PROGRESS * TIME_BUDGET;
        int timeElapsed = 0;
        long overallStartTime = new Date().getTime();

        final float POP_MAX = POP_MIN + (POP_STEP * POP_ITER);
        for (popSize = POP_MIN; popSize < POP_MAX; popSize += POP_STEP) {

            final float ELITISM_MAX = ELITISM_MIN + (ELITISM_STEP * (ELITISM_ITER));
            for (elitismProp = ELITISM_MIN; elitismProp < ELITISM_MAX; elitismProp += ELITISM_STEP) {

                final float NORM_MAX = NORM_MIN + (NORM_STEP * (NORM_ITER));
                for (normFactor = NORM_MIN; normFactor < NORM_MAX; normFactor += NORM_STEP) {

                    // Use to calculate average
                    int cumulativeTotal = 0;

                    // Perform REPETITIONS of genetic algorithm
                    for (int i = 0; i < REPETITIONS; i++) {

                        GeneticAlgorithmRunnable runnable = new GeneticAlgorithmRunnable(clauses, NUM_VARIABLES, MAX_GENERATIONS, popSize, elitismProp, normFactor);
                        Thread thread = new Thread(runnable);

                        thread.start();

                        //Getting the current date
                        long startTime = new Date().getTime();
                        long currentTime = startTime;
                        long endTime = startTime + (TIME_BUDGET * 1000); //ms

                        // Keep going until we reach time limit
                        while (currentTime < endTime && !runnable.isDone()) {
                            currentTime = new Date().getTime();
                        }

                        // Send end signal and return result
                        String result = runnable.endAndReturn();

//                        System.out.println(result);
                        String[] tokens = result.split("\t");
                        int numClausesSatisfied = Integer.parseInt(tokens[1]);
                        cumulativeTotal += numClausesSatisfied;

                        // Produce row and write to CSV
                        int average = cumulativeTotal / REPETITIONS;
                        StringJoiner joiner = new StringJoiner(",");
                        joiner.add(Integer.toString(popSize)).add(Float.toString(elitismProp)).add(Float.toString(normFactor)).add(Integer.toString(numClausesSatisfied));
                        csvWriter.append(joiner.toString());
                        csvWriter.append("\n");

                        progress++;
                        timeElapsed += TIME_BUDGET;
                        // * 1000 to convert seconds to ms
                        long timeRemaining = (TOTAL_TIME - timeElapsed) * 1000L;
                        System.out.print("\r" + progress + "/" + END_PROGRESS + " " + formatTime(timeRemaining) + " " + formatTime(currentTime - overallStartTime) + " Last result: " + numClausesSatisfied);
                    }

//                    // Produce row and write to CSV
//                    int average = cumulativeTotal / REPETITIONS;
//                    StringJoiner joiner = new StringJoiner(",");
//                    joiner.add(Integer.toString(popSize)).add(Float.toString(elitismProp)).add(Float.toString(normFactor)).add(Integer.toString(average));
//                    csvWriter.append(joiner.toString());
//                    csvWriter.append("\n");

//                    System.out.println("\nPop: " + popSize + ", Elite: " + elitismProp + ", Norm: " + normFactor + ", Avg: " + average);
                }
            }
        }

        csvWriter.flush();
        csvWriter.close();
    }

    private static String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedTime = sdf.format(time);
        return "(" + formattedTime + ")";
    }
}

