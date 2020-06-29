package com.arucane.diceroller.fragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private String titles[] = {"Dice", "Calculator"};

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); // only the current fragment will be resumed, all other fragments will be stuck at started
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 1) return CalculatorFragment.getInstance(position);
        return DiceFragment.getInstance(position); // default
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
