package com.codder.ultimate.live.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemPartyBinding;
import com.codder.ultimate.databinding.ItemPkInviteHostBinding;
import com.codder.ultimate.databinding.ItemVideoGridBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class LiveListAdapter extends ListAdapter<PkAudioLiveUserRoot.UsersItem, RecyclerView.ViewHolder> {

    private static final String TAG = "LiveListAdapter";
    public static final int LIVE_LIST_MODE = 2;
    public static final int PARTY_MODE = 3;

    private Context context;
    private int viewMode;
    private final int[] colors = {
            R.drawable.party_bg1,
            R.drawable.party_bg2,
            R.drawable.party_bg3,
            R.drawable.party_bg4,
            R.drawable.party_bg5,
            R.drawable.party_bg6
    };
    private OnHostClickLister onHostClickLister;

    public LiveListAdapter(int viewMode) {
        super(DIFF_CALLBACK);
        this.viewMode = viewMode;
    }

    public void setOnHostClickLister(OnHostClickLister onHostClickLister) {
        this.onHostClickLister = onHostClickLister;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewMode == LIVE_LIST_MODE) {
            return new PartyListViewHolder(inflater.inflate(R.layout.item_party, parent, false));
        } else if (viewMode == PARTY_MODE) {
            return new PartyListViewHolder(inflater.inflate(R.layout.item_party, parent, false));
        } else {
            return new PkInviteListViewHolder(inflater.inflate(R.layout.item_pk_invite_host, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder instanceof VideoListViewHolder) {
                ((VideoListViewHolder) holder).setData(position);
            } else if (holder instanceof PkInviteListViewHolder) {
                ((PkInviteListViewHolder) holder).setData(position);
            } else if (holder instanceof PartyListViewHolder) {
                ((PartyListViewHolder) holder).setData(position);
            }
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: Error binding data at position " + position, e);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.viewMode;
    }

    public void updateViewMode(int viewMode) {
        this.viewMode = viewMode;
        notifyDataSetChanged();
    }

    public interface OnHostClickLister {
        void onHostItemClick(PkAudioLiveUserRoot.UsersItem userDummy, ItemVideoGridBinding itemVideoGridBinding, ItemPkInviteHostBinding itemPkInviteHostBinding);
    }

    public class VideoListViewHolder extends RecyclerView.ViewHolder {
        ItemVideoGridBinding binding;

        public VideoListViewHolder(View itemView) {
            super(itemView);
            binding = ItemVideoGridBinding.bind(itemView);
        }

        public void setData(int position) {
            PkAudioLiveUserRoot.UsersItem userDummy = getItem(position);
            if (userDummy == null) return;
            binding.tvName.setText(userDummy.getName());
            binding.tvCountry.setText(userDummy.getCountry() == null ? "" : userDummy.getCountry());

            String userName = userDummy.getUsername();
            String displayName = "";

            if (userName != null && !userName.isEmpty()) {
                if (!userName.matches("\\d+")) {
                    displayName = "@" + userName;
                } else {
                    displayName = context.getString(R.string.id_) + userName;
                }
            } else {
                userDummy.getUniqueId();
            }

            binding.tvUserName.setText(displayName);

            try {
                MultiTransformation<Bitmap> transformations = new MultiTransformation<>(
                        new BlurTransformation(70),
                        new CenterCrop()
                );
                Glide.with(context)
                        .load(userDummy.getImage())
                        .circleCrop()
                        .apply(MainApplication.requestOptionsLive)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(transformations)
                        .into(binding.ivDetails);
            } catch (Exception e) {
                Log.e(TAG, "Glide blur load failed", e);
            }
            String flagUrl = userDummy.getCountryFlagImage();
            if (flagUrl != null && !flagUrl.isEmpty() && context instanceof Activity) {
                AsyncTask.execute(() -> {
                    try {
                        SVG svg = SVG.getFromInputStream(new URL(flagUrl).openStream());
                        Picture picture = svg.renderToPicture();
                        ((Activity) context).runOnUiThread(() -> {
                            binding.svgWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                            binding.svgWebView.setImageDrawable(new PictureDrawable(picture));
                        });
                    } catch (SVGParseException | IOException e) {
                        Log.e(TAG, "SVG flag load failed", e);
                    }
                });
            }
            if (userDummy.isIsFake()) {
                if (userDummy.isIsPkMode()) {
                    showDoubleLayout(userDummy);
                    binding.ivPk.setVisibility(View.VISIBLE);
                } else {
                    showSingleLayout(userDummy);
                    binding.ivPk.setVisibility(View.GONE);
                }
            } else {
                if (userDummy.isIsPkMode()) {
                    showDoubleLayout(userDummy);
                    binding.ivPk.setVisibility(View.GONE);
                } else {
                    showSingleLayout(userDummy);
                    binding.ivPk.setVisibility(View.GONE);
                }
            }
            binding.ivAudioRoom.setVisibility(userDummy.isAudio() ? View.VISIBLE : View.GONE);
            binding.tvViewCount.setText(String.valueOf(userDummy.getView()));
            if (userDummy.isAudio()) {
                binding.tvName.setText(userDummy.getRoomName());
                binding.imag1.setUserImage(userDummy.getRoomImage(), userDummy.getAvatarFrameImage(), 10);
            } else {
                binding.tvName.setText(userDummy.getName());
                binding.imag1.setUserImage(userDummy.getImage(), userDummy.getAvatarFrameImage(), 10);
            }
            binding.getRoot().setOnClickListener(v -> {
                if (onHostClickLister != null) {
                    onHostClickLister.onHostItemClick(userDummy, binding, null);
                }
            });
        }

        private void showSingleLayout(PkAudioLiveUserRoot.UsersItem userDummy) {
            binding.doubleLay.setVisibility(View.GONE);
            binding.vsLay.setVisibility(View.GONE);
            binding.signleLay.setVisibility(View.VISIBLE);
            try {
                if (userDummy.isAudio()) {
                    Glide.with(context)
                            .load(userDummy.getRoomImage())
                            .apply(MainApplication.requestOptionsLive)
                            .centerCrop()
                            .into(binding.image);
                } else {
                    Glide.with(context)
                            .load(userDummy.getImage())
                            .apply(MainApplication.requestOptionsLive)
                            .centerCrop()
                            .into(binding.image);
                }
            } catch (Exception e) {
                Log.e(TAG, "showSingleLayout Glide error", e);
            }
        }

        private void showDoubleLayout(PkAudioLiveUserRoot.UsersItem userDummy) {
            binding.doubleLay.setVisibility(View.VISIBLE);
            binding.vsLay.setVisibility(View.VISIBLE);
            binding.signleLay.setVisibility(View.GONE);
            try {
                List<String> pkImageArray = userDummy.getPkImageArray();
                if (userDummy.isIsFake() && pkImageArray != null && pkImageArray.size() > 1) {
                    Glide.with(context).load(pkImageArray.get(0))
                            .apply(MainApplication.requestOptionsLive)
                            .centerCrop().into(binding.host1);
                    Glide.with(context).load(pkImageArray.get(1))
                            .apply(MainApplication.requestOptionsLive)
                            .centerCrop().into(binding.host2);
                } else if (!userDummy.isIsFake() && userDummy.getPkConfig() != null
                        && userDummy.getPkConfig().getHost1Details() != null && userDummy.getPkConfig().getHost2Details() != null) {
                    Glide.with(context).load(userDummy.getPkConfig().getHost1Details().getImage())
                            .apply(MainApplication.requestOptionsLive)
                            .centerCrop().into(binding.host1);
                    Glide.with(context).load(userDummy.getPkConfig().getHost2Details().getImage())
                            .apply(MainApplication.requestOptionsLive)
                            .centerCrop().into(binding.host2);
                }
            } catch (Exception e) {
                Log.e(TAG, "showDoubleLayout Glide error", e);
            }
        }
    }

    public class PkInviteListViewHolder extends RecyclerView.ViewHolder {
        ItemPkInviteHostBinding binding;

        public PkInviteListViewHolder(View itemView) {
            super(itemView);
            binding = ItemPkInviteHostBinding.bind(itemView);
        }

        public void setData(int position) {
            PkAudioLiveUserRoot.UsersItem userDummy = getItem(position);
            if (userDummy == null) return;
            binding.tvName.setText(userDummy.getName());
            binding.tvCountry.setText(userDummy.getCountry() == null ? "" : userDummy.getCountry());
            binding.imageUser.setUserImage(userDummy.getImage(), userDummy.getAvatarFrameImage(), 15);

            binding.getRoot().setOnClickListener(v -> {
                if (onHostClickLister != null) {
                    onHostClickLister.onHostItemClick(userDummy, null, binding);
                }
            });
        }
    }

    public class PartyListViewHolder extends RecyclerView.ViewHolder {
        ItemPartyBinding binding;

        public PartyListViewHolder(View itemView) {
            super(itemView);
            binding = ItemPartyBinding.bind(itemView);
        }

        public void setData(int position) {
            PkAudioLiveUserRoot.UsersItem userDummy = getItem(position);
            if (userDummy == null) return;
            binding.tvPartyTitle.setText((userDummy.getRoomName() == null) ? userDummy.getName() : userDummy.getRoomName());

            int colorIndex = position % colors.length;
//            binding.layMain.setCardBackgroundColor(ContextCompat.getColor(context, colors[colorIndex]));
            binding.layoutMain.setBackground(ContextCompat.getDrawable(context,colors[colorIndex]));

            if (userDummy.getPrivateCode() == 0) {
                binding.ivLock.setImageResource(R.drawable.ic_public);
                binding.tvPublic.setText(R.string.public_room);
            } else {
                binding.ivLock.setImageResource(R.drawable.ic_private);
                binding.tvPublic.setText(R.string.private_room);
            }

            String partyImage = (userDummy.getRoomImage() == null) ? userDummy.getImage() : userDummy.getRoomImage();
            Glide.with(context)
                    .load(partyImage)
                    .apply(MainApplication.requestOptionsLive)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(binding.ivPartyImage);

            binding.tvViewCount.setText(String.valueOf(userDummy.getView()));
            binding.tvPartyDescription.setText((userDummy.getRoomWelcome() == null)
                    ? context.getString(R.string.welcome_to_the_party)
                    : userDummy.getRoomWelcome());

            binding.getRoot().setOnClickListener(v -> {
                if (onHostClickLister != null) {
                    onHostClickLister.onHostItemClick(userDummy, null, null);
                }
            });
        }
    }

    public static final DiffUtil.ItemCallback<PkAudioLiveUserRoot.UsersItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull PkAudioLiveUserRoot.UsersItem oldItem, @NonNull PkAudioLiveUserRoot.UsersItem newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull PkAudioLiveUserRoot.UsersItem oldItem, @NonNull PkAudioLiveUserRoot.UsersItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

}