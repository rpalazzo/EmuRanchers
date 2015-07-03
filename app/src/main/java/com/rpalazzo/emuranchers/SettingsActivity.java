/* Copyright Robert Palazzo 2015
 * Copying this propritary source code without express
 * written permission from the author is prohibited.
 */

package com.rpalazzo.emuranchers;


import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.preference.Preference.OnPreferenceChangeListener;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        //Difficulty
        Preference difficulty = findPreference("difficulty_key");
        difficulty.setSummary(difficulty.getSharedPreferences().
        		getString("difficulty_key", getString(R.string.difficulty_default)));
        
        difficulty.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	preference.setSummary(newValue.toString());
            	return true;
            }
        });
        
      //Optional Cards
        Preference optional_cards = findPreference("optionalcards_key");
        optional_cards.setSummary(optional_cards.getSharedPreferences().
        		getString("optionalcards_key", getString(R.string.optionalcards_default)));
        
        optional_cards.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	preference.setSummary(newValue.toString());
            	return true;
            }
        });
        
      //Number of years
        Preference numberofyears = findPreference("numberofyears_key");
        numberofyears.setSummary(numberofyears.getSharedPreferences().
        		getString("numberofyears_key", getString(R.string.numberofyears_default)));
        
        numberofyears.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	preference.setSummary(newValue.toString());
            	return true;
            }
        });
	}
}
