package com.arucane.diceroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    public static final String ROLL_PREF_SWITCH = "roll_history_switch";
    public static final String VERBOSE_ROLL_SWITCH = "verbose_roll_switch";
    public static final String DARK_MODE_SWITCH = "dark_theme_enable";
    public static final String CUSTOM_ROLL = "custom_roll";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Correct theming for settings page
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = sharedPref.getBoolean(SettingsActivity.DARK_MODE_SWITCH, false);
        if (darkMode) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeRed);
        }

        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit(); // Adds a settings fragment to the activity
    }
}
