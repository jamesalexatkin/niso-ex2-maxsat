package com.jaa603.niso2;

public class Clause {
    private int[] values;
    private int[] assignments;

    public Clause(String valueString) {

        String[] tokens = valueString.split(" ");
        values = new int[tokens.length - 2];
        // Start at 1 and end at length-1 to ignore first and last elements
        for (int i = 0; i < tokens.length - 2; i++) {
            values[i] = Integer.parseInt(tokens[i + 1]);
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
        int i = 0;
        try {
            boolean result = false;
            for (i = 0; i < values.length; i++) {
                // Get bool associated with assignment
                boolean assignedValue = Main.intToBool(assignments[i]);
                // Check if negative literal
                if (values[i] < 0) {
                    assignedValue = !assignedValue;
                }
                // Perform disjunction/or to combine with overall expression
                result = result || assignedValue;
            }
            return Main.boolToInt(result);
        } catch (Exception e) {
            System.out.println("Couldn't find element " + i + " in size " + assignments.length);
            throw e;
        }
    }

}
