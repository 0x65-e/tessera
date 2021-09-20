package com.arucane.diceroller.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class to encapsulate all sources of randomness for the app. Mostly static methods to operate on
 * DiceGroups to produce (pseudo-)random die rolls
 */
public final class RollEngine {

    // One source of randomness for the entire program
    // allows for modification in the future to a "better" source of randomness
    static Random rand = new Random();

    // No need to have an instance of this class
    private RollEngine() { }

    /**
     * Roll a DiceGroup and return the results as a List of Integers
     * @param dice DiceGroup with parameters of the dice to be rolled
     * @return List of individual dice results
     */
    public static List<Integer> results(DiceGroup dice) {
        List<Integer> results = new ArrayList<>(Math.abs(dice.getNumRolls()));
        int result;
        if (dice.getMaxVal() != 0) for (int i = 0; i < Math.abs(dice.getNumRolls()); i++) {
            //noinspection StatementWithEmptyBody
            while ((result = dice.applyMutators(roll(dice.getMaxVal()))) == -1); // This could get trapped in an infinite loop if the user isn't careful
            results.add(result);
        }
        // Remember to apply any relevant filters the DiceGroup requires
        results = dice.applyFilters(results);
        return results;
    }

    /**
     * Roll a List of DiceGroups and return a List with a separate List for the  results of each one,
     * as though they had been individually rolled with results(group)
     * @param groups List of DiceGroups to roll
     * @return List of die results. Guaranteed to be the same length as groups.
     */
    public static List<List<Integer>> results(List<DiceGroup> groups) {
        List<List<Integer>> results = new ArrayList<>();
        for (DiceGroup group : groups) {
            // Add results from each die group to the overall results
            results.add(results(group));
        }
        return results;
    }

    /**
     * Convenience method for summing the elements in a List of die results
     * @param lst List of integers to sum
     * @return Sum of all elements
     */
    public static int sum(List<Integer> lst) {
        int sum = 0;
        for (Integer i : lst) sum += i;
        return sum;
    }

    ////////////////////////////
    // Roll Function
    ////////////////////////////

    private static int roll(int max) {
        return rand.nextInt(Math.abs(max)) + 1;
    }
}
