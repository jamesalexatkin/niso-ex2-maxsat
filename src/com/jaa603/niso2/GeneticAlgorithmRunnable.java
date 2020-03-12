package com.jaa603.niso2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;


/**
 * Callable object used to run a genetic algorithm.
 */
public class GeneticAlgorithmRunnable implements Runnable {
    private final int numVariables;
    private final ArrayList<Clause> clauses;
    private final int MAX_GENERATIONS;
    private final int POP_SIZE;
    private final float ELITISM_PROP;
    private final float NORM_FACTOR;
    private final static int POSITIVE_LITERAL_VALUE = 1;

    private boolean done;

    Solution bestSolution;
    int runtime;
    private boolean timeout;

    public GeneticAlgorithmRunnable(ArrayList<Clause> clauses, int numVariables, int maxGenerations, int popSize, float elitismProp, float normFactor) {
        this.clauses = clauses;
        this.numVariables = numVariables;
        this.MAX_GENERATIONS = maxGenerations;
        this.POP_SIZE = popSize;
        this.ELITISM_PROP = elitismProp;
        this.NORM_FACTOR = normFactor;
        this.timeout = false;
        this.done = false;
    }

    @Override
    public void run() {
        performGeneticAlgorithm(clauses, numVariables, MAX_GENERATIONS, POP_SIZE, ELITISM_PROP, NORM_FACTOR);
    }

    public String endAndReturn() {
        timeout = true;

        if (bestSolution == null) {
            bestSolution = generateRandomSolution(clauses, numVariables);
        }
        return runtime + "\t" + bestSolution.getNumSatisfied() + "\t" + bestSolution.getAssignment();
    }

    private void performGeneticAlgorithm(ArrayList<Clause> clauses, int numVariables, final int MAX_GENERATIONS, final int POP_SIZE, final float ELITISM_PROP, final float NORM_FACTOR) {

        // Generate initial population by random
        ArrayList<Solution> pop = generateRandomPop(POP_SIZE, clauses, numVariables);
        // Ranking
        sortSolutions(pop, 0, pop.size() - 1);

        runtime = 0;
        // Initialise to first Solution in population
        bestSolution = pop.get(0);

        int generation = 0;
        while (!timeout && generation < MAX_GENERATIONS) {
            long startTime = new Date().getTime();
            // Selection
            ArrayList<Solution> selectedPop = selectSolutions(pop, POP_SIZE, ELITISM_PROP, NORM_FACTOR);
            long endTime = new Date().getTime();
//            System.out.println("Selection: " + (endTime - startTime));
            if (timeout) {
                return;
            }

            startTime = new Date().getTime();
            // Breeding
            ArrayList<Solution> childSolutions = breedPop(selectedPop, POP_SIZE);
            endTime = new Date().getTime();
//            System.out.println("Breeding: " + (endTime - startTime));
            if (timeout) {
                return;
            }


            startTime = new Date().getTime();
            // Get fitness of children
            for (Solution s : childSolutions) {
                s.calculateFitness(clauses);
            }
            endTime = new Date().getTime();
//            System.out.println("Fitness: " + (endTime - startTime));
            if (timeout) {
                return;
            }


            // Ranking
            startTime = new Date().getTime();
            sortSolutions(childSolutions, 0, childSolutions.size() - 1);
            endTime = new Date().getTime();
//            System.out.println("Ranking: " + (endTime - startTime));
            pop = childSolutions;
            if (timeout) {
                return;
            }

            // Replace best solution if we've found a better one
            if (pop.get(0).getNumSatisfied() > bestSolution.getNumSatisfied()) {
                bestSolution = pop.get(0);
            }
            if (timeout) {
                return;
            }

            // Add 1 to account for starting at 0
            runtime = (generation + 1) * POP_SIZE;
            generation++;
        }
        this.done = true;
    }

    private static ArrayList<Solution> generateRandomPop(int popSize, ArrayList<Clause> clauses, int numVariables) {
        ArrayList<Solution> pop = new ArrayList<>();

        for (int i = 0; i < popSize; i++) {

//            // Build assignment string
//            StringBuilder assignmentStrBldr = new StringBuilder();
//            for (int j = 0; j < numVariables; j++) {
//                // Add 1 to literal value to account for random exclusivity
//                char c = Character.forDigit(r.nextInt(POSITIVE_LITERAL_VALUE + 1), CHAR_NUMBER_BASE);
//                assignmentStrBldr.append(c);
//            }
//
//            String assignment = assignmentStrBldr.toString();
//            int numClausesSatisfied = testClauses(clauses, assignment);
//            Solution newSolution = new Solution(assignment, numClausesSatisfied);
            Solution newSolution = generateRandomSolution(clauses, numVariables);
            pop.add(newSolution);
        }

        return pop;
    }

    private static Solution generateRandomSolution(ArrayList<Clause> clauses, int numVariables) {
        final int CHAR_NUMBER_BASE = 10;
        Random r = new Random();

        // Build assignment string
        StringBuilder assignmentStrBldr = new StringBuilder();
        for (int j = 0; j < numVariables; j++) {
            // Add 1 to literal value to account for random exclusivity
            char c = Character.forDigit(r.nextInt(POSITIVE_LITERAL_VALUE + 1), CHAR_NUMBER_BASE);
            assignmentStrBldr.append(c);
        }

        String assignment = assignmentStrBldr.toString();
        Solution newSolution = new Solution(assignment, 0);
        newSolution.calculateFitness(clauses);
        return newSolution;
    }

    private static ArrayList<Solution> selectSolutions(ArrayList<Solution> pop, int popSize, float elitismProp, float normFactor) {
        ArrayList<Solution> selected = new ArrayList<>();

        // Simple truncation/elitism selection
        int eliteGroupSize = (int) (popSize * elitismProp);

        for (int i = 0; i < eliteGroupSize; i++) {
            selected.add(pop.get(i));
        }

        Random rand = new Random();
        for (int i = eliteGroupSize; i < popSize; i++) {
            float randomProb = rand.nextFloat();
            // Exponential ranking function
            float selectionProb = (float) ((1 - Math.exp(-i)) / normFactor);

            if (randomProb > selectionProb) {
                selected.add(pop.get(i));
            }
        }

        return selected;
    }

    private static ArrayList<Solution> breedPop(ArrayList<Solution> selectedPop, int popSize) {
        ArrayList<Solution> children = new ArrayList<>();
        int numInPop = selectedPop.size();

        Random rand = new Random();
        int i = 0;
        while (children.size() < popSize) {
            // Pick another parent
            int j = rand.nextInt(selectedPop.size());

            Solution child = breedSolution(selectedPop.get(i), selectedPop.get(j));
            children.add(child);

            // Rewind i if we're at last element
            if ((i == numInPop - 1)) {
                i = 0;
            } else {
                i++;
            }
        }

        return children;
    }

    private static Solution breedSolution(Solution parent1, Solution parent2) {
        String assignment1 = parent1.getAssignment();
        String assignment2 = parent2.getAssignment();

        StringBuilder childStrBldr = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < assignment1.length(); i++) {
            // Add 1 to account for random bound exclusivity
            int parentToChooseFrom = rand.nextInt(POSITIVE_LITERAL_VALUE + 1);
            if (parentToChooseFrom == 0) {
                childStrBldr.append(assignment1.charAt(i));
            } else {
                childStrBldr.append(assignment2.charAt(i));
            }
        }
        String childAssignment = childStrBldr.toString();
        return new Solution(childAssignment);
    }


    /**
     * Quicksort implementation for sorting Solutions.
     *
     * @param pop
     * @param start
     * @param end
     */
    private static void sortSolutions(ArrayList<Solution> pop, int start, int end) {
//        if (start < end) {
////            int pivot = partition(pop, start, end);
////
////            sortSolutions(pop, start, pivot - 1);
////            sortSolutions(pop, pivot + 1, end);
////        }
        Collections.sort(pop);
    }

    private static int partition(ArrayList<Solution> pop, int start, int end) {
        Solution pivot = pop.get(end);
        int i = start - 1;

        for (int j = start; j < end; j++) {
            if (pop.get(j).getNumSatisfied() < pivot.getNumSatisfied()) {
                i++;

                Solution swapTemp = pop.get(i);
                pop.set(i, pop.get(j));
                pop.set(j, swapTemp);
            }
        }

        Solution swapTemp = pop.get(i + 1);
        pop.set(i + 1, pop.get(end));
        pop.set(end, swapTemp);

        return i + 1;
    }

    public boolean isDone() {
        return done;
    }
}
