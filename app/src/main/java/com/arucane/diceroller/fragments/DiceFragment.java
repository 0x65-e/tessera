package com.arucane.diceroller.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.arucane.diceroller.MainActivity;
import com.arucane.diceroller.util.Die;
import com.arucane.diceroller.R;
import com.arucane.diceroller.util.Roller;

import java.util.Arrays;
import java.util.Locale;

public class DiceFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dice_fragment, container, false);

        final Button d4 = view.findViewById(R.id.d4);
        final Button d6 = view.findViewById(R.id.d6);
        final Button d8 = view.findViewById(R.id.d8);
        final Button d10 = view.findViewById(R.id.d10);
        final Button d12 = view.findViewById(R.id.d12);
        final Button d20 = view.findViewById(R.id.d20);

        Button[] dice = {d4, d6, d8, d10, d12, d20};
        for (Button die : dice) {
            die.setOnClickListener(buttonView -> {
                // Parse the die to be rolled using the text on the button
                TextView button_text = (TextView) buttonView;
                int max = Integer.parseInt(button_text.getText().subSequence(1, button_text.getText().length()).toString());
                // Parse the number of rolls
                String rollsIn = ((TextView)view.findViewById(R.id.numDice)).getText().toString();
                int rolls = (rollsIn.length() > 0) ? Integer.parseInt(rollsIn) : 0; // Fix for if nothing is entered
                if (rolls < 1) rolls = 1;
                Die[] dice1 = new Die[rolls];
                for (int i = 0; i < rolls; i++) dice1[i] = new Die(max);
                // Parse the modifier
                String modIn = ((TextView)view.findViewById(R.id.modifier)).getText().toString();
                int mod = (modIn.length() > 0) ? Integer.parseInt(modIn) : 0; // Fix for if nothing is entered

                // Calculate rolls
                int[] results = Roller.results(dice1);
                int sum = Roller.sum(results) + mod;

                TextView resultsView = view.findViewById(R.id.results);

                String out = String.format(Locale.getDefault(), "Results: %d (%dd%d", sum, results.length, max);
                if (mod > 0 ) out += "+" + mod;
                else if (mod < 0) out += mod;
                out += ")";

                if (results.length > 1 && MainActivity.verbose) out += "\n" + Arrays.toString(results);

                if (MainActivity.rollHistory) out += "\n\n" + resultsView.getText().subSequence(8, resultsView.getText().length()); // Add the history if the results history is on

                resultsView.setText(out);
            });
        }

        return view;
    }

    public static Fragment getInstance(int position) {
        return new DiceFragment();
    }
}
