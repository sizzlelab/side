package eu.mobileguild.ui;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fi.hut.soberit.sensors.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.DatePicker;
import android.widget.EditText;

/**
 * A {@link Preference} that allows for string
 * input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog. This {@link EditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any EditText
 * attributes on the EditTextPreference.
 * <p>
 * This preference will store a string into the SharedPreferences.
 * <p>
 * See {@link android.R.styleable#EditText EditText Attributes}.
 */
public class DatePickerPreference extends DialogPreference {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
	/**
     * The edit text shown in the dialog.
     */
    private DatePicker mDatePicker;
        
    final Calendar calendar = Calendar.getInstance();

    
    public DatePickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mDatePicker = new DatePicker(context, attrs);
        
        // Give it an ID so it can be saved/restored
        mDatePicker.setId(R.id.dialog_datepicker);
        
        /*
         * The preference framework and view framework both have an 'enabled'
         * attribute. Most likely, the 'enabled' specified in this XML is for
         * the preference framework, but it was also given to the view framework.
         * We reset the enabled state.
         */
        mDatePicker.setEnabled(true);
                
    }

    public DatePickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextPreferenceStyle);
        
    }

    public DatePickerPreference(Context context) {
        this(context, null);
    }
    
    /**
     * Saves the text to the {@link SharedPreferences}.
     * 
     * @param text The text to save
     */
    public void setDate(Date date) {
        final boolean wasBlocking = shouldDisableDependents();
        
        calendar.setTime(date);
        
        persistString((String) DateFormat.format(DATE_FORMAT, calendar.getTime()));
        
        final boolean isBlocking = shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
        
        notifyChanged();
    }
    
    private void setDate(String text) {
    	try {
			setDate(dateFormat.parse(text));
		} catch (ParseException e) {
			// programming error somewhere
			throw new RuntimeException(e);
		}		
	}
    
    /**
     * Gets the text from the {@link SharedPreferences}.
     * 
     * @return The current preference value.
     */
    public Date getDate() {
    	calendar.set(Calendar.YEAR, mDatePicker.getYear());
    	calendar.set(Calendar.MONTH, mDatePicker.getMonth());
    	calendar.set(Calendar.DAY_OF_MONTH, mDatePicker.getDayOfMonth());
        return calendar.getTime();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        DatePicker datePicker = mDatePicker;
        
        datePicker.updateDate(
        		calendar.get(Calendar.YEAR), 
        		calendar.get(Calendar.MONTH), 
        		calendar.get(Calendar.DAY_OF_MONTH));
        
        ViewParent oldParent = datePicker.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(datePicker);
            }
            onAddEditTextToDialogView(view, datePicker);
        }
    }
    protected void onAddEditTextToDialogView(View dialogView, DatePicker datePicker) {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.datepicker_container);
        if (container != null) {
            container.addView(datePicker, ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {

        	final Date value = getDate();
            if (callChangeListener(value)) {
                setDate(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    	final String currentString = dateFormat.format(calendar.getTime());
    	
    	final String initialValue = restoreValue ? getPersistedString(currentString) : (String) defaultValue; 
    	
        setDate(initialValue);
    }

    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
//        if (isPersistent()) {
//            // No need to save instance state since it's persistent
//            return superState;
//        }
        
        final SavedState myState = new SavedState(superState);
        myState.text = dateFormat.format(calendar.getTime());
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
         
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setDate(myState.text);
        notifyChanged();
    }

	private static class SavedState extends BaseSavedState {
        String text;
        
        public SavedState(Parcel source) {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    
}
