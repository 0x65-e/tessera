package com.arucane.diceroller.fragments;

import android.os.Build;
import android.os.Bundle;
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
import com.arucane.diceroller.util.DiceGroup;
import com.arucane.diceroller.util.RollEngine;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fragment that contains calculator-like buttons to create complex dice codes
 */
public class CalculatorFragment extends Fragment {

    List<DiceGroup> terms;
    StringBuilder previewText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.calculator_fragment, container, false);
        terms = new ArrayList<>();
        previewText = new StringBuilder();

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

        // Have to be atomic so that they can be modified in onClickListeners
        AtomicReference<ButtonTypes> lastPressed = new AtomicReference<>(ButtonTypes.OPERATION);
        AtomicBoolean isDN = new AtomicBoolean(false);
        AtomicInteger modifier = new AtomicInteger(1);

        final Button[] simpleDice = { d2, d3, d4, d6, d8, d10, d12, d20, d100 };
        final int[] simpleDiceValues = { 2, 3, 4, 6, 8, 10, 12, 20, 100 };

        for (int i = 0; i < simpleDice.length; i++) {
            // effectively final local variables for onClickListener
            int dieMax = simpleDiceValues[i];
            simpleDice[i].setOnClickListener(buttonView -> {
                // Save the dN if we're working on one
                if (isDN.get()) {
                    previewText.append("+");
                    terms.get(terms.size()-1).setMaxVal(modifier.get());
                    modifier.set(1);
                    isDN.set(false);
                }
                terms.add(new DiceGroup(modifier.get(), dieMax));
                lastPressed.set(ButtonTypes.DIE);
                // disable other dice buttons, enable numbers and operators and roll
                // Update the preview
                previewText.append(((TextView)buttonView).getText());
                TextView preview = view.findViewById(R.id.calc_preview);
                preview.setText(previewText.toString());
            });
        }

        Button[] simpleCalc = { calc0, calc1, calc2, calc3, calc4, calc5, calc6, calc7, calc8, calc9 };

        for (int i = 0; i < simpleCalc.length; i++) {
            // effectively final local variables for onClickListener
            ButtonTypes finalLastPressed = lastPressed.get();
            int finalI = i; // only works as a shorthand for the numerical value because buttons are in increasing order!
            simpleCalc[i].setOnClickListener(buttonView -> {
                switch (finalLastPressed) {
                    case DIE:
                    case DN: // save max val in modifier until a new group is started explicitly
                        modifier.set(finalI);
                        break;
                    case NUMBER: // working on a multi-digit number of rolls or max dN value
                        modifier.set(modifier.get() * 10 + finalI);
                        break;
                    case OPERATION:
                        modifier.set(modifier.get() * finalI); // multiply to copy sign, either -1 or +1
                        break;
                }
                lastPressed.set(ButtonTypes.NUMBER);
                // disable nothing, enable everything
                // Update the preview
                previewText.append(finalI);
                TextView preview = view.findViewById(R.id.calc_preview);
                preview.setText(previewText.toString());
            });
        }

        dN.setOnClickListener(buttonView -> {
            terms.add(new DiceGroup(modifier.get(), 0)); // assigning zero temporarily
            isDN.set(true);
            // disable other dice buttons and operators and roll, enable numbers (except zero)
            // Update the preview
            previewText.append("d");
            TextView preview = view.findViewById(R.id.calc_preview);
            preview.setText(previewText.toString());
        });

        final Button[] operators = { calc_plus, calc_minus };
        final int[] operatorValues = { 1, -1 };

        for (int i = 0; i < operators.length; i++) {
            int val = operatorValues[i];
            operators[i].setOnClickListener(buttonView -> {
                // Save the dN if we're working on one
                if (isDN.get()) {
                    terms.get(terms.size()-1).setMaxVal(modifier.get());
                    isDN.set(false);
                } else if (lastPressed.get() == ButtonTypes.NUMBER) {
                    terms.add(new DiceGroup(0, modifier.get())); // special case of a static modifier
                }
                // Set positive or negative, depending on the button
                modifier.set(val);
                lastPressed.set(ButtonTypes.OPERATION);
                // disable roll and operators, enable dice buttons and numbers
                // Update the preview
                previewText.append(((TextView)buttonView).getText());
                TextView preview = view.findViewById(R.id.calc_preview);
                preview.setText(previewText.toString());
            });
        }

        // Clear the list completely (and replace with empty term)
        calc_clear.setOnClickListener(buttonView -> {
            terms.clear();
            isDN.set(false);
            lastPressed.set(ButtonTypes.OPERATION); // Should be fine as a default value
            modifier.set(1);
            // enable all buttons? except roll
            // Update the preview
            previewText = new StringBuilder();
            TextView preview = view.findViewById(R.id.calc_preview);
            preview.setText(previewText.toString());
        });

        calc_submit.setOnClickListener(buttonView -> {
            if (lastPressed.get() == ButtonTypes.NUMBER) {
                if (isDN.get()) {
                    // finish the current dN
                    terms.get(terms.size() - 1).setMaxVal(modifier.get());
                } else {
                    // finish the current static mod
                    terms.add(new DiceGroup(0, modifier.get()));
                }
            }
            if ((isDN.get() && lastPressed.get() != ButtonTypes.NUMBER) || lastPressed.get() == ButtonTypes.OPERATION) { // Should never occur if buttons are enabled correctly
                Snackbar.make(view, "Invalid Dice Code", Snackbar.LENGTH_SHORT).show();
            } else {
                StringBuilder expandedResults = new StringBuilder();
                int sum = 0;

                // Roll results
                List<List<Integer>> results = RollEngine.results(terms);

                // Create appropriate result string
                for (int i = 0; i < results.size(); i++) {
                    int maxVal = terms.get(i).getMaxVal();
                    int numRolls = terms.get(i).getNumRolls();
                    if (numRolls == 0) {
                        // Static mod
                        if (expandedResults.length() > 0 && maxVal > 0) expandedResults.append("+");
                        expandedResults.append(maxVal);
                        sum += maxVal;
                    } else {
                        if (numRolls < 0) {
                            expandedResults.append("-");
                            sum -= RollEngine.sum(results.get(i));
                        } else {
                            if (expandedResults.length() > 0) expandedResults.append("+");
                            sum += RollEngine.sum(results.get(i));
                        }
                        expandedResults.append(results.get(i).toString());
                    }
                }

                // inflate the layout of the popup window
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

            // Remove an extra term from unfinished modifier
            if (lastPressed.get() == ButtonTypes.NUMBER && !isDN.get()) {
                terms.remove(terms.size()-1);
            }
        });

        return view;
    }

    public static Fragment getInstance(int position) {
        return new CalculatorFragment();
    }

    private enum ButtonTypes {
        NUMBER,
        DIE,
        DN,
        OPERATION
    }
}
