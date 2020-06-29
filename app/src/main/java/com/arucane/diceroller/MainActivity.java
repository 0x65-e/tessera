package com.arucane.diceroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.arucane.diceroller.fragments.SectionsPagerAdapter;
import com.arucane.diceroller.util.Die;
import com.arucane.diceroller.util.Roller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static boolean rollHistory;
    public static boolean verbose;
    static boolean darkMode = false;

    ArrayList<Die> customDice = new ArrayList<>();
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vPager.getCurrentItem() == 0) {
                    // If the custom roll isn't valid, FAB shouldn't work
                    if (decodeCustomRoll(customDiceCode)) {
                        Toast.makeText(getApplicationContext(), "Rolled " + customDiceCode, Toast.LENGTH_SHORT).show();

                        int[] results = Roller.results(customDice);
                        int sum = Roller.sum(results) + customMod;

                        TextView resultsView = findViewById(R.id.results);
                        String out = String.format(Locale.getDefault(), "Results: %d (%s)", sum, customDiceCode);
                        if (results.length > 1 && verbose) out += "\n" + Arrays.toString(results);
                        if (rollHistory)
                            out += "\n\n" + resultsView.getText().subSequence(8, resultsView.getText().length()); // Add the history if the results history is on
                        resultsView.setText(out);
                    } else {
                        Snackbar.make(view, "Custom dice code not set", Snackbar.LENGTH_SHORT)
                                .setAction("SET", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                        startActivity(intent); // Launch the preferences screen
                                    }
                                }).show();
                    }
                } else {
                    Snackbar.make(view, "Not implemented yet", Snackbar.LENGTH_SHORT).show();
                }
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

    private void switchMode() {
        // Should switch styles
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
                int num = Math.abs(Integer.parseInt(temp[0]));
                int max = Integer.parseInt(temp[1]);
                for (int j = 0; j < num; j++) {
                    customDice.add(new Die((int)Math.copySign(max, Integer.parseInt(temp[0])))); // Allow for subtracting dice
                }
            } else { // it's a modifier
                customMod += Integer.parseInt(parts[i]);
            }
        }
        return true;
    }
}
