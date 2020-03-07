package com.jaa603.niso2;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

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

                    String wdimacFilepath = args[3];

                    try {
                        File f = new File(wdimacFilepath);
                        Scanner s = new Scanner(f);
                        ArrayList<Clause> clauses = new ArrayList<>();
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

                        String assignment = args[5];
                        int numClausesSatisfied = 0;
                        for (Clause clause : clauses) {
                            clause.assignValues(assignment);
                            try {
                                if (clause.evaluateClause() == 1) {
                                    numClausesSatisfied++;
                                }
                            } catch (Exception e) {
                                System.out.println("Something went wrong");
                            }
                        }
                        System.out.println(Integer.toString(numClausesSatisfied));

                    } catch (FileNotFoundException e) {
                        System.out.println("Couldn't find file " + wdimacFilepath);
                        e.printStackTrace();
                    }

                    break;
                }
                case 3: {
                    // TODO
                    System.out.println("TODO: Question 3");
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


    public static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    public static boolean intToBool(int i) {
        return i == 1;
    }
}

