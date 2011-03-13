/*
 *  Android Libraries contains useful classes for the Android applications
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class RangeNumberPicker extends LinearLayout implements NumberPicker.OnChangedListener {
	
	/**
	 * The callback interface used to indicate the number value has been
	 * adjusted.
	 */
	public interface OnChangedListener {
		/**
		 * @param picker
		 *            The NumberPicker associated with this listener.
		 * @param oldVal
		 *            The previous value.
		 * @param newVal
		 *            The new value.
		 */
		void onChanged(int which, int oldVal, int newVal);
	}

	private static final LayoutParams mParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
	private static final int mIncrementId;
	private static final int mDecrementId;
	static {
		int incrementId = 0, decrementId = 0;
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$id");
			incrementId = clazz.getField("increment").getInt(null);
			decrementId = clazz.getField("decrement").getInt(null);
		} catch (Exception e) {
		}
		mIncrementId = incrementId;
		mDecrementId = decrementId;
	}
	
	public static final int PICKER_MIN = 0;
	public static final int PICKER_MAX = 1;
	
	private final NumberPicker mMinPicker;
	private final NumberPicker mMaxPicker;
	
	private OnChangedListener mListener;
	private boolean mAreLinked = true;

	public RangeNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMinPicker = new NumberPicker(context);
		mMinPicker.setId(mIncrementId);
		mMinPicker.setOnChangeListener(this);
		mMaxPicker = new NumberPicker(context);
		mMaxPicker.setId(mDecrementId);
		mMaxPicker.setOnChangeListener(this);
		super.addView(mMinPicker, 0, mParams);
		super.addView(mMaxPicker, 1, mParams);
	}

	public RangeNumberPicker(Context context) {
		this(context, null);
	}
	
	/**
     * Set the formatter that will be used to format the number for presentation
     * @param formatter the formatter object.  If formatter is null, String.valueOf()
     * will be used
     */
    public void setFormatter(NumberPicker.Formatter formatter) {
    	mMinPicker.setFormatter(formatter);
    	mMaxPicker.setFormatter(formatter);
    }

    /**
     * Set the range of numbers allowed for the number picker. The current
     * value will be automatically set to the start.
     *
     * @param start the start of the range (inclusive)
     * @param end the end of the range (inclusive)
     */
    public void setRange(int start, int end) {
        setRange(start, end, null/*displayedValues*/);
    }

    /**
     * Set the range of numbers allowed for the number picker. The current
     * value will be automatically set to the start. Also provide a mapping
     * for values used to display to the user.
     *
     * @param start the start of the range (inclusive)
     * @param end the end of the range (inclusive)
     * @param displayedValues the values displayed to the user.
     */
    public void setRange(int start, int end, String[] displayedValues) {
        mMinPicker.setRange(start, end, displayedValues);
        mMaxPicker.setRange(start, end, displayedValues);
        mMaxPicker.setCurrent(end);
    }

    /**
     * Set the current min value for the RangeNumberPicker.
     *
     * @param current the min current value the start of the range (inclusive)
     * @throws IllegalArgumentException when current is not within the range
     *         of of the RangeNumberPicker
     */
    public void setMinCurrent(int current) {
    	mMinPicker.setCurrent(current);
    }

    /**
     * Set the current max value for the RangeNumberPicker.
     *
     * @param current the max current value the start of the range (inclusive)
     * @throws IllegalArgumentException when current is not within the range
     *         of of the RangeNumberPicker
     */
    public void setMaxCurrent(int current) {
    	mMaxPicker.setCurrent(current);
    }

    /**
     * Sets the speed at which the numbers will scroll when the +/-
     * buttons are longpressed
     *
     * @param speed The speed (in milliseconds) at which the numbers will scroll
     * default 300ms
     */
    public void setSpeed(long speed) {
    	mMinPicker.setSpeed(speed);
    	mMaxPicker.setSpeed(speed);
    }

	public void setCircularity(boolean circular) {
		mMinPicker.setCircularity(circular);
    	mMaxPicker.setCircularity(circular);
	}
    
    public void setLinked(boolean linked) {
    	mAreLinked = linked;
    }
    
    public void setOnChangeListener(OnChangedListener listener) {
    	mListener = listener;
    }

    /**
     * Returns the current min value of the RangeNumberPicker
     * @return the current min value.
     */
    public int getMinCurrent() {
        return mMinPicker.getCurrent();
    }

    /**
     * Returns the current max value of the RangeNumberPicker
     * @return the current max value.
     */
    public int getMaxCurrent() {
        return mMaxPicker.getCurrent();
    }

	@Override
	public void onChanged(NumberPicker picker, int oldVal, int newVal) {
		if (null != mListener) {
			if (picker.getId() == mIncrementId) {
				int max = mMaxPicker.getCurrent();
				if (mAreLinked && max < newVal) {
					mMaxPicker.setCurrent(newVal);
				}
				mListener.onChanged(PICKER_MIN, oldVal, newVal);
			} else if (picker.getId() == mDecrementId) {
				int min = mMinPicker.getCurrent();
				if (mAreLinked && newVal < min) {
					mMinPicker.setCurrent(newVal);
				}
				mListener.onChanged(PICKER_MAX, oldVal, newVal);
			}
		}
	}

	// disabled some methods
	@Override
	public void addView(View child) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addView(View child, int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addView(View child, int width, int height) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		throw new UnsupportedOperationException();
	}
}
