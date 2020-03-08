package com.jaa603.niso2;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    final static int POSITIVE_LITERAL_VALUE = 1;

    static class GeneticAlgorithmTask implements Callable<String> {
        private ArrayList<Clause> clauses;
        private int timeBudget;

        public GeneticAlgorithmTask(ArrayList<Clause> clauses, int timeBudget) {
            this.clauses = clauses;
            this.timeBudget = timeBudget;
        }

        @Override
        public String call() throws Exception {
            String result = performGeneticAlgorithm(clauses, timeBudget);
            System.out.println(result);
            return "Ready!";
        }
    }

    public static void main(String[] args) {
        if (args[0].equals("-question")) {
            switch (Integer.parseInt(args[1])) {
                case 1: {
                    String clause = args[3];
                    String assignment = args[5];
                    Clause c = new Clause(clause);
                    c.assignValues(assignment);
                    try {
                        int result = c.evaluateClause();
                        System.out.println(Integer.toString(result));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }
                case 2: {

                    String wdimacsFilepath = args[3];
                    ArrayList<Clause> clauses = readWdimacsFile(wdimacsFilepath);

                    String assignment = args[5];
                    int numClausesSatisfied = testClauses(clauses, assignment);
                    System.out.println(Integer.toString(numClausesSatisfied));


                    break;
                }
                case 3: {
                    String wdimacsFilepath = args[3];
                    ArrayList<Clause> clauses = readWdimacsFile(wdimacsFilepath);
                    int timeBudget = Integer.parseInt(args[5]);
                    int repetitions = Integer.parseInt(args[7]);
                    for (int i = 0; i < repetitions; i++) {

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Future<String> future = executor.submit(new GeneticAlgorithmTask(clauses, timeBudget));

                        try {
                            // Start running task for number of seconds specified in time budget
                            System.out.println(future.get(timeBudget, TimeUnit.SECONDS));
                        } catch (TimeoutException e) {
                            // Cancel when we run out of time
                            future.cancel(true);
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }

                        executor.shutdownNow();
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

    private static String performGeneticAlgorithm(ArrayList<Clause> clauses, int timeBudget) {
        // Parameters for algorithm
        final int POP_SIZE = 5;
        final float ELITISM_PROP = 0.3f;
        final float NORM_FACTOR = 0.5f;

        // Generate initial population by random
        ArrayList<Solution> pop = generateRandomPop(POP_SIZE, clauses);
        // Ranking
        sortSolutions(pop, 0, pop.size() - 1);

        int runtime = 0;
        Solution bestSolution = new Solution("", 0);

        int generation = 1;
        // Perform this until we get interrupted
        while (!Thread.interrupted()) {
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

        return runtime + "\t" + String.valueOf(bestSolution.getNumSatisfied()) + "\t" + bestSolution.getAssignment();
    }

    private static ArrayList<Solution> breedPop(ArrayList<Solution> selectedPop, int pop_size) {
        ArrayList<Solution> children = new ArrayList<>();
        int numInPop = selectedPop.size();

        Random rand = new Random();
        int i = 0;
        while (children.size() < pop_size) {
            int j = i;
            while (j == i) {
                j = rand.nextInt(selectedPop.size());
            }
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

    private static ArrayList<Solution> selectSolutions(ArrayList<Solution> pop, int pop_size, float elitism_prop, float norm_factor) {
        ArrayList<Solution> selected = new ArrayList<>();

        // Simple truncation/elitism selection
        int eliteGroupSize = (int) (pop_size * elitism_prop);

        for (int i = 0; i < eliteGroupSize; i++) {
            selected.add(pop.get(i));
        }

        Random rand = new Random();
        for (int i = eliteGroupSize; i < pop_size; i++) {
            float randomProb = rand.nextFloat();
            // Exponential ranking function
            float selectionProb = (float) ((1 - Math.exp(-i)) / norm_factor);

            if (randomProb > selectionProb) {
                selected.add(pop.get(i));
            }
        }

        return selected;
    }

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

    private static ArrayList<Solution> generateRandomPop(int popSize, ArrayList<Clause> clauses) {
        ArrayList<Solution> pop = new ArrayList<>();
        int assignmentLength = clauses.get(0).getValues().size();

        final int CHAR_NUMBER_BASE = 10;
        Random r = new Random();

        for (int i = 0; i < popSize; i++) {

            // Build assignment string
            StringBuilder assignmentStrBldr = new StringBuilder();
            for (int j = 0; j < assignmentLength; j++) {
                char c = Character.forDigit(r.nextInt(POSITIVE_LITERAL_VALUE + 1), CHAR_NUMBER_BASE);
                assignmentStrBldr.append(c);
            }

            String assignment = assignmentStrBldr.toString();
            int numClausesSatisfied = testClauses(clauses, assignment);
            Solution newSolution = new Solution(assignment.toString(), numClausesSatisfied);
            pop.add(newSolution);
        }

        return pop;
    }

    public static int testClauses(ArrayList<Clause> clauses, String assignment) {
        int numClausesSatisfied = 0;
        for (Clause clause : clauses) {
            clause.assignValues(assignment);
            try {
                // Check if satisfied
                if (clause.evaluateClause() == 1) {
                    numClausesSatisfied++;
                }
            } catch (Exception e) {
                System.out.println("Something went wrong");
            }
        }
        return numClausesSatisfied;
    }

    private static ArrayList<Clause> readWdimacsFile(String wdimacsFilepath) {
        ArrayList<Clause> clauses = new ArrayList<>();
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
                        break;
                    // Clause
                    default:
                        Clause clauseFromLine = new Clause(line);
                        clauses.add(clauseFromLine);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file " + wdimacsFilepath);
            e.printStackTrace();
        }
        return clauses;
    }

    private static class Clause {

        private ArrayList<Integer> values;
        private ArrayList<Integer> assignments;

        public Clause(String valueString) {
            values = new ArrayList<Integer>();
            String[] tokens = valueString.split(" ");
            for (int i = 1; i < tokens.length - 1; i++) {
                values.add(Integer.parseInt(tokens[i]));
            }
        }

        public ArrayList<Integer> getValues() {
            return values;
        }

        public ArrayList<Integer> getAssignments() {
            return assignments;
        }

        public void assignValues(String assignmentString) {
            assignments = new ArrayList<>();
            for (Integer value : values) {
                // Minus 1 as clause values start from 1
                int variableIndex = Math.abs(value) - 1;
                char charAtPosition = assignmentString.charAt(variableIndex);
                int valueToAssign = Character.getNumericValue(charAtPosition);
                assignments.add(valueToAssign);
            }
        }

        public int evaluateClause() throws Exception {
            if (values.size() == assignments.size()) {
                boolean result = false;
                for (int i = 0; i < values.size(); i++) {
                    boolean value = intToBool(assignments.get(i));
                    if (values.get(i) < 0) {
                        value = !value;
                    }
                    result = result || value;
                }
                return boolToInt(result);
            } else {
                throw new Exception("Not all variables have been assigned");
            }
        }
    }

    private static class Solution {
        private String assignment;
        private int numSatisfied;

        public Solution(String assignment, int numSatisfied) {
            this.assignment = assignment;
            this.numSatisfied = numSatisfied;
        }

        public Solution(String assignment) {
            this.assignment = assignment;
            this.numSatisfied = 0;
        }

        public String getAssignment() {
            return assignment;
        }

        public int getNumSatisfied() {
            return numSatisfied;
        }

        public void testClauses(ArrayList<Main.Clause> clauses) {
            this.numSatisfied = Main.testClauses(clauses, this.assignment);
        }
    }

    public static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    public static boolean intToBool(int i) {
        return i == 1;
    }
}

