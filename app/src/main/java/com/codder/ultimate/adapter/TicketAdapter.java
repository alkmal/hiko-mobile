package com.codder.ultimate.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.ComplainDetailsActivity;
import com.codder.ultimate.databinding.ItemTicketBinding;
import com.codder.ultimate.modelclass.ComplainRoot;
import com.google.gson.Gson;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final List<ComplainRoot.ComplainItem> dataList;

    public TicketAdapter(@NonNull List<ComplainRoot.ComplainItem> dataList) {
        this.dataList = dataList != null ? dataList : List.of();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTicketBinding binding = ItemTicketBinding.inflate(inflater, parent, false);
        return new TicketViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        private final ItemTicketBinding binding;

        public TicketViewHolder(@NonNull ItemTicketBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ComplainRoot.ComplainItem Ticket) {
            Context context = binding.getRoot().getContext();

            if (Ticket == null) return;

            String message = Ticket.getMessage();
            String createdAt = Ticket.getCreatedAt();
            boolean isSolved = Ticket.isSolved();

            binding.tvTitle.setText(message != null ? message : context.getString(R.string.no_message_available));
            binding.tvTime.setText(createdAt != null ? createdAt : context.getString(R.string.no_date));

            if (isSolved) {
                binding.status.setText(context.getString(R.string.solved));
            } else {
                binding.status.setText(context.getString(R.string.open));
            }

            binding.getRoot().setOnClickListener(v -> {
                try {
                    String ticketJson = new Gson().toJson(Ticket);
                    Intent intent = new Intent(context, ComplainDetailsActivity.class);
                    intent.putExtra("ticket", ticketJson);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

