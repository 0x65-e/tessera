package com.arucane.diceroller.fragments;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.arucane.diceroller.MainActivity;
import com.arucane.diceroller.R;
import com.arucane.diceroller.util.Die;
import com.arucane.diceroller.util.Roller;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CalculatorFragment extends Fragment {

    List<String> terms = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.calculator_fragment, container, false);

        // Dice buttons
        final Button d2 = view.findViewById(R.id.calc_d2);
        final Button d3 = view.findViewById(R.id.calc_d3);
        final Button d4 = view.findViewById(R.id.calc_d4);
        final Button d6 = view.findViewById(R.id.calc_d6);
        final Button d8 = view.findViewById(R.id.calc_d8);
        final Button d10 = view.findViewById(R.id.calc_d10);
        final Button d12 = view.findViewById(R.id.calc_d12);
        final Button d20 = view.findViewById(R.id.calc_d20);
        final Button d100 = view.findViewById(R.id.calc_d100);
        final Button dN = view.findViewById(R.id.calc_dN);
        // Calculator buttons
        final Button calc0 = view.findViewById(R.id.calc_0);
        final Button calc1 = view.findViewById(R.id.calc_1);
        final Button calc2 = view.findViewById(R.id.calc_2);
        final Button calc3 = view.findViewById(R.id.calc_3);
        final Button calc4 = view.findViewById(R.id.calc_4);
        final Button calc5 = view.findViewById(R.id.calc_5);
        final Button calc6 = view.findViewById(R.id.calc_6);
        final Button calc7 = view.findViewById(R.id.calc_7);
        final Button calc8 = view.findViewById(R.id.calc_8);
        final Button calc9 = view.findViewById(R.id.calc_9);
        // Term-ending buttons
        final Button calc_plus = view.findViewById(R.id.calc_plus);
        final Button calc_minus = view.findViewById(R.id.calc_minus);
        // Control buttons
        final Button calc_submit = view.findViewById(R.id.calc_submit);
        final Button calc_clear = view.findViewById(R.id.calc_clear);

        // TODO: backspace button?

        Button[] simpleButtons = { d2, d3, d4, d6, d8, d10, d12, d20, d100,
                calc0, calc1, calc2, calc3, calc4, calc5, calc6, calc7, calc8, calc9 };

        for (Button b : simpleButtons) {
            b.setOnClickListener(buttonView -> {
                // Append the direct text to the last term, to be parsed later
                appendToLastTerm(((TextView)buttonView).getText());
                // Update the preview
                TextView preview = view.findViewById(R.id.calc_preview);
                updatePreview(preview);
            });
        }

        // special version for dN since we don't want to append the underscore
        dN.setOnClickListener(buttonView -> {
            // Append the direct text to the last term, to be parsed later
            appendToLastTerm("d");
            // Update the preview
            TextView preview = view.findViewById(R.id.calc_preview);
            updatePreview(preview);
        });

        Button[] termEnding = { calc_plus, calc_minus };

        for (Button b : termEnding) {
            b.setOnClickListener(buttonView -> {
                // Adds a new term starting with the sign
                terms.add(String.valueOf(((TextView)buttonView).getText()));
                // Update the preview
                TextView preview = view.findViewById(R.id.calc_preview);
                updatePreview(preview);
            });
        }

        // Clear the list completely (and replace with empty term)
        calc_clear.setOnClickListener(buttonView -> {
            terms.clear();
            terms.add("");
            // Update the preview
            TextView preview = view.findViewById(R.id.calc_preview);
            updatePreview(preview);
        });

        calc_submit.setOnClickListener(buttonView -> {
            StringBuilder expandedResults = new StringBuilder();
            int sum = 0;
            boolean valid = true;
            // Validate terms
            for (String term : terms) {
                // Ignore empty terms
                if (term.equals(""))
                    continue;

                if (term.endsWith("d")) {
                    // problem, we need a die to roll
                    valid = false;
                    break;
                }
                String[] components = term.split("d");
                if (components.length > 2) {
                    // problem, we have more than one die code
                    valid = false;
                    break;
                } else if (components.length == 2) {
                    // Quick fix for codes with blank number of rolls
                    if (components[0].equals("") || components[0].equals("-") || components[0].equals("+"))
                        components[0] += "1";

                    int numRolls = Math.abs(Integer.parseInt(components[0]));
                    int max = Integer.parseInt(components[1]);

                    // Ignore rolls with zero dice
                    if (numRolls == 0)
                        continue;

                    // Roll
                    Die[] dice = new Die[numRolls];
                    for (int i = 0; i < numRolls; i++) {
                        dice[i] = new Die(max);
                    }
                    int[] results = Roller.results(dice);
                    // Account for sign
                    if (term.startsWith("-")) {
                        expandedResults.append("-");
                        sum -= Roller.sum(results);
                    } else if (term.startsWith("+")) {
                        expandedResults.append("+");
                        sum += Roller.sum(results);
                    } else {
                        sum += Roller.sum(results);
                    }
                    expandedResults.append(Arrays.toString(results));
                } else {
                    // Prevent double signs or signs at the end of terms
                    if (term.equals("+") || term.equals("-")) {
                        valid = false;
                        break;
                    }
                    // Must be a single string with no d, therefore a static bonus
                    sum += Integer.parseInt(term);
                    expandedResults.append(term);
                }
            }

            if (!valid) {
                Snackbar.make(view, "Invalid Dice Code", Snackbar.LENGTH_SHORT).show();
            } else {
                // inflate the layout of the popup window
                //LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.result_popup, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
                ((TextView)popupWindow.getContentView().findViewById(R.id.result_sum)).setText(String.valueOf(sum));

                if (MainActivity.verbose) {
                    ((TextView)popupWindow.getContentView().findViewById(R.id.result_breakdown)).setText(expandedResults.toString());
                }

                // Styling for drop shadow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    popupWindow.setElevation(20);
                }
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener((v, event) -> {
                    v.performClick();
                    popupWindow.dismiss();
                    return true;
                });
            }
        });

        return view;
    }

    private void appendToLastTerm(CharSequence str) {
        String term = "";
        if (terms.size() > 0)
            term = terms.remove(terms.size() - 1);
        terms.add(term + str);
    }

    private void updatePreview(TextView preview) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence cs : terms)
            sb.append(cs);
        preview.setText(sb.toString());
    }

    public static Fragment getInstance(int position) {
        return new CalculatorFragment();
    }
}
