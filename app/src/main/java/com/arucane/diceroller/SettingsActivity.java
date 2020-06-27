package com.arucane.diceroller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.diceroller.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    public static final String ROLL_PREF_SWITCH = "roll_history_switch";
    public static final String VERBOSE_ROLL_SWITCH = "verbose_roll_switch";
    public static final String DARK_MODE_SWITCH = "dark_theme_enable";
    public static final String CUSTOM_ROLL = "custom_roll";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit(); // Adds a settings fragment to the activity
    }
}
