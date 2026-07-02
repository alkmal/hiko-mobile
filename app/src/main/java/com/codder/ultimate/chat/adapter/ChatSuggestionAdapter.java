package com.codder.ultimate.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.chat.modelclass.ChatSuggestion;

import java.util.List;

public class ChatSuggestionAdapter extends RecyclerView.Adapter<ChatSuggestionAdapter.ViewHolder> {

    private final List<ChatSuggestion.DataItem> suggestionList;
    private final OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String message);
    }

    public ChatSuggestionAdapter(List<ChatSuggestion.DataItem> suggestionList, OnSuggestionClickListener listener) {
        this.suggestionList = suggestionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatSuggestion.DataItem item = suggestionList.get(position);
        holder.tvSuggestion.setText(item.getMessage());
        holder.tvSuggestion.setOnClickListener(v -> listener.onSuggestionClick(item.getMessage()));
    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
        }
    }
}

