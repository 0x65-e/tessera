package com.arucane.diceroller.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
 * Calculator that contains calculator-like buttons to create complicated dice codes with modifiers
 * and filters, including rerolling, exploding, and dropping dice
 */
public class AdvancedCalculatorFragment extends Fragment {

    List<DiceGroup> terms;
    StringBuilder previewText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.advanced_calculator_fragment, container, false);
        terms = new ArrayList<>();
        previewText = new StringBuilder();

        // Dice buttons
        final Button d2 = view.findViewById(R.id.advcalc_d2);
        final Button d3 = view.findViewById(R.id.advcalc_d3);
        final Button d4 = view.findViewById(R.id.advcalc_d4);
        final Button d6 = view.findViewById(R.id.advcalc_d6);
        final Button d8 = view.findViewById(R.id.advcalc_d8);
        final Button d10 = view.findViewById(R.id.advcalc_d10);
        final Button d12 = view.findViewById(R.id.advcalc_d12);
        final Button d20 = view.findViewById(R.id.advcalc_d20);
        final Button d100 = view.findViewById(R.id.advcalc_d100);
        final Button dN = view.findViewById(R.id.advcalc_dN);
        // Calculator buttons
        final Button calc0 = view.findViewById(R.id.advcalc_0);
        final Button calc1 = view.findViewById(R.id.advcalc_1);
        final Button calc2 = view.findViewById(R.id.advcalc_2);
        final Button calc3 = view.findViewById(R.id.advcalc_3);
        final Button calc4 = view.findViewById(R.id.advcalc_4);
        final Button calc5 = view.findViewById(R.id.advcalc_5);
        final Button calc6 = view.findViewById(R.id.advcalc_6);
        final Button calc7 = view.findViewById(R.id.advcalc_7);
        final Button calc8 = view.findViewById(R.id.advcalc_8);
        final Button calc9 = view.findViewById(R.id.advcalc_9);
        // Term-ending buttons
        final Button calc_plus = view.findViewById(R.id.advcalc_plus);
        final Button calc_minus = view.findViewById(R.id.advcalc_minus);
        // Control buttons
        final Button calc_roll = view.findViewById(R.id.advcalc_roll);
        final ImageButton calc_clear = view.findViewById(R.id.advcalc_clear);
        // Group filter buttons
        final Button keep_low = view.findViewById(R.id.advcalc_keep_low);
        final Button keep_high = view.findViewById(R.id.advcalc_keep_high);
        final Button drop_low = view.findViewById(R.id.advcalc_drop_low);
        final Button drop_high = view.findViewById(R.id.advcalc_drop_high);
        // Individual die conditions
        final Button reroll = view.findViewById(R.id.advcalc_reroll);
        final Button reroll_continuously = view.findViewById(R.id.advcalc_reroll_continuously);
        final Button explode = view.findViewById(R.id.advcalc_explode);
        final Button explode_continuously = view.findViewById(R.id.advcalc_explode_continuously);
        final Button calc_min = view.findViewById(R.id.advcalc_min);
        final Button calc_max = view.findViewById(R.id.advcalc_max);
        // Condition modifiers
        final Button greater_than = view.findViewById(R.id.advcalc_greaterthan);
        final Button less_than = view.findViewById(R.id.advcalc_lessthan);
        final Button greater_than_equal = view.findViewById(R.id.advcalc_greaterthan_equal);
        final Button less_than_equal = view.findViewById(R.id.advcalc_lessthan_equal);

        calc_roll.setEnabled(false); // No rolling before any buttons are pressed
        calc_plus.setEnabled(false); // Can't start with a plus either

        // TODO: backspace button?

        // Have to be atomic so that they can be modified in onClickListeners
        AtomicReference<ButtonTypes> lastPressed = new AtomicReference<>(ButtonTypes.OPERATION);
        AtomicBoolean isDN = new AtomicBoolean(false);
        AtomicInteger modifier = new AtomicInteger(1);

        final Button[] simpleDice = { d2, d3, d4, d6, d8, d10, d12, d20, d100 };
        final int[] simpleDiceValues = { 2, 3, 4, 6, 8, 10, 12, 20, 100 };
        final Button[] simpleCalc = { calc0, calc1, calc2, calc3, calc4, calc5, calc6, calc7, calc8, calc9 };
        final Button[] operators = { calc_plus, calc_minus };
        final int[] operatorValues = { 1, -1 };
        // Arrays to make enable/disable easier
        final Button[] dNArray = { dN };
        final Button[] rollArray = { calc_roll };

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
                // Add a plus if we have two dice in a row
                if (lastPressed.get() == ButtonTypes.DIE) {
                    previewText.append("+");
                    modifier.set(1);
                }
                terms.add(new DiceGroup(modifier.get(), dieMax));
                lastPressed.set(ButtonTypes.DIE);
                // enable all buttons
                enableButtons(simpleDice, dNArray, simpleCalc, operators, rollArray);
                // Update the preview
                previewText.append(((TextView)buttonView).getText());
                TextView preview = view.findViewById(R.id.advcalc_preview);
                preview.setText(previewText.toString());
            });
        }

        for (int i = 0; i < simpleCalc.length; i++) {
            // effectively final local variables for onClickListener
            int finalI = i; // only works as a shorthand for the numerical value because buttons are in increasing order!
            simpleCalc[i].setOnClickListener(buttonView -> {
                switch (lastPressed.get()) {
                    case DIE:
                        previewText.append("+");
                    case DN: // save max val in modifier until a new group is started explicitly
                        modifier.set(finalI);
                        break;
                    case NUMBER: // working on a multi-digit number of rolls or max dN value
                        // don't care about race conditions between modifier.get() and modifier.set()
                        // because two onClickHandlers shouldn't be hit fast enough. If they are, then
                        // just reset and try again.
                        int currentMod = modifier.get();
                        if (currentMod < 0) modifier.set(currentMod * 10 - finalI);
                        else modifier.set(currentMod * 10 + finalI);
                        break;
                    case OPERATION:
                        modifier.set(modifier.get() * finalI); // multiply to copy sign, either -1 or +1
                        break;
                }
                lastPressed.set(ButtonTypes.NUMBER);
                // disable nothing, enable everything
                enableButtons(simpleDice, dNArray, simpleCalc, operators, rollArray);
                // Update the preview
                previewText.append(finalI);
                TextView preview = view.findViewById(R.id.advcalc_preview);
                preview.setText(previewText.toString());
            });
        }

        dN.setOnClickListener(buttonView -> {
            // Save the previous dN if we're working on one
            if (isDN.get()) {
                previewText.append("+");
                terms.get(terms.size()-1).setMaxVal(modifier.get());
                modifier.set(1);
            }
            // Add a plus if we have two dice in a row
            if (lastPressed.get() == ButtonTypes.DIE) {
                previewText.append("+");
                modifier.set(1);
            }
            terms.add(new DiceGroup(modifier.get(), 0)); // assigning zero temporarily
            isDN.set(true);
            lastPressed.set(ButtonTypes.DN);
            // disable other dice buttons and operators and roll, enable numbers (except zero)
            enableButtons(simpleCalc);
            disableButtons(simpleDice, dNArray, operators, rollArray);
            calc0.setEnabled(false); // No dN starting with zero
            // Update the preview
            previewText.append("d");
            TextView preview = view.findViewById(R.id.advcalc_preview);
            preview.setText(previewText.toString());
        });

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
                enableButtons(simpleDice, dNArray, simpleCalc);
                disableButtons(rollArray, operators);
                // Update the preview
                previewText.append(((TextView)buttonView).getText());
                TextView preview = view.findViewById(R.id.advcalc_preview);
                preview.setText(previewText.toString());
            });
        }

        // Clear the list completely (and replace with empty term)
        calc_clear.setOnClickListener(buttonView -> {
            terms.clear();
            isDN.set(false);
            lastPressed.set(ButtonTypes.OPERATION); // Should be fine as a default value
            modifier.set(1);
            // enable all buttons except roll and plus
            enableButtons(simpleDice, dNArray, simpleCalc, operators);
            disableButtons(rollArray);
            calc_plus.setEnabled(false);
            // Update the preview
            previewText = new StringBuilder();
            TextView preview = view.findViewById(R.id.advcalc_preview);
            preview.setText(previewText.toString());
        });

        calc_roll.setOnClickListener(buttonView -> {
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
                        if (expandedResults.length() > 0 && maxVal >= 0) expandedResults.append("+");
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
            if (lastPressed.get() == ButtonTypes.NUMBER) {
                if (isDN.get()) {
                    // reset the dN. Not necessary, but it may help later for parsing
                    terms.get(terms.size() - 1).setMaxVal(0);
                } else {
                    terms.remove(terms.size() - 1);
                }
            }
        });

        return view;
    }

    private static void disableButtons(Button[]... buttons) {
        for (Button[] barray : buttons) for (Button b : barray) b.setEnabled(false);
    }

    private static void enableButtons(Button[]... buttons) {
        for (Button[] barray : buttons) for (Button b : barray) b.setEnabled(true);
    }

    public static Fragment getInstance() {
        return new AdvancedCalculatorFragment();
    }

    private enum ButtonTypes {
        NUMBER,
        DIE,
        DN,
        OPERATION
    }
}