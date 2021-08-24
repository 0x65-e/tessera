package com.arucane.diceroller.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold information about a group of similar dice, e.g. 6d8 or 3d4
 */
public class DiceGroup {
    int numRolls, max;
    List<Filter> filters;

    /**
     * Create a new group of dice. To subtract values, use a negative number parameter. Max must not be negative or zero.
     * @param numRolls The number of dice to roll
     * @param max The maximum value the die can roll
     */
    public DiceGroup(int numRolls, int max) {
        this.numRolls = numRolls;
        this.max = Math.abs(max);
        this.filters = new ArrayList<>();
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
    public int getDieMax() {
        return max;
    }

    /**
     * Set the maximum value of the die
     * @param max maximum value (i.e. the highest face on the die)
     */
    public void setDieMax(int max) {
        this.max = max;
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
                rolls.remove(position);
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
                rolls.remove(position);
            }
            return rolls;
        };
    }

    public interface Filter {
        /**
         * Filters a list of roll results down according to some specification
         * @param rolls Initial roll results
         * @return New results after filtering
         */
        List<Integer> applyFilter(List<Integer> rolls);
    }
}
