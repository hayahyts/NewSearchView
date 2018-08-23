package com.nfortics.searchview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Aryeetey Solomon Aryeetey
 */
public class NewSearchView extends FrameLayout {
    public static final int RC_VOICE = 100;
    private static final String SUGGESTIONS_KEY = "suggestions";
    private static final String TAG = NewSearchView.class.getSimpleName();

    // Views
    private View scrimView;
    private RecyclerView suggestions;
    private EditText searchTextView;
    private ImageButton backBtn;
    private ImageButton voiceBtn;
    private ImageButton emptyBtn;
    private ConstraintLayout inputField;
    private View bottomPanel;
    private View toolbar;

    private boolean isClearingFocus;

    private CharSequence previousText;
    private CharSequence currentText;

    private QueryTextListener queryTextListener;
    private SuggestionsAdapter adapter;
    private OnItemListener itemListener;

    private SavedState mSavedState;
    private boolean submitText = false;

    private boolean isVoiceSearchAllowed;

    private Context context;
    private View searchBar;
    private TextView exception;
    private final OnClickListener onClickListener = new OnClickListener() {

        public void onClick(View v) {
            if (v == voiceBtn) {
                onVoiceClicked();
            } else if (v == emptyBtn) {
                searchTextView.setText(null);
            } else if (v == searchTextView) {
                showSuggestions();
            } else if (v == scrimView) {
                hideSuggestions();
            }
        }
    };

    public NewSearchView(Context context) {
        this(context, null);
    }

    public NewSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);

        this.context = context;

        setupViews();

        initStyle(attrs, defStyleAttr);
    }

    private void initStyle(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NewSearchView, defStyleAttr, 0);

        if (a != null) {
            if (a.hasValue(R.styleable.NewSearchView_searchBackground)) {
                setBackground(a.getDrawable(R.styleable.NewSearchView_searchBackground));
            }

            if (a.hasValue(R.styleable.NewSearchView_android_textColor)) {
                setTextColor(a.getColor(R.styleable.NewSearchView_android_textColor, 0));
            }

            if (a.hasValue(R.styleable.NewSearchView_android_textColorHint)) {
                setHintTextColor(a.getColor(R.styleable.NewSearchView_android_textColorHint, 0));
            }

            if (a.hasValue(R.styleable.NewSearchView_android_hint)) {
                setHint(a.getString(R.styleable.NewSearchView_android_hint));
            }

            if (a.hasValue(R.styleable.NewSearchView_searchVoiceIcon)) {
                setVoiceIcon(a.getDrawable(R.styleable.NewSearchView_searchVoiceIcon));
            }

            if (a.hasValue(R.styleable.NewSearchView_searchCloseIcon)) {
                setCloseIcon(a.getDrawable(R.styleable.NewSearchView_searchCloseIcon));
            }

            if (a.hasValue(R.styleable.NewSearchView_searchBackIcon)) {
                setBackIcon(a.getDrawable(R.styleable.NewSearchView_searchBackIcon));
            }

            if (a.hasValue(R.styleable.NewSearchView_searchSuggestionBackground)) {
                setSuggestionBackground(a.getDrawable(R.styleable.NewSearchView_searchSuggestionBackground));
            }

            if (a.hasValue(R.styleable.NewSearchView_android_inputType)) {
                setInputType(a.getInt(R.styleable.NewSearchView_android_inputType, EditorInfo.TYPE_NULL));
            }

            a.recycle();
        }
    }

    private void setupViews() {
        LayoutInflater.from(context).inflate(R.layout.main_view, this, true);
        searchBar = findViewById(R.id.search_bar);

        inputField = searchBar.findViewById(R.id.input_field);
        suggestions = searchBar.findViewById(R.id.suggestions);
        searchTextView = searchBar.findViewById(R.id.searchTextView);
        backBtn = searchBar.findViewById(R.id.back_btn);
        voiceBtn = searchBar.findViewById(R.id.voice_btn);
        emptyBtn = searchBar.findViewById(R.id.clear_btn);
        scrimView = searchBar.findViewById(R.id.scrim_view);
        bottomPanel = searchBar.findViewById(R.id.bottomPanel);
        exception = searchBar.findViewById(R.id.exception);
        toolbar = searchBar.findViewById(R.id.toolbar_background);

        searchTextView.setOnClickListener(onClickListener);
        backBtn.setOnClickListener(onClickListener);
        voiceBtn.setOnClickListener(onClickListener);
        emptyBtn.setOnClickListener(onClickListener);
        scrimView.setOnClickListener(onClickListener);

        isVoiceSearchAllowed = false;

        showVoice(true);

        setupSearchView();

        adapter = new SuggestionsAdapter(new ArrayList<>(), getSuggestionHistory(), new SuggestionsAdapter.SuggestionListener() {
            @Override
            public void onSuggestionClicked(String suggestion) {
                suggestionClicked(suggestion);
            }

            @Override
            public void removeSuggestionFromHistory(String suggestion) {
                NewSearchView.this.removeSuggestionFromHistory(suggestion);
            }
        });
        suggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestions.setAdapter(adapter);
        suggestions.setVisibility(GONE);
    }

    private void setupSearchView() {
        searchTextView.setOnEditorActionListener((v, actionId, event) -> {
            submitQuery();
            return true;
        });

        searchTextView.addTextChangedListener((TextChangedAdapter) s -> {
            currentText = s;
            NewSearchView.this.onTextChanged(s);
        });

        searchTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(searchTextView);
                showSuggestions();
            }
        });
    }

    private void onVoiceClicked() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");    // user hint
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);    // setting recognition model, optimized for short phrases – search queries
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);    // quantity of results we want to receive
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, RC_VOICE);
        }
    }

    private void onTextChanged(CharSequence newText) {
        CharSequence text = searchTextView.getText();
        currentText = text;
        boolean hasText = !TextUtils.isEmpty(text);
        if (hasText) {
            emptyBtn.setVisibility(VISIBLE);
            showVoice(false);
        } else {
            emptyBtn.setVisibility(GONE);
            showVoice(true);
        }

        adapter.filter(newText.toString());

        if (queryTextListener != null && !TextUtils.equals(newText, previousText)) {
            queryTextListener.onQueryTextChange(newText.toString());
        }
        previousText = newText.toString();
    }

    private void submitQuery() {
        CharSequence query = searchTextView.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (queryTextListener == null || !queryTextListener.onQueryTextSubmit(query.toString())) {
                searchTextView.setText(null);
            }
            addSuggestionToHistory(query.toString());
        }
    }

    private boolean isVoiceAvailable() {
        if (isInEditMode()) {
            return true;
        }
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() == 0;
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    private void suggestionClicked(String suggestion) {
        searchTextView.setText(suggestion);
        if (submitText) {
            submitQuery();
        }
        if (itemListener != null) {
            itemListener.onItemClicked(suggestion);
        }
        hideSuggestions();
    }

    //Public Attributes
    @Override
    public void setBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            inputField.setBackground(background);
        } else {
            inputField.setBackgroundDrawable(background);
        }
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        toolbar.setBackgroundColor(color);
    }

    public void setTextColor(@ColorInt int color) {
        searchTextView.setTextColor(color);
    }

    public void setHintTextColor(@ColorInt int color) {
        searchTextView.setHintTextColor(color);
    }

    public void setHint(CharSequence hint) {
        searchTextView.setHint(hint);
    }

    public void setVoiceIcon(Drawable drawable) {
        voiceBtn.setImageDrawable(drawable);
    }

    public void setCloseIcon(Drawable drawable) {
        emptyBtn.setImageDrawable(drawable);
    }

    public void setBackIcon(Drawable drawable) {
        backBtn.setImageDrawable(drawable);
    }

    public void setInputType(int inputType) {
        searchTextView.setInputType(inputType);
    }

    public void setSuggestionBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            suggestions.setBackground(background);
        } else {
            suggestions.setBackgroundDrawable(background);
        }
    }

    public void allowVoiceSearch(boolean voiceSearch) {
        isVoiceSearchAllowed = voiceSearch;
    }

    //Public Methods
    public void showSuggestions() {
        if (adapter != null && adapter.getCount() > 0 && suggestions.getVisibility() == GONE) {
            suggestions.setVisibility(VISIBLE);
            scrimView.setVisibility(VISIBLE);
            bottomPanel.setVisibility(VISIBLE);
            exception.setVisibility(GONE);
            searchBar.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            exception.setVisibility(VISIBLE);
        }
    }

    public void hideSuggestions() {
        suggestions.setVisibility(GONE);
        scrimView.setVisibility(GONE);
        bottomPanel.setVisibility(GONE);
        hideKeyboard(searchTextView);
        searchBar.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * Submit the query as soon as the user clicks the item.
     *
     * @param submit submitText state
     */
    @SuppressWarnings("unused")
    public void setSubmitOnClick(boolean submit) {
        this.submitText = submit;
    }

    /**
     * Set Adapter for suggestions list with the given suggestion array
     *
     * @param suggestions array of suggestions
     */
    public void setSuggestions(List<String> suggestions) {
        if (suggestions != null && suggestions.size() > 0)
            adapter.replaceData(suggestions, getSuggestionHistory());
    }

    /**
     * Calling this will set the query to search text box. if submitText is true, it'll submitText the query.
     *
     * @param query  the text to query
     * @param submit should submit as soon as query is set
     */
    public void setQuery(CharSequence query, boolean submit) {
        searchTextView.setText(query);
        if (query != null) {
            searchTextView.setSelection(searchTextView.length());
            currentText = query;
        }
        if (submit && !TextUtils.isEmpty(query)) {
            submitQuery();
        }
    }

    /**
     * if show is true, this will enable voice search. If voice is not available on the device, this method call has not effect.
     *
     * @param show true will show voice icon else hide it
     */
    public void showVoice(boolean show) {
        if (show && isVoiceAvailable() && isVoiceSearchAllowed) {
            voiceBtn.setVisibility(VISIBLE);
        } else {
            voiceBtn.setVisibility(GONE);
        }
    }

    /**
     * Set this listener to listen to Query Change events.
     *
     * @param listener listens to query changes
     */
    public void setOnQueryTextListener(QueryTextListener listener) {
        queryTextListener = listener;
    }

    public void setItemListener(OnItemListener itemListener) {
        this.itemListener = itemListener;
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // Don't accept focus if in the middle of clearing focus
        if (isClearingFocus) return false;
        // Check if SearchView is focusable.
        return isFocusable() && searchTextView.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public void clearFocus() {
        isClearingFocus = true;
        hideKeyboard(this);
        super.clearFocus();
        searchTextView.clearFocus();
        isClearingFocus = false;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        mSavedState = new SavedState(superState);
        mSavedState.query = currentText != null ? currentText.toString() : null;

        return mSavedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        mSavedState = (SavedState) state;

        setQuery(mSavedState.query, submitText);

        super.onRestoreInstanceState(mSavedState.getSuperState());
    }

    private void addSuggestionToHistory(String suggestion) {
        Set<String> suggestions = getSuggestionHistory();
        suggestions.add(suggestion);
        Log.d(TAG, suggestions.toString());
        Log.d(TAG, context.getPackageName());
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(SUGGESTIONS_KEY, suggestions);
        editor.apply();
    }

    private void removeSuggestionFromHistory(String suggestion) {
        Set<String> suggestions = getSuggestionHistory();
        suggestions.remove(suggestion);
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(SUGGESTIONS_KEY, suggestions);
        editor.apply();
    }

    private Set<String> getSuggestionHistory() {
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sp.getStringSet(SUGGESTIONS_KEY, new HashSet<>());
    }

    public interface QueryTextListener {

        /**
         * Called when the user submits the query. This could be due to a key press on the
         * keyboard or due to pressing a submitText button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submitText request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        boolean onQueryTextSubmit(String query);

        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         */
        void onQueryTextChange(String newText);
    }

    public interface OnItemListener {

        void onItemClicked(String suggestion);
    }

    static class SavedState extends BaseSavedState {
        //required field that makes Parcelables from a Parcel
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        String query;
        boolean isSearchOpen;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.query = in.readString();
            this.isSearchOpen = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(query);
            out.writeInt(isSearchOpen ? 1 : 0);
        }
    }
}