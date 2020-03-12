package com.jaa603.niso2;

import java.util.ArrayList;

/**
 * Represents a possible solution to a genetic algorithm for the MAXSAT problem.
 */
public class Solution implements Comparable<Solution> {
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

    @Override
    public int compareTo(Solution s) {
        return s.numSatisfied - this.numSatisfied;
    }

    public void calculateFitness(ArrayList<Clause> clauses) {
        int numClausesSatisfied = 0;
        for (Clause clause : clauses) {
            // Check if satisfied
            if (clause.evaluate(assignment) == 1) {
                numClausesSatisfied++;
            }
        }
        this.numSatisfied = numClausesSatisfied;
    }
}
