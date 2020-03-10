package com.jaa603.niso2;

import java.util.ArrayList;

/**
 * Represents a possible solution to a genetic algorithm for the MAXSAT problem.
 */
public class Solution {
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
