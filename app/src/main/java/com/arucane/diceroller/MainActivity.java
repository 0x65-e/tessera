package com.arucane.diceroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.arucane.diceroller.fragments.SectionsPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    boolean rollHistory;
    boolean verbose;
    static boolean darkMode = false;
    Random rand = new Random();

    ArrayList<Die> customDice = new ArrayList<Die>();
    int customMod = 0;
    String customDiceCode;

    private ViewPager vPager;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);
        vPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(vPager);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        rollHistory = sharedPref.getBoolean(SettingsActivity.ROLL_PREF_SWITCH, false);
        verbose = sharedPref.getBoolean(SettingsActivity.VERBOSE_ROLL_SWITCH, true);
        if (darkMode != sharedPref.getBoolean(SettingsActivity.DARK_MODE_SWITCH, false)) {
            darkMode = sharedPref.getBoolean(SettingsActivity.DARK_MODE_SWITCH, false);
            Toast.makeText(this, "Dark Mode Changed", Toast.LENGTH_SHORT).show();
            switchMode();
        }
        //Toast.makeText(this, "Roll history " + ((rollHistory) ? "activated" : "deactivated"), Toast.LENGTH_SHORT).show(); // Only want to update this when setting changes
        customDiceCode = sharedPref.getString(SettingsActivity.CUSTOM_ROLL, "").replaceAll("[^dD0-9+-]", "");

        FloatingActionButton fab = findViewById(R.id.fab);

        // TODO: wrap decodeCustomRoll in a try/catch statement and erase setting if it fails

        // If the custom roll isn't valid, FAB shouldn't work
        if (decodeCustomRoll(customDiceCode)) {

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), "Rolled " + customDiceCode, Toast.LENGTH_SHORT).show();

                    int[] results = results(customDice);
                    int sum = sum(results) + customMod;

                    TextView resultsView = findViewById(R.id.results);
                    String out = String.format("Results: %d (%s)", sum, customDiceCode);
                    if (results.length > 1 && verbose) out += "\n" + Arrays.toString(results);
                    if (rollHistory) out += "\n\n" + resultsView.getText().subSequence(8, resultsView.getText().length()); // Add the history if the results history is on
                    resultsView.setText(out);

                }
            });
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Custom dice code not set", Snackbar.LENGTH_SHORT)
                            .setAction("SET", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                    startActivity(intent); // Launch the preferences screen
                                }
                            }).show();
                }
            });
        }




        /*final Button d20 = findViewById(R.id.d20);
        final Button d6 = findViewById(R.id.d6);
        final Button d8 = findViewById(R.id.d8);
        final Button d10 = findViewById(R.id.d10);
        final Button d12 = findViewById(R.id.d12);
        final Button d4 = findViewById(R.id.d4);

        Button[] dice = {d4, d6, d8, d10, d12, d20};
        for (Button die : dice) {
            die.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Parse the die to be rolled
                    TextView button_text = (TextView) view;
                    int max = Integer.parseInt(button_text.getText().subSequence(1, button_text.getText().length()).toString());
                    // Parse the number of rolls
                    String rollsIn = ((TextView)findViewById(R.id.numDice)).getText().toString();
                    int rolls = (rollsIn.length() > 0) ? Integer.valueOf(rollsIn) : 0; // Fix for if nothing is entered
                    if (rolls < 1) rolls = 1;
                    Die[] dice = new Die[rolls];
                    for (int i = 0; i < rolls; i++) dice[i] = new Die(max);
                    // Parse the modifier
                    String modIn = ((TextView)findViewById(R.id.modifier)).getText().toString();
                    int mod = (modIn.length() > 0) ? Integer.valueOf(modIn) : 0; // Fix for if nothing is entered

                    // Calculate rolls
                    int[] results = results(dice);
                    int sum = sum(results) + mod;

                    TextView resultsView = findViewById(R.id.results);

                    String out = String.format(Locale.getDefault(), "Results: %d (%dd%d", sum, results.length, max);
                    if (mod > 0 ) out += "+" + mod;
                    else if (mod < 0) out += mod;
                    out += ")";

                    if (results.length > 1 && verbose) out += "\n" + Arrays.toString(results);

                    if (rollHistory) out += "\n\n" + resultsView.getText().subSequence(8, resultsView.getText().length()); // Add the history if the results history is on

                    resultsView.setText(out);
                }
            });
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent); // Launch the preferences screen
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchMode() {
        // Should switch styles
    }

    /* Roll Functions */
    private int roll(Die die) {
        return (int) Math.copySign(rand.nextInt(Math.abs(die.max)) + 1, die.max);
    }

    // Return the single int result for a set of dice
    private int roll(Die[] dice) {
        int sum = 0;
        for (Die die : dice) sum += roll(die);
        return sum;
    }

    private boolean decodeCustomRoll(String code) {
        String codeScrubbed = code.replaceAll("[^dD0-9+-]", "");
        if (codeScrubbed.length() < 1) return false; // Disable FAB in this case
        String[] parts = codeScrubbed.split("(?=[-+])");
        for  (int i = 0; i < parts.length; i++) {
            if (parts[i].contains("d") || parts[i].contains("D")) {
                String[] temp = parts[i].split("[dD]");
                // TODO: Deal with no leading number
                if (temp[0].length() == 0) temp[0] = "1";
                else if (temp[0].length() == 1 && temp[0].charAt(0) == '-') temp[0] = "-1";
                else if (temp[0].length() == 1 && temp[0].charAt(0) == '+') temp[0] = "1";
                int num = Math.abs(Integer.valueOf(temp[0]));
                int max = Integer.valueOf(temp[1]);
                for (int j = 0; j < num; j++) {
                    customDice.add(new Die((int)Math.copySign(max, Integer.valueOf(temp[0])))); // Allow for subtracting dice
                }
            } else { // it's a modifier
                customMod += Integer.valueOf(parts[i]);
            }
        }
        return true;
    }

    // Return the array results for a set of dice
    private int[] results(Die[] dice) {
        int[] out = new int[dice.length];
        for (int i = 0; i < out.length; i++) out[i] = roll(dice[i]);
        return out;
    }

    private int[] results(ArrayList<Die> dice) {
        int[] out = new int[dice.size()];
        for (int i = 0; i < out.length; i++) out[i] = roll(dice.get(i));
        return out;
    }

    private int sum(int[] arr) {
        int sum = 0;
        for (int i : arr) sum += i;
        return sum;
    }


}

class Die {

    int max;

    public Die(int max) {
        this.max = max;
    }

    public int max() {
        return max;
    }
}
