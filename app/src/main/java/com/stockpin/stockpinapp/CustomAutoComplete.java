package com.stockpin.stockpinapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

/**
 * This is a custom autocomplete text view class with additional functionality
 */
public class CustomAutoComplete extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {

    public CustomAutoComplete(Context context) {
        super(context);
    }

    public CustomAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomAutoComplete(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //This will prevent autocomplete from closing with keyboard
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing()) {
            InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputManager.hideSoftInputFromWindow(findFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS)){
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
