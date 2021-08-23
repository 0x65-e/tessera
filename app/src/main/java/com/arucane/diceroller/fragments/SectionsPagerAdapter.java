package com.arucane.diceroller.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private final CharSequence[] titles;

    /**
     * Create a fragment pager adapter to serve fragments
     * @param fm system FragmentManager to pass to super constructor
     * @param fragmentTitles String array of fragment titles. Should have enough entries for the number
     *                       of fragments that will be requested with getItem
     */
    public SectionsPagerAdapter(FragmentManager fm, CharSequence[] fragmentTitles) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); // only the current fragment will be resumed, all other fragments will be stuck at started
        titles = fragmentTitles;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) return CalculatorFragment.getInstance(position);
        else if (position == 2) return AdvancedCalculatorFragment.getInstance();
        return DiceFragment.getInstance(position); // default
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    public CharSequence getPageTitle(int position) {
        if (position >= titles.length) return "";
        return titles[position];
    }
}
