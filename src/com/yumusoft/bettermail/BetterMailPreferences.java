/* Copyright (C) 2011 Eduardo Gonzalez See LICENSE for details. */

package com.yumusoft.bettermail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

import com.yumusoft.bettermail.R;

import static com.yumusoft.bettermail.Globals.GMAIL_2_3_5_VERSION;

public class BetterMailPreferences extends PreferenceActivity {

	private static final int DIALOG_INCOMPATIBLE_GMAIL = 0;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        
        if (!compatibleGmailVersion()) {
        	showDialog(DIALOG_INCOMPATIBLE_GMAIL);
        	
        	CheckBoxPreference pref;
        	
        	pref = (CheckBoxPreference)findPreference("pref_show_notification");
        	pref.setChecked(false);
        	pref.setEnabled(false);

        	pref = (CheckBoxPreference)findPreference("pref_priority_inbox");
        	pref.setChecked(false);
        	pref.setEnabled(false);

        	pref = (CheckBoxPreference)findPreference("pref_vibrate");
        	pref.setChecked(false);
        	pref.setEnabled(false);

        	pref = (CheckBoxPreference)findPreference("pref_sound_notification");
        	pref.setChecked(false);        	
        	pref.setEnabled(false);

        	pref = (CheckBoxPreference)findPreference("pref_led_notification");
        	pref.setChecked(false);        	
        	pref.setEnabled(false);
        }
    }
	
	@Override
	public Dialog onCreateDialog(int id) {
		Dialog dialog;
    	AlertDialog.Builder builder;

		switch (id) {
		case DIALOG_INCOMPATIBLE_GMAIL:
    		
    		builder = new AlertDialog.Builder(this);
    		builder.setMessage(R.string.dialog_incompatible_gmail_message)
    			   .setTitle(R.string.dialog_incompatible_gmail_title)
    		       .setCancelable(true)
    		       .setNeutralButton(R.string.dialog_button_ok, 
    		    		   new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		        	   dialog.cancel();
    		           }
    		       });

    		dialog = builder.create();
    		break;
    	default:
    		dialog = null;
    		break;
		}
		
		return dialog;
	}

	private boolean compatibleGmailVersion() {
		PackageManager pm = getPackageManager();
        try {
        	int version = pm.getPackageInfo("com.google.android.gm", 0).versionCode;
        	if (version < GMAIL_2_3_5_VERSION) {
        		return true;
        	}
        } catch (NameNotFoundException e) {
        	// No Gmail.  Notifications wont work anyway.
        }
        
        return false;
	}
}
