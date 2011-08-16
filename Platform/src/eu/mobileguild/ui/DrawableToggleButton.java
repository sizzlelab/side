package eu.mobileguild.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

public class DrawableToggleButton extends CompoundButton {

    public DrawableToggleButton(Context context) {
        this(context, null);
    }

    public DrawableToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
	
	public DrawableToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
}
