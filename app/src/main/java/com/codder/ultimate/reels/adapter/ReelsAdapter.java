package com.codder.ultimate.reels.adapter;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemFeedBinding;
import com.codder.ultimate.databinding.ItemReelsBinding;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.reels.model.ReliteRoot;
import com.codder.ultimate.utils.like.LikeButton;
import com.codder.ultimate.utils.like.OnLikeListener;

public class ReelsAdapter extends ListAdapter<ReliteRoot.VideoItem, ReelsAdapter.ReelsViewHolder> {
    private int playAtPosition = RecyclerView.NO_POSITION;
    private OnReelsVideoAdapterListener listener;

    public ReelsAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setListener(OnReelsVideoAdapterListener listener) {
        this.listener = listener;
    }


    public void playVideoAt(int newPosition) {
        if (playAtPosition == newPosition) return;

        int oldPosition = playAtPosition;
        playAtPosition = newPosition;

        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
        notifyItemChanged(newPosition);
    }


    @Override
    public ReelsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemReelsBinding binding = ItemReelsBinding.inflate(inflater, parent, false);
        return new ReelsViewHolder(binding, this); // pass adapter reference
    }


    @Override
    public void onBindViewHolder(@NonNull ReelsViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public static class ReelsViewHolder extends RecyclerView.ViewHolder {
        private final ItemReelsBinding binding;
        private final ReelsAdapter adapter;

        public ReelsViewHolder(ItemReelsBinding binding, ReelsAdapter adapter) {
            super(binding.getRoot());
            this.binding = binding;
            this.adapter = adapter;
        }

        public ItemReelsBinding getBinding() {
            return binding;
        }

        void bind(ReliteRoot.VideoItem reel, int position) {
            if (reel == null) return;

            binding.imgUser.setUserImage(reel.getUserImage(), reel.getAvatarFrameImage(), 16);

            binding.imgSong.setUserImage(reel.getUserImage(), "", 16);
            binding.tvUserName.setText(reel.getName());
            binding.tvDescription.setText(reel.getCaption());
            binding.tvLikeCount.setText(String.valueOf(reel.getLikeCount()));
            binding.tvCommentCount.setText(String.valueOf(reel.getComment()));
            binding.lytComments.setVisibility(reel.isAllowComment() ? View.VISIBLE : View.GONE);
            binding.likebtn.setLiked(reel.isLike());

            if (position == adapter.playAtPosition) {
                binding.buffering.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
                adapter.listener.onItemClick(binding, adapter.playAtPosition, 1);
                binding.lytSound.startAnimation(animation);
            }

            // Load per-item thumbnail (visible by default)
            binding.imgThumbnail.setVisibility(View.VISIBLE);
            String thumbUrl = reel.getScreenshot(); // backend still used in shareRelite()
            Glide.with(binding.getRoot())
                    .load(thumbUrl)
                    .placeholder(R.drawable.reel_placeholder)  // light placeholder
                    .into(binding.imgThumbnail);

            if (position == adapter.playAtPosition) {
                binding.buffering.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
                adapter.listener.onItemClick(binding, adapter.playAtPosition, 1);
                binding.lytSound.startAnimation(animation);
            }

            binding.btnShare.setOnClickListener(view -> {

            });

            binding.pd.setVisibility(View.GONE);

            if (reel.isFollow()) {
                binding.tvFollow.setVisibility(GONE);
            } else {
                binding.tvFollow.setVisibility(VISIBLE);
                binding.tvFollow.setText(R.string.follow);
                binding.tvFollow.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.btn_bg));
                binding.tvFollow.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_follow, 0, 0, 0);
            }

            binding.tvFollow.setOnClickListener(v -> {
                if (adapter.listener != null) {
                    adapter.listener.onFollowClick(reel, binding, position);
                }
            });



            setupListeners(reel, position);
        }

        private void setupListeners(ReliteRoot.VideoItem reel, int position) {
            binding.tvLikeCount.setOnClickListener(v -> adapter.listener.onClickLikeList(reel));
            binding.tvCommentCount.setOnClickListener(v -> adapter.listener.onClickCommentList(reel, position));
            binding.btnReport.setOnClickListener(v -> adapter.listener.onClickReport(reel));
            binding.btnShare.setOnClickListener(v -> adapter.listener.onClickShare(reel));
            binding.imgUser.setOnClickListener(v -> adapter.listener.onClickUser(reel));
            binding.imgComment.setOnClickListener(v -> adapter.listener.onClickCommentList(reel, position));
            binding.tvDescription.setOnMentionClickListener((view, text) -> adapter.listener.onMentionClick(text.toString()));

            binding.likebtn.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    adapter.listener.onClickLike(binding, position, reel);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    adapter.listener.onClickLike(binding, position, reel);
                }
            });

            GestureDetector detector = new GestureDetector(binding.getRoot().getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDown(MotionEvent e) {
                            Log.d("Gesture", "onDown");
                            return true;
                        }

                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {
                            Log.d("Gesture", "onSingleTapConfirmed");
                            adapter.listener.onItemClick(binding, position, 2);
                            return true;
                        }

                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            Log.d("Gesture", "onDoubleTap");
                            adapter.listener.onDoubleClick(reel, e, binding);
                            return true;
                        }
                    });


            binding.playerView.setOnTouchListener((v, event) -> {
                Log.d("Touch", "Event: " + event.getAction());
                return detector.onTouchEvent(event);
            });


        }
    }

    public static final DiffUtil.ItemCallback<ReliteRoot.VideoItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ReliteRoot.VideoItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ReliteRoot.VideoItem oldItem, @NonNull ReliteRoot.VideoItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ReliteRoot.VideoItem oldItem, @NonNull ReliteRoot.VideoItem newItem) {
            return oldItem.getLikeCount() == newItem.getLikeCount()
                    && oldItem.getComment() == newItem.getComment()
                    && oldItem.isLike() == newItem.isLike()
                    && oldItem.getCaption().equals(newItem.getCaption())
                    && oldItem.getName().equals(newItem.getName())
                    && oldItem.getUserImage().equals(newItem.getUserImage())
                    && oldItem.getAvatarFrameImage().equals(newItem.getAvatarFrameImage())
                    && oldItem.isAllowComment() == newItem.isAllowComment();
        }
    };


    public interface OnReelsVideoAdapterListener {
        void onItemClick(ItemReelsBinding binding, int pos, int type);

        void onClickUser(ReliteRoot.VideoItem reel);

        void onClickShare(ReliteRoot.VideoItem reel);

        void onClickCommentList(ReliteRoot.VideoItem reel, int pos);

        void onClickLikeList(ReliteRoot.VideoItem reel);

        void onHashTagClick(String hashTag);

        void onMentionClick(String userName);

        void onDoubleClick(ReliteRoot.VideoItem reel, MotionEvent e, ItemReelsBinding binding);

        void onClickLike(ItemReelsBinding binding, int pos, ReliteRoot.VideoItem reel);

        void onClickReport(ReliteRoot.VideoItem reel);
        void onFollowClick(@NonNull ReliteRoot.VideoItem reel, @NonNull ItemReelsBinding binding, int position);
    }

}
