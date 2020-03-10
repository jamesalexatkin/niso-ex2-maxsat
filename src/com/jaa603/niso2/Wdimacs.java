package com.jaa603.niso2;

import java.util.ArrayList;

/**
 * Represents the data read from a WDIMACS file. Stores the clauses and number of variables.
 */
public class Wdimacs {
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
