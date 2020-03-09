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
        private final int numVariables;
        private final ArrayList<Clause> clauses;

        public GeneticAlgorithmTask(ArrayList<Clause> clauses, int numVariables) {
            this.clauses = clauses;
            this.numVariables = numVariables;
        }

        @Override
        public String call() {
            String result = performGeneticAlgorithm(clauses, numVariables);
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
                        System.out.println(result);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
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
                    int timeBudget = Integer.parseInt(args[5]);
                    int repetitions = Integer.parseInt(args[7]);

                    // Perform repetitions of genetic algorithm
                    for (int i = 0; i < repetitions; i++) {

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Future<String> future = executor.submit(new GeneticAlgorithmTask(clauses, numVariables));

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

    private static String performGeneticAlgorithm(ArrayList<Clause> clauses, int numVariables) {
        // Parameters for algorithm
        final int POP_SIZE = 5;
        final float ELITISM_PROP = 0.3f;
        final float NORM_FACTOR = 0.5f;

        // Generate initial population by random
        ArrayList<Solution> pop = generateRandomPop(POP_SIZE, clauses, numVariables);
//        System.out.println("pop generated");
        // Ranking
        sortSolutions(pop, 0, pop.size() - 1);

        int runtime = 0;
        Solution bestSolution = new Solution("", 0);

        int generation = 1;
        // Perform this until we get interrupted
        while (!Thread.currentThread().isInterrupted()) {
            // Selection
            ArrayList<Solution> selectedPop = selectSolutions(pop, POP_SIZE, ELITISM_PROP, NORM_FACTOR);
//            System.out.println("selected pop");
            // Breeding
            ArrayList<Solution> childSolutions = breedPop(selectedPop, POP_SIZE);
//            System.out.println("children created");
            // Get fitness of children
            for (Solution s : childSolutions) {
                s.testClauses(clauses);
            }
//            System.out.println("children tested");
            // Ranking
            sortSolutions(childSolutions, 0, childSolutions.size() - 1);
            pop = childSolutions;
//            System.out.println("children ranked");

            // Replace best solution if we've found a better one
            if (pop.get(0).getNumSatisfied() > bestSolution.getNumSatisfied()) {
                bestSolution = pop.get(0);
            }

            runtime = generation * POP_SIZE;
//            System.out.println(runtime);
        }

//        System.out.println("BLAH");


        return runtime + "\t" + bestSolution.getNumSatisfied() + "\t" + bestSolution.getAssignment();
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
                e.printStackTrace();
            }
        }
        return numClausesSatisfied;
    }

    private static Wdimacs readWdimacsFile(String wdimacsFilepath) {
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
                        Clause clauseFromLine = new Clause(line);
                        clauses.add(clauseFromLine);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file " + wdimacsFilepath);
            e.printStackTrace();
        }
        return new Wdimacs(clauses, numVariables);
    }

    /**
     * Represents a clause of literals which combine with disjunction.
     */
    private static class Clause {

        private int[] values;
        private int[] assignments;

        public Clause(String valueString) {

            String[] tokens = valueString.split(" ");
            values = new int[tokens.length - 2];
            // Start at 1 and end at length-1 to ignore first and last elements
            for (int i = 0; i < tokens.length - 2; i++) {
                values[i] = Integer.parseInt(tokens[i+1]);
            }
        }

        public void assignValues(String assignmentString) {
            assignments = new int[values.length];

//            System.out.println(Arrays.toString(values));
            for (int i = 0; i < values.length; i++) {
                int value = values[i];
                // Minus 1 as clause values start from 1
                int variableIndex = Math.abs(value) - 1;
                // Get 0 or 1 at correct position in assignment string
                char charAtPosition = assignmentString.charAt(variableIndex);
                int valueToAssign = Character.getNumericValue(charAtPosition);
                assignments[i] = (valueToAssign);
            }
        }

        public int evaluateClause() {
//            if (values.size() == assignments.size()) {
            int i = 0;
            try {
                boolean result = false;
                for (i = 0; i < values.length; i++) {
                    // Get bool associated with assignment
                    boolean assignedValue = intToBool(assignments[i]);
                    // Check if negative literal
                    if (values[i] < 0) {
                        assignedValue = !assignedValue;
                    }
                    // Perform disjunction/or to combine with overall expression
                    result = result || assignedValue;
                }
                return boolToInt(result);
            } catch (Exception e) {
                System.out.println("Couldn't find element " + i + " in size " + assignments.length);
                throw e;
            }
//            } else {
//                System.out.println(values);
//                System.out.println(assignments);
//                throw new Exception("Not all variables have been assigned " + values.size() + " " + assignments.size());
//            }
        }
    }

    /**
     * Represents a possible solution to a genetic algorithm for the MAXSAT problem.
     */
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

        public void testClauses(ArrayList<Clause> clauses) {
            this.numSatisfied = Main.testClauses(clauses, this.assignment);
        }
    }

    /**
     * Represents the data read from a WDIMACS file. Stores the clauses and number of variables.
     */
    public static class Wdimacs {
        private final ArrayList<Clause> clauses;
        private final int numVariables;

        public Wdimacs(ArrayList<Clause> clauses, int numVariables) {
            this.clauses = clauses;
            this.numVariables = numVariables;
        }

        public ArrayList<Clause> getClauses() {
            return clauses;
        }

        public int getNumVariables() {
            return numVariables;
        }
    }

    public static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    public static boolean intToBool(int i) {
        return i == 1;
    }
}

