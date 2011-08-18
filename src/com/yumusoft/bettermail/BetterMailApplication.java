package com.yumusoft.bettermail;

import static android.os.PatternMatcher.PATTERN_SIMPLE_GLOB;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import static com.yumusoft.bettermail.Globals.DEBUG;
import static com.yumusoft.bettermail.Globals.GMAIL_2_3_5_VERSION;

public class BetterMailApplication extends Application {
	private GmailNotificationReceiver _receiver;

	@Override
	public void onCreate() {
		super.onCreate();
		
		if (DEBUG)
			Log.d("BetterMail", "BetterMail Application start.");
		
		checkAndRegisterNotificationBroadcast();
	}

	public void registerNotificationBroadcast() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_PROVIDER_CHANGED);
		filter.addDataPath("/unread/.*", PATTERN_SIMPLE_GLOB);
		filter.addDataScheme("content");
		filter.addDataAuthority("gmail-ls", null);
		
		registerReceiver(getNotificationReceiver(), filter);
	}

	public void deregisterNotificationBroadcast() {
		if (_receiver != null) {
			unregisterReceiver(getNotificationReceiver());
		}
	}

	private synchronized GmailNotificationReceiver getNotificationReceiver() {
		if (_receiver == null) {
			_receiver = new GmailNotificationReceiver();
		}

		return _receiver;
	}
	
	private void checkAndRegisterNotificationBroadcast() {
		SharedPreferences pref =
			PreferenceManager.getDefaultSharedPreferences(this);
		
		String prefKey = 
			getResources().getString(R.string.pref_show_notification);
		
		boolean showNotifications = pref.getBoolean(prefKey, false);
		
		if (showNotifications && compatibleGmailVersion()) {
			registerNotificationBroadcast();
		}
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
