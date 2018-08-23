package com.aryeetey.newsearchview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nfortics.searchview.NewSearchView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> suggestions = new ArrayList<>();
        suggestions.add("SpaceWork");
        suggestions.add("Google");
        suggestions.add("Tesla");
        suggestions.add("AirBnb");

        NewSearchView searchView = findViewById(R.id.search_bar);
        searchView.allowVoiceSearch(true);
        searchView.setSuggestions(suggestions);
        searchView.setSubmitOnClick(true);
        searchView.setOnQueryTextListener(new NewSearchView.QueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                log(query + " was submitted!");
                return true;
            }

            @Override
            public void onQueryTextChange(String newText) {
                log(newText + " is the new text!");
            }
        });
        searchView.setItemListener(suggestion -> log(suggestion + " was clicked!"));
    }

    private void log(String message) {
        Log.d(TAG, message);
    }
}
