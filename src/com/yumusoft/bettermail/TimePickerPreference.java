/* Copyright (C) 2011 Eduardo Gonzalez See LICENSE for details. */

package com.yumusoft.bettermail;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class TimePickerPreference extends DialogPreference 
		implements OnTimeChangedListener {
	
	private int _hour;
	private int _minute;

	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setPersistent(true);
	}
	
	@Override
	protected View onCreateDialogView() {
		TimePicker tp = new TimePicker(getContext());
		tp.setOnTimeChangedListener(this);
 
		tp.setCurrentHour(getPersistentHour());
		tp.setCurrentMinute(getPersistentMinute());

		return tp;
	}

	public void onTimeChanged(TimePicker view, int hour, int minute) {
		_hour = hour;
		_minute = minute;	
	}
	
	@Override
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			persistString(_hour + ":" + _minute);
		}
	}
	
	private int getPersistentHour() {
		String time = getPersistedString("00:00");
		return Integer.valueOf(time.split(":")[0]);
	}
 
	private int getPersistentMinute() {
		String time = getPersistedString("00:00");
		return Integer.valueOf(time.split(":")[1]);
	}
}
