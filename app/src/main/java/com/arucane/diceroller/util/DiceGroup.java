package com.arucane.diceroller.util;

/**
 * Class to hold information about a group of similar dice, e.g. 6d8 or 3d4
 */
public class DiceGroup {
    int numRolls, max;

    /**
     * Create a new group of dice. To subtract values, use a negative number parameter. Max must not be negative or zero.
     * @param numRolls The number of dice to roll
     * @param max The maximum value the die can roll
     */
    public DiceGroup(int numRolls, int max) {
        this.numRolls = numRolls;
        this.max = Math.abs(max);
    }

    /**
     * Get the number of rolls in the DiceGroup
     * @return how many dice to roll
     */
    public int getNumRolls() {
        return numRolls;
    }

    /**
     * Get the maximum value the die can roll
     * @return maximum value (i.e. the highest face on the die)
     */
    public int getMax() {
        return max;
    }

}
