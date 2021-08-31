package com.arucane.diceroller.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold information about a group of similar dice, e.g. 6d8 or 3d4
 */
public class DiceGroup {
    int numRolls, maxVal;
    List<Filter> filters;
    List<Modifier> modifiers;

    /**
     * Create a new group of dice. To subtract values, use a negative numRolls parameter. Negative
     * maxVal or numRolls will be treated as positive, but the original negative value will be returned
     * by the respective get methods. A zero maxVal or numRolls will not be evaluated by RollEngine.
     * @param numRolls The number of dice to roll
     * @param maxVal The maximum value the die can roll
     */
    public DiceGroup(int numRolls, int maxVal) {
        this.numRolls = numRolls;
        this.maxVal = maxVal;
        this.filters = new ArrayList<>();
        this.modifiers = new ArrayList<>();
    }

    /**
     * Get the number of rolls in the DiceGroup
     * @return how many dice to roll
     */
    public int getNumRolls() {
        return numRolls;
    }

    /**
     * Set the number of rolls in this DiceGroup
     * @param numRolls number of dice to roll when evaluating
     */
    public void setNumRolls(int numRolls) {
        this.numRolls = numRolls;
    }

    /**
     * Get the maximum value the die can roll
     * @return maximum value (i.e. the highest face on the die)
     */
    public int getMaxVal() {
        return maxVal;
    }

    /**
     * Set the maximum value of the die
     * @param maxVal maximum value (i.e. the highest face on the die)
     */
    public void setMaxVal(int maxVal) {
        this.maxVal = maxVal;
    }

    ////////////////////////////
    // Group Filters
    ////////////////////////////

    /**
     * Add a new filter to be applied to the results of a roll. Filters are applied in the order
     * they are added.
     * @param filter Filter to apply to roll results
     */
    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    /**
     * Remove the last filter added
     */
    public void removeLastFilter() {
        if (!filters.isEmpty()) filters.remove(filters.size()-1);
    }

    /**
     * Apply all filters added to this DiceGroup to the raw roll results and return the filtered results
     * @param results Initial roll results
     * @return New results after filtering
     */
    public List<Integer> applyFilters(List<Integer> results) {
        List<Integer> out = results;
        for (Filter f : filters) {
            out = f.applyFilter(out);
        }
        return out;
    }

    /**
     * Get a filter that will drop the specified number of rolls in ascending order
     * @param numToDrop Number of rolls to drop
     * @return Filter to apply to a DiceGroup
     */
    public static Filter newDropLowestFilter(int numToDrop) {
        return rolls -> {
            if (rolls.size() <= numToDrop) {
                Log.d("FILTER.DL", "Dropping: " + rolls.toString());
                rolls.clear();
                return rolls;
            }
            // Iterate through rolls numToDrop times, removing the lowest element
            // rolls is guaranteed to have more than numToDrop entries by the previous statement
            for (int i = 0; i < numToDrop; i++) {
                int lowest = rolls.get(0);
                int position = 0;
                for (int j = 1; j < rolls.size(); j++) {
                    if (rolls.get(j) < lowest) {
                        lowest = rolls.get(j);
                        position = j;
                    }
                }
                Log.d("FILTER.DL", "Dropping: " + rolls.remove(position).toString());
            }
            return rolls;
        };
    }

    /**
     * Get a filter that will drop the specified number of rolls in descending order
     * @param numToDrop Number of rolls to drop
     * @return Filter to apply to a DiceGroup
     */
    public static Filter newDropHighestFilter(int numToDrop) {
        return rolls -> {
            if (rolls.size() <= numToDrop) {
                Log.d("FILTER.DL", "Dropping: " + rolls.toString());
                rolls.clear();
                return rolls;
            }
            // Iterate through rolls numToDrop times, removing the highest element
            // rolls is guaranteed to have more than numToDrop entries by the previous statement
            for (int i = 0; i < numToDrop; i++) {
                int highest = rolls.get(0);
                int position = 0;
                for (int j = 1; j < rolls.size(); j++) {
                    if (rolls.get(j) > highest) {
                        highest = rolls.get(j);
                        position = j;
                    }
                }
                Log.d("FILTER.DH", "Dropping: " + rolls.remove(position).toString());
            }
            return rolls;
        };
    }

    /**
     * Get a filter that will keep the specified number of rolls in ascending order
     * @param numToKeep Number of rolls to keep
     * @return Filter to apply to a DiceGroup
     */
    public static Filter newKeepLowestFilter(int numToKeep) {
        return rolls -> {
            int numToDrop = rolls.size() - numToKeep;
            // Iterate through rolls numToDrop times, removing the highest element
            // If numToKeep is greater than rolls.size(), we won't drop anything
            for (int i = 0; i < numToDrop; i++) {
                int highest = rolls.get(0);
                int position = 0;
                for (int j = 1; j < rolls.size(); j++) {
                    if (rolls.get(j) > highest) {
                        highest = rolls.get(j);
                        position = j;
                    }
                }
                Log.d("FILTER.KL", "Dropping: " + rolls.remove(position).toString());
            }
            return rolls;
        };
    }

    /**
     * Get a filter that will keep the specified number of rolls in descending order
     * @param numToKeep Number of rolls to keep
     * @return Filter to apply to a DiceGroup
     */
    public static Filter newKeepHighestFilter(int numToKeep) {
        return rolls -> {
            int numToDrop = rolls.size() - numToKeep;
            // Iterate through rolls numToDrop times, removing the lowest element
            // If numToKeep is greater than rolls.size(), we won't drop anything
            for (int i = 0; i < numToDrop; i++) {
                int lowest = rolls.get(0);
                int position = 0;
                for (int j = 1; j < rolls.size(); j++) {
                    if (rolls.get(j) < lowest) {
                        lowest = rolls.get(j);
                        position = j;
                    }
                }
                Log.d("FILTER.KH", "Dropping: " + rolls.remove(position).toString());
            }
            return rolls;
        };
    }

    /**
     * Return a new Filter based on the type
     * @param type Type of Filter to create. Self-explanatory by name.
     * @param num Single parameter to pass to Filter. Ignored in the case of Type.None
     * @return Filter to apply to a DiceGroup
     */
    public static Filter newFilterFromType(Filter.Type type, int num) {
        switch (type) {
            case DropLowest:
                return newDropLowestFilter(num);
            case DropHighest:
                return newDropHighestFilter(num);
            case KeepLowest:
                return newKeepLowestFilter(num);
            case KeepHighest:
                return newKeepHighestFilter(num);
            default:
                return rolls -> rolls;
        }
    }

    public interface Filter {
        /**
         * Filters a list of roll results down according to some specification
         * @param rolls Initial roll results
         * @return New results after filtering
         */
        List<Integer> applyFilter(List<Integer> rolls);

        enum Type {
            DropLowest,
            DropHighest,
            KeepLowest,
            KeepHighest
        }
    }

    ////////////////////////////
    // Individual Modifiers
    ////////////////////////////

    /**
     * Add a modifier to be run on every die roll in this DiceGroup. Modifiers are run in the order they are added.
     * @param mod Modifier to apply to all roll results
     */
    public void addModifier(Modifier mod) {
        modifiers.add(mod);
    }

    /**
     * Remove the last Modifier added to this Dice Group
     */
    public void removeLastModifier() {
        if (!modifiers.isEmpty()) modifiers.remove(modifiers.size()-1);
    }

    /**
     * Apply all modifiers added to this DiceGroup to the raw roll results and return the modified result
     * @param roll Raw result to modify
     * @return Possibly modified valid roll result, or -1 if result rejected
     */
    public int applyModifiers(int roll) {
        for (Modifier mod : modifiers) {
            roll = mod.accept(roll);
            if (roll == -1)
                return roll; // break early for a rejected filter
        }
        return roll;
    }

    /**
     * Sets the minimum value the die can roll
     * @param min Minimum value
     * @return Modifier to apply to a DiceGroup
     */
    public static Modifier newMinModifier(int min) {
        return roll -> Math.max(roll, min);
    }

    /**
     * Sets the maximum value the die can roll
     * @param max Maximum value
     * @return Modifier to apply to a DiceGroup
     */
    public static Modifier newMaxModifier(int max) {
        return roll -> Math.min(roll, max);
    }

    public interface Modifier {
        /**
         * Accepts or rejects a given roll, possibly after modifying
         * @param roll Initial roll result
         * @return Possibly modified accepted value, or -1 for an unacceptable value
         */
        int accept(int roll);

        enum Type {
            MIN,
            MAX,
            REROLL,
            EXPLODE
        }
    }
}
