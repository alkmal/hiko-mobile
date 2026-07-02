package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemLivestreamCommentBinding;
import com.codder.ultimate.live.model.PKLiveStreamComment;
import com.codder.ultimate.modelclass.UserRoot;

import java.util.ArrayList;
import java.util.List;

public class PKLiveStreamCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PKLiveStreamCommentAdapter";
    private static final int VIEW_TYPE_NOTICE = 1;
    private static final int VIEW_TYPE_COMMENT = 2;

    @NonNull
    private final List<PKLiveStreamComment> comments = new ArrayList<>();

    @Nullable
    private OnCommentClickListener onCommentClickListener;

    @Nullable
    private Context context;

    @Nullable
    private String hostLiveStreamingId;

    private boolean isPkOn = false;

    public interface OnCommentClickListener {
        void onClickComment(@NonNull UserRoot.User user);
    }

    @Override
    public int getItemViewType(int position) {
        return comments.get(position) == null ? VIEW_TYPE_NOTICE : VIEW_TYPE_COMMENT;
    }

    public void setOnCommentClickListener(@Nullable OnCommentClickListener listener) {
        this.onCommentClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_NOTICE) {
            View view = inflater.inflate(R.layout.item_livestream_comment_1, parent, false);
            return new NoticeViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_livestream_comment, parent, false);
            return new CommentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).bind(comments.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addSingleComment(@NonNull PKLiveStreamComment comment, @NonNull String hostId, boolean pkOn) {
        this.comments.add(0, comment);
        this.hostLiveStreamingId = hostId;
        this.isPkOn = pkOn;
        notifyItemInserted(0);
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {
        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemLivestreamCommentBinding binding;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemLivestreamCommentBinding.bind(itemView);
        }

        public void bind(@Nullable PKLiveStreamComment comment) {
            if (comment == null || context == null) return;

            UserRoot.User user = comment.getUser();

            if (user != null) {
                binding.tvName.setText(user.getName() != null ? user.getName() : "Unknown");

                if (user.getLevel() != null && user.getLevel().getImage() != null) {
                    Glide.with(context)
                            .load(BuildConfig.BASE_URL + user.getLevel().getImage())
                            .placeholder(R.drawable.ic_level_placeholder)
                            .error(R.drawable.ic_level_placeholder)
                            .into(binding.ivLevel);
                }else {
                    binding.ivLevel.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_level_placeholder));
                }
            }

            if ("reaction".equals(comment.getType())) {
                binding.tvComment.setText(R.string.reacted);
                binding.tvJoined.setVisibility(View.GONE);
                binding.imgReaction.setVisibility(View.VISIBLE);

                if (comment.getReaction() != null) {
                    Glide.with(context)
                            .load(comment.getReaction())
                            .into(binding.imgReaction);
                }

            } else if (comment.isJoined()) {
                String commentText = comment.getComment() != null ? comment.getComment() : "";

                if (comment.isJoined() && !commentText.isEmpty()) {
                    binding.tvJoined.setText(commentText);
                    binding.tvJoined.setTextColor(context.getResources().getColor(R.color.green_light));
                    binding.tvJoined.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_et_bg));
                    binding.tvName.setVisibility(View.GONE);
                    binding.tvJoined.setVisibility(View.VISIBLE);
                    binding.imgReaction.setVisibility(View.GONE);
                    binding.ivLevel.setVisibility(View.GONE);
                    binding.layUserImage.setVisibility(View.GONE);
                    binding.layTvComment.setVisibility(View.GONE);
                } else if (comment.isJoined()) {
                    binding.tvJoined.setText(R.string.enter_a_room);
                    binding.tvJoined.setTextColor(context.getResources().getColor(R.color.yellow));
                    if (user != null && user.getName() != null) {
                        binding.tvName.setText(user.getName());
                    } else {
                        binding.tvName.setText("");
                    }
                    binding.tvName.setVisibility(View.VISIBLE);
                    binding.tvJoined.setVisibility(View.VISIBLE);
                    binding.ivLevel.setVisibility(View.VISIBLE);
                    binding.imgReaction.setVisibility(View.GONE);
                    binding.layTvComment.setVisibility(View.GONE);
                    binding.layUserImage.setVisibility(View.VISIBLE);
                } else {
                    binding.tvJoined.setVisibility(View.GONE);
                    binding.tvName.setVisibility(View.VISIBLE);
                    binding.ivLevel.setVisibility(View.VISIBLE);
                    binding.imgReaction.setVisibility(View.GONE);
                    binding.layTvComment.setVisibility(View.VISIBLE);
                    binding.layUserImage.setVisibility(View.VISIBLE);

                    if (user != null && user.getName() != null) {
                        binding.tvName.setText(user.getName());
                    } else {
                        binding.tvName.setText("");
                    }
                    binding.tvComment.setText(commentText);

                }
            } else {
                binding.tvJoined.setVisibility(View.GONE);
                binding.layTvComment.setVisibility(View.VISIBLE);
                binding.ivLevel.setVisibility(View.VISIBLE);
                binding.imgReaction.setVisibility(View.GONE);
                binding.layUserImage.setVisibility(View.VISIBLE);
                binding.tvComment.setText(comment.getComment() != null ? comment.getComment() : "");

                if (comment.getComment().contains(" sent a request to join a call") || comment.getComment().contains("Request accepted")) {
                    binding.layTvComment.setBackgroundResource(R.drawable.comment_purple_bg);
                    binding.ivLevel.setVisibility(View.GONE);
                    binding.tvName.setVisibility(View.GONE);
                } else {
                    binding.layTvComment.setBackgroundResource(R.drawable.bg_comment_item);
                }
            }

            if (user != null && user.getImage() != null) {
                binding.imgUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 10);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (onCommentClickListener != null && user != null) {
                    onCommentClickListener.onClickComment(user);
                }
            });

            Log.d(TAG, "bind: isPkOn = " + isPkOn);
            Log.d(TAG, "bind: hostLiveStreamingId = " + hostLiveStreamingId);
            Log.d(TAG, "bind: commentLiveId = " + comment.getLiveStreamingId());
            Log.d(TAG, "bind: avatarFrameImage = " + (user != null ? user.getAvatarFrameImage() : "null"));
        }
    }
}
