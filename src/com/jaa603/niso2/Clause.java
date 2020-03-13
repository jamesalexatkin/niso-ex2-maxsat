package com.jaa603.niso2;

public class Clause {
    private int[] values;

    public Clause(String valueString) {
        String[] tokens = valueString.split("\\s+");
        values = new int[tokens.length - 2];
        // Start at 1 and end at length-1 to ignore first and last elements
        for (int i = 0; i < tokens.length - 2; i++) {
            if (!tokens[i + 1].isBlank()) {
                values[i] = Integer.parseInt(tokens[i + 1]);
            }
        }
    }

    public int evaluate(String assignment) {
        for (int value : values) {
            // Minus 1 as clause values start from 1
            int index = Math.abs(value) - 1;
            // Get 0 or 1 at correct position in assignment string
            char charAtPosition = assignment.charAt(index);
            int assignedValue = Character.getNumericValue(charAtPosition);
//
            // Check if negative literal
            if (value < 0) {
                // Toggle if so
                if (assignedValue == 1) {
                    assignedValue = 0;
                } else {
                    assignedValue = 1;
                }
            }

            // If the new value is 1, we can return 1 immediately as the whole clause is satisfied
            if (assignedValue == 1) {
                return 1;
            }
        }

        // By this point, we have found no satisfied variables and so the clause itself is not satisfied
        return 0;
    }
}
