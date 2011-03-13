/*
 *  Android Libraries contains usefull classes for the Android applications
 *  development.
 *  Copyright (C) 2011  Luc Chante <luc.chante@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ldev.nbpicker.widget;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

/**
 * This class exists purely to cancel long click events, that got
 * started in NumberPicker
 * 
 * A big part of this class is taken from The Android Open Source Project.
 */
class NumberPickerButton extends ImageButton implements View.OnClickListener, View.OnLongClickListener {
	
	static final int BTN_UP = 1;
	static final int BTN_DOWN = -1;
	
	private static final int mRDrawableBtnUp;
	private static final int mRDrawableBtnDonw;
	static {
		int btnUp = 0;
		int btnDown = 0;
		try {
			btnUp = Class.forName("com.android.internal.R$drawable").getField("timepicker_up_btn").getInt(null);
			btnDown = Class.forName("com.android.internal.R$drawable").getField("timepicker_down_btn").getInt(null);
		} catch (Exception e) {
		}
		mRDrawableBtnUp = btnUp;
		mRDrawableBtnDonw = btnDown;
	}
	
    private final NumberPicker mNumberPicker;
    private final int mIncremental;

    NumberPickerButton(final NumberPicker picker, int increment) {
    	super(picker.getContext());
        mNumberPicker = picker;
        mIncremental = increment;
        setOnClickListener(this);
        setOnLongClickListener(this);
        
        if (BTN_UP == increment) {
        	setBackgroundResource(mRDrawableBtnUp);
        } else if (BTN_DOWN == increment) {
        	setBackgroundResource(mRDrawableBtnDonw);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        cancelLongpressIfRequired(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        cancelLongpressIfRequired(event);
        return super.onTrackballEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                || (keyCode == KeyEvent.KEYCODE_ENTER)) {
            cancelLongpress();
        }
        return super.onKeyUp(keyCode, event);
    }

    private void cancelLongpressIfRequired(MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_CANCEL)
                || (event.getAction() == MotionEvent.ACTION_UP)) {
            cancelLongpress();
        }
    }

    private void cancelLongpress() {
    	mNumberPicker.cancelIncrement();
    }

	@Override
	public void onClick(View v) {
		mNumberPicker.validateInput();
		mNumberPicker.requestInputFocus();
		mNumberPicker.increment(mIncremental);
	}

	@Override
	public boolean onLongClick(View v) {
		mNumberPicker.startIncrement(mIncremental);
		return true;
	}
}
