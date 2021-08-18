package com.arucane.diceroller.util;

import java.util.List;
import java.util.Random;

public final class Roller {

    static Random rand = new Random();

    // No need to have an instance of this class
    private Roller() { }

    // Return the array results for a set of dice
    public static int[] results(Die[] dice) {
        int[] out = new int[dice.length];
        for (int i = 0; i < out.length; i++) out[i] = roll(dice[i]);
        return out;
    }

    public static int[] results(List<Die> dice) {
        int[] out = new int[dice.size()];
        for (int i = 0; i < out.length; i++) out[i] = roll(dice.get(i));
        return out;
    }

    public static int sum(int[] arr) {
        int sum = 0;
        for (int i : arr) sum += i;
        return sum;
    }

    ////////////////////////////
    // Roll Functions
    ////////////////////////////


    public static int roll(Die die) {
        return (int) Math.copySign(rand.nextInt(Math.abs(die.max())) + 1, die.max());
    }

    // Return the single int result for a set of dice
    public static int roll(Die[] dice) {
        int sum = 0;
        for (Die die : dice) sum += roll(die);
        return sum;
    }
}
