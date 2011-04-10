/* Copyright (C) 2011 Eduardo Gonzalez See LICENSE for details. */

package com.yumusoft.bettermail;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.yumusoft.bettermail.R;

public class BetterMailPreferences extends PreferenceActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
        
    }
}
