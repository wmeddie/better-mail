package com.yumusoft.bettermail;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class NotificationPreference extends CheckBoxPreference {

	public NotificationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(true);
	}
	
	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		
		if (checked) {
			BetterMailApplication app = 
				(BetterMailApplication)getContext().getApplicationContext();
			app.registerNotificationBroadcast();
		} else {
			BetterMailApplication app = 
				(BetterMailApplication)getContext().getApplicationContext();
			app.deregisterNotificationBroadcast();
		}
	}
}
