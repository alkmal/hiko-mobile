package com.codder.ultimate.live.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
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
import com.codder.ultimate.live.model.LiveStramComment;
import com.codder.ultimate.modelclass.UserRoot;

import java.util.ArrayList;
import java.util.List;

public class LiveStramCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "LiveStramCommentAdapter";

    private static final int VIEW_TYPE_NOTICE = 1;
    private static final int VIEW_TYPE_COMMENT = 2;

    private final List<LiveStramComment> comments = new ArrayList<>();
    private Context context;

    @Nullable
    private OnCommentClickListener onCommentClickListener;

    public void setOnCommentClickListener(@Nullable OnCommentClickListener listener) {
        this.onCommentClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= comments.size() || comments.get(position) == null) {
            return VIEW_TYPE_NOTICE;
        }
        return VIEW_TYPE_COMMENT;
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
            ((CommentViewHolder) holder).bindData(position);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addSingleComment(@NonNull LiveStramComment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    public void setComments(@NonNull List<LiveStramComment> newComments) {
        comments.clear();
        comments.addAll(newComments);
        notifyDataSetChanged();
    }

    public interface OnCommentClickListener {
        void onClickComment(@NonNull UserRoot.User user);
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

        public void bindData(int position) {
            if (position < 0 || position >= comments.size()) return;

            LiveStramComment comment = comments.get(position);
            if (comment == null) return;

            UserRoot.User user = comment.getUser();

            if (user != null && user.getLevel() != null && user.getLevel().getImage() != null) {

                String levelImageUrl ;

                if (user.getLevel().getImage().contains("android.resource://")){
                    levelImageUrl = user.getLevel().getImage();
                }else {
                    levelImageUrl = BuildConfig.BASE_URL + user.getLevel().getImage();
                }

                Log.d(TAG, "bindData: =======" + levelImageUrl);
                Glide.with(context)
                        .load(levelImageUrl)
                        .placeholder(R.drawable.ic_level_placeholder)
                        .error(R.drawable.ic_level_placeholder)
                        .into(binding.ivLevel);
            } else {
                binding.ivLevel.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_level_placeholder));
            }

            // Handle reaction type
            if ("reaction".equals(comment.getType())) {
                binding.tvComment.setText(R.string.reacted);
                if (user != null && user.getName() != null) {
                    binding.tvName.setText(user.getName());
                } else {
                    binding.tvName.setText("");
                }
                binding.tvJoined.setVisibility(GONE);
                binding.imgReaction.setVisibility(VISIBLE);
                if (comment.getReaction() != null && !comment.getReaction().isEmpty()) {
                    Glide.with(context)
                            .load(comment.getReaction())
                            .into(binding.imgReaction);
                } else {
                    binding.imgReaction.setImageDrawable(null);
                }

                binding.layUserImage.setVisibility(VISIBLE);
                binding.layTvComment.setVisibility(VISIBLE);
            } else {
                boolean joined = comment.isJoined();
                String commentText = comment.getComment() != null ? comment.getComment() : "";

                if (joined && !commentText.isEmpty()) {

                    ViewGroup.MarginLayoutParams params =
                            (ViewGroup.MarginLayoutParams) binding.tvJoined.getLayoutParams();
                    params.topMargin = 0;
                    binding.tvJoined.setLayoutParams(params);

                    binding.tvJoined.setText(commentText);
                    binding.tvJoined.setTextColor(context.getResources().getColor(R.color.green_light));
                    binding.tvName.setVisibility(GONE);
                    binding.tvJoined.setVisibility(VISIBLE);
                    binding.ivLevel.setVisibility(GONE);
                    binding.imgReaction.setVisibility(GONE);
                    binding.layUserImage.setVisibility(GONE);
                    binding.layTvComment.setVisibility(GONE);
                } else if (joined) {

                    ViewGroup.MarginLayoutParams params =
                            (ViewGroup.MarginLayoutParams) binding.tvJoined.getLayoutParams();
                    params.topMargin = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 4,
                            context.getResources().getDisplayMetrics()
                    );


                    binding.ivLevel.setVisibility(VISIBLE);
                    binding.tvJoined.setText(R.string.enter_a_room);
                    binding.tvJoined.setTextColor(context.getResources().getColor(R.color.green_light));
                    if (user != null && user.getName() != null) {
                        binding.tvName.setText(user.getName());
                    } else {
                        binding.tvName.setText("");
                    }
                    binding.tvName.setVisibility(VISIBLE);
                    binding.tvJoined.setVisibility(VISIBLE);
                    binding.imgReaction.setVisibility(GONE);
                    binding.layTvComment.setVisibility(GONE);
                    binding.layUserImage.setVisibility(VISIBLE);
                } else {
                    binding.tvJoined.setVisibility(GONE);
                    binding.tvName.setVisibility(VISIBLE);
                    binding.imgReaction.setVisibility(GONE);
                    binding.layTvComment.setVisibility(VISIBLE);
                    binding.layUserImage.setVisibility(VISIBLE);

                    if (user != null && user.getName() != null) {
                        binding.tvName.setText(user.getName());
                    } else {
                        binding.tvName.setText("");
                    }
                    binding.tvComment.setText(commentText);
                }
            }

            if (user != null) {
                binding.imgUser.setProfileUserImage(
                        user.getImage() != null ? user.getImage() : "",
                        user.getAvatarFrameImage() != null ? user.getAvatarFrameImage() : "",
                        10);
            } else {
                Log.d(TAG, "bindData: Clear Image");
            }

            binding.getRoot().setOnClickListener(v -> {
                if (onCommentClickListener != null && user != null) {
                    onCommentClickListener.onClickComment(user);
                }
            });

        }
    }

    // Updates the first "joined" system row whose text starts with the given prefix.
    public boolean updateFirstAnnouncement(String prefix, String newText) {
        if (comments == null || comments.isEmpty()) return false;
        for (int i = 0; i < comments.size(); i++) {
            LiveStramComment c = comments.get(i);
            if (c != null && c.isJoined()) {
                String t = c.getComment();
                if (t != null && t.startsWith(prefix)) {
                    c.setComment(newText);
                    notifyItemChanged(i);
                    return true;
                }
            }
        }
        return false;
    }

}

