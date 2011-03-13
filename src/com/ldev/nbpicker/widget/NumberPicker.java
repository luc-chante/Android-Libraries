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
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * A view for selecting a number
 * 
 * A big part of this class is taken from The Android Open Source Project.
 */
public class NumberPicker extends LinearLayout implements OnFocusChangeListener {

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
		void onChanged(NumberPicker picker, int oldVal, int newVal);
	}

	private static final int mRDrawableTimepickerInput;
	static {
		int timepickerInput = 0;
		try {
			timepickerInput = Class.forName("com.android.internal.R$drawable")
					.getField("timepicker_input").getInt(null);
		} catch (Exception e) {
		}
		mRDrawableTimepickerInput = timepickerInput;
	}

	/**
	 * Interface used to format the number into a string for presentation
	 */
	public interface Formatter {
		String toString(int value);
	}

	private int mStep = 0;
	private final Handler mHandler;
	private final Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (0 != mStep) {
				changeCurrent(mCurrent + mStep);
				mHandler.postDelayed(this, mSpeed);
			}
		}
	};

	private final EditText mText;
	private final InputFilter mNumberInputFilter;

	private String[] mDisplayedValues;

	/**
	 * Lower value of the range of numbers allowed for the NumberPicker
	 */
	private int mStart;

	/**
	 * Upper value of the range of numbers allowed for the NumberPicker
	 */
	private int mEnd;

	/**
	 * Current value of this NumberPicker
	 */
	private int mCurrent;

	/**
	 * Previous value of this NumberPicker.
	 */
	private int mPrevious;
	private OnChangedListener mListener;
	private Formatter mFormatter;
	private long mSpeed = 200;

	/**
	 * If the range is circular, or not.
	 */
	private boolean mCircular;

	/**
	 * Create a new number picker
	 * 
	 * @param context
	 *            the application environment
	 */
	public NumberPicker(Context context) {
		this(context, null);
	}

	/**
	 * Create a new number picker
	 * 
	 * @param context
	 *            the application environment
	 * @param attrs
	 *            a collection of attributes
	 */
	public NumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		mHandler = new Handler();

		InputFilter inputFilter = new NumberPickerInputFilter();
		mNumberInputFilter = new NumberRangeKeyListener();

		final LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		mIncrementButton = new NumberPickerButton(this,
				NumberPickerButton.BTN_UP);
		mDecrementButton = new NumberPickerButton(this,
				NumberPickerButton.BTN_DOWN);

		mText = new EditText(context);
		mText.setTextAppearance(context,
				android.R.style.TextAppearance_Large_Inverse);
		mText.setGravity(Gravity.CENTER);
		mText.setLines(1);
		ColorStateList color = getResources().getColorStateList(
				android.R.color.primary_text_light);
		mText.setTextColor(color);
		mText.setOnFocusChangeListener(this);
		mText.setFilters(new InputFilter[] { inputFilter });
		mText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		mText.setBackgroundResource(mRDrawableTimepickerInput);

		addView(mIncrementButton, params);
		addView(mText, params);
		addView(mDecrementButton, params);

		if (!isEnabled()) {
			setEnabled(false);
		}
	}

	@Override
	public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
		return mText.requestFocus();
	}

	/**
	 * Set the enabled state of this view. The interpretation of the enabled
	 * state varies by subclass.
	 * 
	 * @param enabled
	 *            True if this view is enabled, false otherwise.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mIncrementButton.setEnabled(enabled);
		mDecrementButton.setEnabled(enabled);
		mText.setEnabled(enabled);
	}

	/**
	 * Set the callback that indicates the number has been adjusted by the user.
	 * 
	 * @param listener
	 *            the callback, should not be null.
	 */
	public void setOnChangeListener(OnChangedListener listener) {
		mListener = listener;
	}

	/**
	 * Set the formatter that will be used to format the number for presentation
	 * 
	 * @param formatter
	 *            the formatter object. If formatter is null, String.valueOf()
	 *            will be used
	 */
	public void setFormatter(Formatter formatter) {
		mFormatter = formatter;
	}

	/**
	 * Set the range of numbers allowed for the number picker. The current value
	 * will be automatically set to the start.
	 * 
	 * @param start
	 *            the start of the range (inclusive)
	 * @param end
	 *            the end of the range (inclusive)
	 */
	public void setRange(int start, int end) {
		setRange(start, end, null/* displayedValues */);
	}

	/**
	 * Set the range of numbers allowed for the number picker. The current value
	 * will be automatically set to the start. Also provide a mapping for values
	 * used to display to the user.
	 * 
	 * @param start
	 *            the start of the range (inclusive)
	 * @param end
	 *            the end of the range (inclusive)
	 * @param displayedValues
	 *            the values displayed to the user.
	 */
	public void setRange(int start, int end, String[] displayedValues) {
		mDisplayedValues = displayedValues;
		mStart = start;
		mEnd = end;
		mCurrent = start;
		updateView();

		if (displayedValues != null) {
			// Allow text entry rather than strictly numeric entry.
			mText.setRawInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		}
	}

	/**
	 * Set the current value for the number picker.
	 * 
	 * @param current
	 *            the current value the start of the range (inclusive)
	 * @throws IllegalArgumentException
	 *             when current is not within the range of of the number picker
	 */
	public void setCurrent(int current) {
		if (current < mStart || current > mEnd) {
			throw new IllegalArgumentException(
					"current should be >= start and <= end");
		}
		mCurrent = current;
		updateView();
	}

	/**
	 * Sets the speed at which the numbers will scroll when the +/- buttons are
	 * longpressed
	 * 
	 * @param speed
	 *            The speed (in milliseconds) at which the numbers will scroll
	 *            default 200ms
	 */
	public void setSpeed(long speed) {
		mSpeed = speed;
	}

	/**
	 * Sets if number pickers should be circular or not (restart from the
	 * beginning of the range exceeding the max value.
	 * 
	 * @param circular
	 */
	public void setCircularity(boolean circular) {
		mCircular = circular;
	}

	/**
	 * Returns the current value of the NumberPicker
	 * 
	 * @return the current value.
	 */
	public int getCurrent() {
		return mCurrent;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		/*
		 * When focus is lost check that the text field has valid values.
		 */
		if (!hasFocus) {
			validateInput();
		}
	}

	/**
	 * Sets the current value of this NumberPicker, and sets mPrevious to the
	 * previous value. If current is greater than mEnd less than mStart, the
	 * value of mCurrent is wrapped around.
	 * 
	 * Subclasses can override this to change the wrapping behavior
	 * 
	 * @param current
	 *            the new value of the NumberPicker
	 */
	protected void changeCurrent(int current) {
		// Wrap around the values if we go past the start or end
		if (current > mEnd) {
			current = mCircular ? mStart : mEnd;
		} else if (current < mStart) {
			current = mCircular ? mEnd : mStart;
		}
		mPrevious = mCurrent;
		mCurrent = current;
		if (mPrevious != mCurrent) {
			notifyChange();
			updateView();
		}
	}

	/**
	 * Returns the upper value of the range of the NumberPicker
	 * 
	 * @return the uppper number of the range.
	 */
	protected int getEndRange() {
		return mEnd;
	}

	/**
	 * Returns the lower value of the range of the NumberPicker
	 * 
	 * @return the lower number of the range.
	 */
	protected int getBeginRange() {
		return mStart;
	}

	/**
	 * Notifies the listener, if registered, of a change of the value of this
	 * NumberPicker.
	 */
	private void notifyChange() {
		if (mListener != null) {
			mListener.onChanged(this, mPrevious, mCurrent);
		}
	}

	/**
	 * Updates the view of this NumberPicker. If displayValues were specified in
	 * {@link #setRange}, the string corresponding to the index specified by the
	 * current value will be returned. Otherwise, the formatter specified in
	 * {@link setFormatter} will be used to format the number.
	 */
	private void updateView() {
		/*
		 * If we don't have displayed values then use the current number else
		 * find the correct value in the displayed values for the current
		 * number.
		 */
		if (mDisplayedValues == null) {
			mText.setText(formatNumber(mCurrent));
		} else {
			mText.setText(mDisplayedValues[mCurrent - mStart]);
		}
		mText.setSelection(mText.getText().length());
	}

	private String formatNumber(int value) {
		return (mFormatter != null) ? mFormatter.toString(value) : String
				.valueOf(value);
	}

	private void validateCurrentView(CharSequence str) {
		int val = getSelectedPos(str.toString());
		if ((val >= mStart) && (val <= mEnd)) {
			if (mCurrent != val) {
				mPrevious = mCurrent;
				mCurrent = val;
				notifyChange();
			}
		}
		updateView();
	}

	void increment(int inc) {
		changeCurrent(mCurrent + inc);
	}

	void startIncrement(int step) {
		validateInput();

		mStep = step;
		mHandler.post(mRunnable);
	}

	void validateInput() {
		String str = String.valueOf((mText).getText());
		if ("".equals(str)) {
			// Restore to the old value as we don't allow empty values
			updateView();
		} else {
			// Check the new value and ensure it's in range
			validateCurrentView(str);
		}
	}

	void cancelIncrement() {
		mStep = 0;
	}

	void requestInputFocus() {
		if (!mText.hasFocus())
			mText.requestFocus();
	}

	private static final char[] DIGIT_CHARACTERS = new char[] { '0', '1', '2',
			'3', '4', '5', '6', '7', '8', '9' };

	private NumberPickerButton mIncrementButton;
	private NumberPickerButton mDecrementButton;

	private class NumberPickerInputFilter implements InputFilter {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			if (mDisplayedValues == null) {
				return mNumberInputFilter.filter(source, start, end, dest,
						dstart, dend);
			}
			CharSequence filtered = String.valueOf(source.subSequence(start,
					end));
			String result = String.valueOf(dest.subSequence(0, dstart))
					+ filtered + dest.subSequence(dend, dest.length());
			String str = String.valueOf(result).toLowerCase();
			for (String val : mDisplayedValues) {
				val = val.toLowerCase();
				if (val.startsWith(str)) {
					return filtered;
				}
			}
			return "";
		}
	}

	private class NumberRangeKeyListener extends NumberKeyListener {

		// XXX This doesn't allow for range limits when controlled by a
		// soft input method!
		@Override
		public int getInputType() {
			return InputType.TYPE_CLASS_NUMBER;
		}

		@Override
		protected char[] getAcceptedChars() {
			return DIGIT_CHARACTERS;
		}

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {

			CharSequence filtered = super.filter(source, start, end, dest,
					dstart, dend);
			if (filtered == null) {
				filtered = source.subSequence(start, end);
			}

			String result = String.valueOf(dest.subSequence(0, dstart))
					+ filtered + dest.subSequence(dend, dest.length());

			if ("".equals(result)) {
				return result;
			}
			int val = getSelectedPos(result);

			/*
			 * Ensure the user can't type in a value greater than the max
			 * allowed. We have to allow less than min as the user might want to
			 * delete some numbers and then type a new number.
			 */
			if (val > mEnd) {
				return "";
			} else {
				return filtered;
			}
		}
	}

	private int getSelectedPos(String str) {
		if (mDisplayedValues == null) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				/* Ignore as if it's not a number we don't care */
			}
		} else {
			for (int i = 0; i < mDisplayedValues.length; i++) {
				/* Don't force the user to type in jan when ja will do */
				str = str.toLowerCase();
				if (mDisplayedValues[i].toLowerCase().startsWith(str)) {
					return mStart + i;
				}
			}

			/*
			 * The user might have typed in a number into the month field i.e.
			 * 10 instead of OCT so support that too.
			 */
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {

				/* Ignore as if it's not a number we don't care */
			}
		}
		return mStart;
	}
}
