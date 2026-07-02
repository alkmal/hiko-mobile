package com.codder.ultimate.live.bottomsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.BottomSheetGameTeenPattiBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetGameTeenPatti {
    private static final String TAG = "BottomSheetGameTeenPatti";
    private BottomSheetGameTeenPattiBinding binding;
    private SessionManager sessionManager;
    private BottomSheetDialog bottomSheetDialog;
    private WebView webView;
    private boolean dismissed = false;

    @SuppressLint("SetJavaScriptEnabled")
    public BottomSheetGameTeenPatti(
            @NonNull Context context,
            @NonNull String gameUrl,
            @NonNull OnDialogDismissListener onDialogDismissListener
    ) {
        sessionManager = new SessionManager(context);
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);

        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_game_teen_patti, null, false);
        bottomSheetDialog.setContentView(binding.getRoot());

        // WebView setup
        webView = binding.webViewGame;
        if (webView != null) {
            webView.setWebViewClient(new SafeWebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            WebView.setWebContentsDebuggingEnabled(true);

            String userId = (sessionManager.getUser() != null && sessionManager.getUser().getId() != null)
                    ? sessionManager.getUser().getId()
                    : "";
            webView.loadUrl(gameUrl + "?id=" + userId);
        }

        binding.loader.setVisibility(View.VISIBLE);

        binding.closeBtn.setOnClickListener(v -> dismiss(onDialogDismissListener));
        bottomSheetDialog.setOnCancelListener(dialog -> dismiss(onDialogDismissListener));
        bottomSheetDialog.setOnDismissListener(dialog -> dismiss(onDialogDismissListener));

        // Defensive: show only if not already showing
        if (!bottomSheetDialog.isShowing()) {
            bottomSheetDialog.show();
        }
    }

    private void dismiss(OnDialogDismissListener onDialogDismissListener) {
        if (dismissed) return; // Only dismiss once
        dismissed = true;

        if (webView != null) {
            try {
                webView.loadUrl("about:blank");
                webView.clearHistory();
                webView.removeAllViews();
                webView.destroy();
            } catch (Throwable t) {
                Log.w(TAG, "WebView destroy error", t);
            }
            webView = null;
        }
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
        if (onDialogDismissListener != null) {
            onDialogDismissListener.onDismiss();
        }
    }

    private class SafeWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (binding != null) binding.loader.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (view != null && url != null) {
                view.loadUrl(url);
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (binding != null) binding.loader.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.e(TAG, "WebView Error: " + (error != null ? error.getErrorCode() : "unknown"));
            if (binding != null) binding.loader.setVisibility(View.GONE);
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            Log.e(TAG, "WebView RenderProcessGone: " + (detail != null ? detail.toString() : "unknown"));
            if (view != null) {
                try {
                    view.destroy();
                } catch (Throwable t) {
                    Log.w(TAG, "WebView destroy onRenderProcessGone error", t);
                }
            }
            return true;
        }
    }

    public interface OnDialogDismissListener {
        void onDismiss();
    }
}

