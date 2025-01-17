package com.jaa603.niso2;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

import static com.jaa603.niso2.Main.testClauses;

/**
 * Callable object used to run a genetic algorithm.
 */
public class GeneticAlgorithmTask implements Callable<String> {
    private final int numVariables;
    private final ArrayList<Clause> clauses;
    private final int POP_SIZE;
    private final float ELITISM_PROP;
    private final float NORM_FACTOR;
    private final static int POSITIVE_LITERAL_VALUE = 1;

    private boolean timeout;

    public GeneticAlgorithmTask(ArrayList<Clause> clauses, int numVariables, int popSize, float elitismProp, float normFactor) {
        this.clauses = clauses;
        this.numVariables = numVariables;
        this.POP_SIZE = popSize;
        this.ELITISM_PROP = elitismProp;
        this.NORM_FACTOR = normFactor;
        this.timeout = false;
    }

    @Override
    public String call() {
        String result = performGeneticAlgorithm(clauses, numVariables, POP_SIZE, ELITISM_PROP, NORM_FACTOR);
        System.out.println(result);
        return "Ready!";
    }

    private static String performGeneticAlgorithm(ArrayList<Clause> clauses, int numVariables, final int POP_SIZE, final float ELITISM_PROP, final float NORM_FACTOR) {

        // Generate initial population by random
        ArrayList<Solution> pop = generateRandomPop(POP_SIZE, clauses, numVariables);
        // Ranking
        sortSolutions(pop, 0, pop.size() - 1);

        int runtime = 0;
        Solution bestSolution = new Solution("", 0);

        int generation = 1;
        // Perform this until we get interrupted
        while (!Thread.currentThread().isInterrupted()) {
            // Selection
            ArrayList<Solution> selectedPop = selectSolutions(pop, POP_SIZE, ELITISM_PROP, NORM_FACTOR);
            // Breeding
            ArrayList<Solution> childSolutions = breedPop(selectedPop, POP_SIZE);
            // Get fitness of children
            for (Solution s : childSolutions) {
                s.testClauses(clauses);
            }
            // Ranking
            sortSolutions(childSolutions, 0, childSolutions.size() - 1);
            pop = childSolutions;

            // Replace best solution if we've found a better one
            if (pop.get(0).getNumSatisfied() > bestSolution.getNumSatisfied()) {
                bestSolution = pop.get(0);
            }

            runtime = generation * POP_SIZE;
        }

        return runtime + "\t" + bestSolution.getNumSatisfied() + "\t" + bestSolution.getAssignment();
    }

    private static ArrayList<Solution> generateRandomPop(int popSize, ArrayList<Clause> clauses, int numVariables) {
        ArrayList<Solution> pop = new ArrayList<>();

        final int CHAR_NUMBER_BASE = 10;
        Random r = new Random();

        for (int i = 0; i < popSize; i++) {

            // Build assignment string
            StringBuilder assignmentStrBldr = new StringBuilder();
            for (int j = 0; j < numVariables; j++) {
                char c = Character.forDigit(r.nextInt(POSITIVE_LITERAL_VALUE + 1), CHAR_NUMBER_BASE);
                assignmentStrBldr.append(c);
            }

            String assignment = assignmentStrBldr.toString();
            int numClausesSatisfied = testClauses(clauses, assignment);
            Solution newSolution = new Solution(assignment, numClausesSatisfied);
            pop.add(newSolution);
        }

        return pop;
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
     * @param pop
     * @param start
     * @param end
     */
    private static void sortSolutions(ArrayList<Solution> pop, int start, int end) {
        if (start < end) {
            int pivot = partition(pop, start, end);

            sortSolutions(pop, start, pivot - 1);
            sortSolutions(pop, pivot + 1, end);
        }
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


//    public String endAndReturn() {
//        this.timeout = true;
//        return "";
//    }
}
