package com.arucane.diceroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.arucane.diceroller.fragments.SectionsPagerAdapter;
import com.arucane.diceroller.util.DiceGroup;
import com.arucane.diceroller.util.Roller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static boolean rollHistory;
    public static boolean verbose;
    static boolean darkMode = false;

    List<DiceGroup> customDice = new ArrayList<>();
    int customMod = 0;
    String customDiceCode;

    private ViewPager vPager;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Theme (and thus preferences) has to come before super.onCreate
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        rollHistory = sharedPref.getBoolean(SettingsActivity.ROLL_PREF_SWITCH, false);
        verbose = sharedPref.getBoolean(SettingsActivity.VERBOSE_ROLL_SWITCH, true);
        darkMode = sharedPref.getBoolean(SettingsActivity.DARK_MODE_SWITCH, false);
        if (darkMode) {
            setTheme(R.style.AppThemeDark_NoActionBar);
        } else {
            setTheme(R.style.AppThemeRed_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);
        vPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(vPager);



        //Toast.makeText(this, "Roll history " + ((rollHistory) ? "activated" : "deactivated"), Toast.LENGTH_SHORT).show(); // Only want to update this when setting changes
        customDiceCode = sharedPref.getString(SettingsActivity.CUSTOM_ROLL, "").replaceAll("[^dD0-9+-]", "");

        FloatingActionButton fab = findViewById(R.id.fab);

        // TODO: wrap decodeCustomRoll in a try/catch statement and erase setting if it fails

        fab.setOnClickListener(view -> {
            if (vPager.getCurrentItem() == 0) {
                // If the custom roll isn't valid, FAB shouldn't work
                if (decodeCustomRoll(customDiceCode)) {
                    Toast.makeText(getApplicationContext(), "Rolled " + customDiceCode, Toast.LENGTH_SHORT).show();

                    List<List<Integer>> results = Roller.results(customDice);
                    int sum = customMod;
                    for (int i = 0; i < customDice.size(); i++) {
                        // Account for negative dice groups
                        if (customDice.get(i).getNumRolls() < 0)
                            sum -= Roller.sum(results.get(i));
                        else
                            sum += Roller.sum(results.get(i));
                    }

                    TextView resultsView = findViewById(R.id.results);
                    String out = String.format(Locale.getDefault(), "Results: %d (%s)", sum, customDiceCode);
                    // Always show roll results if there is more than one DiceGroup
                    if (verbose && results.size() > 1) {
                        StringBuilder sb = new StringBuilder("\n");
                        for (int i = 0; i < customDice.size(); i++) {
                            if (customDice.get(i).getNumRolls() < 0)
                                sb.append("-");
                            else if (i > 0)
                                sb.append("+");
                            sb.append(results.get(i).toString());
                        }
                        out += sb.toString();
                    }
                    // Show roll results if there is only one DiceGroup with multiple dice
                    else if (verbose && results.size() == 1 && results.get(0).size() > 0) {
                        out += "\n" + ((customDice.get(0).getNumRolls() < 0) ? "-" : "") + results.get(0).toString();
                    }
                    // Don't show roll results for a single DiceGroup with a single die

                    // Add the history if the results history is on
                    if (rollHistory)
                        out += "\n\n" + resultsView.getText().subSequence(8, resultsView.getText().length());
                    resultsView.setText(out);
                } else {
                    Snackbar.make(view, "Custom dice code not set", Snackbar.LENGTH_SHORT)
                            .setAction("SET", snackView -> {
                                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(intent); // Launch the preferences screen
                            }).show();
                }
            } else {
                Snackbar.make(view, "Not implemented yet", Snackbar.LENGTH_SHORT).show();
            }
        });
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
                int num = Integer.parseInt(temp[0]);
                int max = Integer.parseInt(temp[1]);
                customDice.add(new DiceGroup(num, max));
            } else { // it's a modifier
                customMod += Integer.parseInt(parts[i]);
            }
        }
        return true;
    }
}
