package com.nfortics.searchview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder> {
    private List<String> originalSuggestions;
    private List<String> filteredSuggestions;
    private Set<String> suggestionHistory;
    private SuggestionListener listener;

    SuggestionsAdapter(List<String> originalSuggestions, Set<String> suggestionHistory, SuggestionListener listener) {
        this.originalSuggestions = originalSuggestions;
        this.listener = listener;
        filteredSuggestions = new ArrayList<>();
        this.filteredSuggestions.addAll(originalSuggestions);
        this.suggestionHistory = suggestionHistory;
    }

    @Override
    public SuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SuggestionViewHolder holder, int position) {
        holder.bind(filteredSuggestions.get(position));
    }

    @Override
    public int getItemCount() {
        if (filteredSuggestions.size() < 7) {
            return filteredSuggestions.size();
        }
        return 7;
    }

    public int getCount() {
        return originalSuggestions.size();
    }

    public void filter(String s) {
        filteredSuggestions.clear();
        if (s.isEmpty()) {
            filteredSuggestions.addAll(originalSuggestions);
        } else {
            s = s.toLowerCase();
            for (String suggestion : originalSuggestions) {
                if (suggestion.toLowerCase().contains(s)) {
                    filteredSuggestions.add(suggestion);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void replaceData(List<String> suggestions, Set<String> suggestionHistory) {
        this.originalSuggestions = suggestions;
        this.filteredSuggestions.addAll(originalSuggestions);
        this.suggestionHistory = suggestionHistory;
        notifyDataSetChanged();
    }

    public interface SuggestionListener {

        void onSuggestionClicked(String suggestion);

        void removeSuggestionFromHistory(String suggestion);
    }

    class SuggestionViewHolder extends RecyclerView.ViewHolder {
        private TextView suggestionTV;
        private ImageView removeImg;

        SuggestionViewHolder(View itemView) {
            super(itemView);

            suggestionTV = itemView.findViewById(R.id.suggestion);
            removeImg = itemView.findViewById(R.id.remove);
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onSuggestionClicked(filteredSuggestions.get(getAdapterPosition())));
                removeImg.setOnClickListener(v -> {
                    suggestionHistory.remove(filteredSuggestions.get(getAdapterPosition()));
                    listener.removeSuggestionFromHistory(filteredSuggestions.get(getAdapterPosition()));
                    notifyDataSetChanged();
                });
            }
        }

        void bind(String suggestion) {
            suggestionTV.setText(suggestion);
            if (suggestionHistory.contains(suggestion)) {
                removeImg.setVisibility(View.VISIBLE);
            } else {
                removeImg.setVisibility(View.GONE);
            }
        }
    }
}
