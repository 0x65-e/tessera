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
        for (int i = 0; i < Math.abs(dice.getNumRolls()); i++) results.add(roll(dice.getMax()));
        return results;
    }

    public static List<List<Integer>> results(List<DiceGroup> groups) {
        List<List<Integer>> results = new ArrayList<>();
        for (DiceGroup group : groups) {
            // Add results from each die group to the overall results
            results.add(results(group));
        }
        return results;
    }

    public static int sum(List<Integer> lst) {
        int sum = 0;
        for (Integer i : lst) sum += i;
        return sum;
    }

    ////////////////////////////
    // Roll Function
    ////////////////////////////

    private static int roll(int max) {
        return rand.nextInt(max) + 1;
    }
}
