package com.codder.ultimate.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ActivityComplainDetailsBinding;
import com.codder.ultimate.modelclass.ComplainRoot;
import com.google.gson.Gson;

public class ComplainDetailsActivity extends BaseActivity {
    ActivityComplainDetailsBinding binding;
    private ComplainRoot.ComplainItem ticket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_complain_details);
        
        Intent intent = getIntent();
        String ticketStr = intent.getStringExtra("ticket");
        if (ticketStr != null) {
            ticket = new Gson().fromJson(ticketStr, ComplainRoot.ComplainItem.class);
            if (ticket != null) {
                setData();
            }
        }
    }

    private void setData() {
        binding.tvTitle.setText(ticket.getContact());
        binding.tvDescription.setText(ticket.getMessage());

        binding.tvTime.setText(ticket.getCreatedAt());
        if (ticket.isSolved()) {
            binding.status.setText(R.string.solved);
            binding.status.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
        } else {
            binding.status.setText( getString(R.string.open));
            binding.status.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.pink));
        }
        if (ticket.getImage().equals("")) {
            binding.imageview.setVisibility(View.GONE);
            binding.tvImage.setVisibility(View.GONE);
        } else {

            Glide.with(this).load(BuildConfig.BASE_URL + ticket.getImage())
                    .apply(MainApplication.requestOptions)
                    .override(500,500)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.imageview);
        }
    }

}