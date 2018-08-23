# NewSearchView
A material search view which uses principles from Material Design. *Works from Android API 14 and above*.

# Usage
**Add jitpack in your root build.gradle at the end of repositories:**
```javascript
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Add dependency in your app's build.gradle file**
```javascript
	dependencies {
	        implementation 'com.github.hayahyts:NewSearchView:0.5.0'
	}
```

**Add NewSearchView to your layout file**
```xml
   <android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.nfortics.searchview.NewSearchView
        android:id="@+id/search_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <Button
        android:id="@+id/button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/click_me"
        app:layout_constraintTop_toBottomOf="@id/search_bar" />
     </android.support.constraint.ConstraintLayout>
```

**Get NewSearchView in your Activity or Fragment**
```java
  List<String> suggestions = new ArrayList<>();
        suggestions.add("SpaceWork");
        suggestions.add("Google");
        suggestions.add("Tesla");
        suggestions.add("AirBnb");

        NewSearchView searchView = findViewById(R.id.search_bar);
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
```

# Use VoiceSearch
**Allow/Disable it in the code:**
```java
	searchView.allowVoiceSearch(true); //or false
```
**Handle the response:**
```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NewSearchView.RC_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
```
# Style it
```xml
    <style name="NewSearchView">
        <!-- Background for the search bar -->
        <item name="searchBackground">@color/theme_primary</item>

        <!-- Change voice icon -->
        <item name="searchVoiceIcon">@drawable/ic_action_voice_search_inverted</item>

        <!-- Change clear text icon -->
        <item name="searchCloseIcon">@drawable/ic_action_navigation_close_inverted</item>

        <!-- Change up icon -->
        <item name="searchBackIcon">@drawable/ic_action_navigation_arrow_back_inverted</item>
       
        <!-- Change background for the suggestions list view -->
        <item name="searchSuggestionBackground">@android:color/white</item>

        <!-- Change text color for edit text. This will also be the color of the cursor -->
        <item name="android:textColor">@color/theme_primary_text_inverted</item>

        <!-- Change hint text color for edit text -->
        <item name="android:textColorHint">@color/theme_secondary_text_inverted</item>

        <!-- Hint for edit text -->
        <item name="android:hint">@string/search_hint</item>
    </style>
```   

# License
	Copyright 2018 Aryeetey Solomon Aryeetey

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
