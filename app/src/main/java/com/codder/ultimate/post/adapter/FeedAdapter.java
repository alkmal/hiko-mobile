package com.codder.ultimate.post.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.bottomsheets.BottomSheetReport;
import com.codder.ultimate.bottomsheets.BottomSheetReportOption;
import com.codder.ultimate.bottomsheets.BottomsheetGuestUserProfile;
import com.codder.ultimate.databinding.ItemFeedBinding;
import com.codder.ultimate.databinding.ItemSearchUsersBinding;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.guestuser.adapter.SearchUserAdapter;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.utils.like.LikeButton;
import com.codder.ultimate.utils.like.OnLikeListener;

import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends ListAdapter<PostRoot.PostItem, FeedAdapter.FeedViewHolder> {

    private final Context context;
    private OnPostClickListener listener;
    private OnFollowClickListener onFollowClickListener;
    public FeedAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;

    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setOnUserClickListener(OnFollowClickListener listener) {
        this.onFollowClickListener = listener;
    }


    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemFeedBinding binding = ItemFeedBinding.inflate(inflater, parent, false);
        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        PostRoot.PostItem item = getItem(position);
        if (item != null) {
            holder.bind(item, position);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains("like_change")) {
            holder.updateLikeUI(getItem(position));
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }


    public void updateItem(int position, PostRoot.PostItem updatedItem) {
        List<PostRoot.PostItem> currentList = new ArrayList<>(getCurrentList());
        if (position >= 0 && position < currentList.size()) {
            currentList.set(position, updatedItem);
            submitList(currentList); // Optionally avoid this
            notifyItemChanged(position, "like_change");
        }
    }

    public interface OnPostClickListener {
        void onLikeClick(PostRoot.PostItem post, int position, ItemFeedBinding binding);
        void onCommentListClick(PostRoot.PostItem post);
        void onLikeListClick(PostRoot.PostItem post);
        void onShareClick(PostRoot.PostItem post);
        void onMentionClick(String userName);
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ItemFeedBinding binding;

        FeedViewHolder(ItemFeedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("ClickableViewAccessibility")
        void bind(PostRoot.PostItem post, int position) {
            Glide.with(binding.getRoot())
                    .load(BuildConfig.BASE_URL + post.getPost())
                    .apply(MainApplication.requestOptionsFeed)
                    .placeholder(R.drawable.feed_placeholder)
                    .into(binding.imagePost);

            GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("DoubleTap", "Double tap detected on post: " + post.getId());
                    showHeartAnimation(binding);

                    if (!post.isUserLiked() && !post.isLikeInProgress()) {
                        if (listener != null) {
                            listener.onLikeClick(post, getAdapterPosition(), binding);
                        }
                    }
                    return true;
                }

            });

            binding.imagePost.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return false;
            });


            binding.imgUser.setUserImage(post.getUserImage(), post.getAvatarFrameImage(), 16);
            binding.imgUser.setOnClickListener(v -> {

                BottomsheetGuestUserProfile bs = new BottomsheetGuestUserProfile(post.getUserId(),false);
                bs.show(((AppCompatActivity) context).getSupportFragmentManager(), "guestProfile");
            });

            binding.tvCaption1.setText(post.getCaption());
            binding.tvCaption1.setVisibility(post.getCaption().isEmpty() ? GONE : VISIBLE);
            binding.tvLocation.setText(post.getTime());
            binding.tvUserName.setText(post.getName());
            binding.tvTime.setText(post.getTime());
            binding.tvComments.setText(post.getComment() + " ");
            binding.tvLikes.setText(post.getLikeCount() + " ");
//            binding.svgWebView.setVisibility(post.getLocation() == null ? View.GONE : View.VISIBLE);

            binding.tvCaption1.setHashtagEnabled(true);
            binding.tvCaption1.setMentionEnabled(true);
            binding.tvCaption1.setHashtagColor(ContextCompat.getColor(context, R.color.text_gray));
            binding.tvCaption1.setMentionColors(ContextCompat.getColorStateList(context, R.color.tintColor));

            float scale = BaseActivity.isRTL(context) ? -1f : 1f;
            binding.imgMessage.setScaleX(scale);

            binding.btnShare.setOnClickListener(view -> {
                listener.onShareClick(post);
            });

            binding.likeButton.setLiked(post.isUserLiked());
            binding.lytComments.setVisibility(post.isAllowComment() ? VISIBLE : GONE);

            binding.lytComments.setOnClickListener(v -> {
                if (listener != null) listener.onCommentListClick(post);
            });

            binding.tvLikes.setOnClickListener(v -> {
                if (listener != null) listener.onLikeListClick(post);
            });


            binding.tvCaption1.setOnMentionClickListener((view, text) -> {
                if (listener != null) listener.onMentionClick(text.toString());
            });

            binding.btnReport.setOnClickListener(v -> onClickReport(post));

            binding.likeButton.setLiked(post.isUserLiked());
            binding.likeButton.setEnabled(!post.isLikeInProgress());
            binding.tvLikes.setText(post.getLikeCount() + " ");

            binding.likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    listener.onLikeClick(post, position, binding);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    listener.onLikeClick(post, position, binding);
                }
            });


            binding.pd.setVisibility(GONE);

            if (post.isFollow()) {

                binding.tvFollow.setVisibility(GONE);
            } else {
                binding.tvFollow.setVisibility(VISIBLE);
                binding.tvFollow.setText(R.string.follow);
                binding.tvFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_bg));
                binding.tvFollow.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_follow, 0, 0, 0);
            }

            binding.tvFollow.setOnClickListener(v -> {
                if (onFollowClickListener != null) {
                    onFollowClickListener.onFollowClick(post, binding, position);
                }
            });


        }

        void updateLikeUI(PostRoot.PostItem post) {
            binding.likeButton.setLiked(post.isUserLiked());
            binding.likeButton.setEnabled(!post.isLikeInProgress());
            binding.tvLikes.setText(String.valueOf(post.getLikeCount()));
        }

    }

    private void showHeartAnimation(ItemFeedBinding binding) {
        binding.heartAnimation.setVisibility(VISIBLE);
        binding.heartAnimation.setAlpha(1f);
        binding.heartAnimation.setScaleX(0f);
        binding.heartAnimation.setScaleY(0f);

        binding.heartAnimation.animate()
                .alpha(1f)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(300)
                .withEndAction(() -> binding.heartAnimation.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> binding.heartAnimation.setVisibility(GONE))
                        .start())
                .start();
    }

    private void onClickReport(PostRoot.PostItem post) {
        if (post == null) return;
        new BottomSheetReportOption(context, new BottomSheetReportOption.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport(context, post.getUserId(), () ->
                        Toast.makeText(context, R.string.we_will_take_immediately_action_for_this_user_thank_you, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onBlocked() {
                List<PostRoot.PostItem> currentList = new ArrayList<>(getCurrentList());
                currentList.remove(post);
                submitList(currentList);
            }
        });
    }

    private static final DiffUtil.ItemCallback<PostRoot.PostItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull PostRoot.PostItem oldItem, @NonNull PostRoot.PostItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PostRoot.PostItem oldItem, @NonNull PostRoot.PostItem newItem) {
            boolean result = oldItem.equals(newItem);
            Log.d("FeedAdapter", "Comparing items: " + oldItem.getId() + " -> " + result);
            return result;
        }
    };

    public interface OnFollowClickListener {
        void onFollowClick(@NonNull PostRoot.PostItem user, @NonNull ItemFeedBinding binding, int position);

    }

}