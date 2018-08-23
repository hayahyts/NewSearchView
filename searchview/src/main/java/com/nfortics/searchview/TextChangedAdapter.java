package com.nfortics.searchview;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * @author Solomon on 3/30/2018.
 */

interface TextChangedAdapter extends TextWatcher {
    @Override
    default void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    default void onTextChanged(CharSequence s, int start, int before, int count) {
        onTextWatcherTextChanged(s);
    }

    @Override
    default void afterTextChanged(Editable s) {
    }

    void onTextWatcherTextChanged(CharSequence newText);
}
