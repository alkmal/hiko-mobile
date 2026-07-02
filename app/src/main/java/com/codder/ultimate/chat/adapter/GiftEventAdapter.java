package com.codder.ultimate.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.chat.modelclass.GiftEvent;
import com.codder.ultimate.databinding.ItemGiftEventBinding;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.net.MalformedURLException;
import java.net.URL;

public class GiftEventAdapter extends ListAdapter<GiftEvent, GiftEventAdapter.VH> {

    public GiftEventAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<GiftEvent> DIFF =
            new DiffUtil.ItemCallback<GiftEvent>() {
                @Override
                public boolean areItemsTheSame(@NonNull GiftEvent o, @NonNull GiftEvent n) {
                    return o.senderId.equals(n.senderId);  // use unique id, NOT timestamp
                }

                @Override
                public boolean areContentsTheSame(@NonNull GiftEvent o, @NonNull GiftEvent n) {
                    return o.equals(n);
                }
            };


    static class VH extends RecyclerView.ViewHolder {
        final ItemGiftEventBinding b;

        VH(@NonNull ItemGiftEventBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemGiftEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        GiftEvent e = getItem(pos);

        // User area
        h.b.imgAvatar.setUserImage(e.senderAvatar, "", 20);
        h.b.tvUser.setText(e.senderName);
        h.b.tvAction.setText(h.b.getRoot().getContext().getString(R.string.sent_a_gift_));

        // Count badge
        Glide.with(h.b.getRoot())
                .load(RayziUtils.getImageFromNumber(e.count))
                .into(h.b.imgGiftCount);

        // Gift thumb
        String abs = ensureAbs(e.giftUrl);
        if (isSvga(abs)) {
            loadSvgaThumb(h.b.imgGiftThumb, abs);
        } else {
            Glide.with(h.b.getRoot())
                    .load(abs)
                    .placeholder(R.drawable.gift_placeholder)
                    .error(R.drawable.gift_placeholder)
                    .centerCrop()
                    .into(h.b.imgGiftThumb);
        }
    }

    private static boolean isSvga(String url) {
        return url != null && url.toLowerCase().endsWith(".svga");
    }

    private static String ensureAbs(String url) {
        if (url == null) return null;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return BuildConfig.BASE_URL + url;
    }

    // ---- SVGA first-frame cache & renderer ----
    private static final int MAX_CACHE = 12 * 1024 * 1024;
    private static final LruCache<String, Bitmap> SVGA_CACHE = new LruCache<String, Bitmap>(MAX_CACHE) {
        @Override
        protected int sizeOf(@NonNull String k, @NonNull Bitmap v) {
            return v.getByteCount();
        }
    };

    private static void loadSvgaThumb(@NonNull android.widget.ImageView target, @NonNull String url) {
        Context ctx = target.getContext();

        // Wait for size so we render sharp
        if (target.getWidth() == 0 || target.getHeight() == 0) {
            target.post(() -> loadSvgaThumb(target, url));
            return;
        }

        target.setTag(url);
        target.setImageResource(R.drawable.gift_placeholder);

        Bitmap cached = SVGA_CACHE.get(url);
        if (cached != null) {
            if (url.equals(target.getTag())) target.setImageBitmap(cached);
            return;
        }

        final int w = Math.max(target.getWidth(), 1);
        final int h0 = Math.max(target.getHeight(), 1);

        SVGAParser parser = new SVGAParser(ctx);
        try {
            parser.parse(new URL(url), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity video) {
                    float vw = (float) video.getVideoSize().getWidth();
                    float vh = (float) video.getVideoSize().getHeight();
                    int h = h0;
                    if (vw > 0 && vh > 0) {
                        float scale = w / vw;
                        h = Math.max(1, Math.round(vh * scale));
                    }

                    SVGAImageView off = new SVGAImageView(ctx);
                    off.setVideoItem(video);
                    off.setLoops(1);
                    int ws = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY);
                    int hs = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY);
                    off.measure(ws, hs);
                    off.layout(0, 0, w, h);
                    off.stepToFrame(0, false);

                    Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(bmp);
                    off.draw(c);

                    SVGA_CACHE.put(url, bmp);
                    if (url.equals(target.getTag())) target.setImageBitmap(bmp);
                }

                @Override
                public void onError() {
                }
            });
        } catch (MalformedURLException ignored) {
        }
    }
}