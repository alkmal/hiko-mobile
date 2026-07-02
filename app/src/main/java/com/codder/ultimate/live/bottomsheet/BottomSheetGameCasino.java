package com.codder.ultimate.live.bottomsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.codder.ultimate.databinding.BottomSheetGameCasinoBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetGameCasino {
    private static final String TAG = "BottomSheetGameCasino";
    private final SessionManager sessionManager;
    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetGameCasinoBinding binding;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    public BottomSheetGameCasino(@NonNull Context context, @NonNull String gameUrl, @NonNull OnDialogDismissListener onDialogDismissListener) {
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

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_game_casino, null, false);
        bottomSheetDialog.setContentView(binding.getRoot());

        binding.loader.setVisibility(View.VISIBLE);

        webView = binding.webViewGame;
        if (webView != null) {
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setWebViewClient(new SafeWebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);

            String userId = (sessionManager.getUser() != null && sessionManager.getUser().getId() != null)
                    ? sessionManager.getUser().getId() : "";
            webView.loadUrl(gameUrl + "?id=" + userId);
            Log.d(TAG, "BottomSheetGameCasino: ========" + gameUrl + "?id=" + userId);

            WebView.setWebContentsDebuggingEnabled(true);
        }

        binding.closeBtn.setOnClickListener(v -> dismiss(onDialogDismissListener));

        bottomSheetDialog.setOnCancelListener(dialog -> dismiss(onDialogDismissListener));

        bottomSheetDialog.setOnDismissListener(dialog -> dismiss(onDialogDismissListener));

        if (!bottomSheetDialog.isShowing()) {
            bottomSheetDialog.show();
        }
    }

    private void dismiss(OnDialogDismissListener onDialogDismissListener) {
        if (webView != null) {
            // Clear and destroy the WebView to prevent leaks
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
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
            if (binding != null) {
                binding.loader.setVisibility(View.VISIBLE);
            }
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
            if (binding != null) {
                binding.loader.setVisibility(View.GONE);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.e(TAG, "WebView Error: " + (error != null ? error.getErrorCode() : "unknown"));
            if (binding != null) {
                binding.loader.setVisibility(View.GONE);
            }
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            Log.e(TAG, "WebView RenderProcessGone: " + (detail != null ? detail.toString() : "unknown"));
            if (view != null) {
                view.destroy();
            }
            return true;
        }
    }

    public interface OnDialogDismissListener {
        void onDismiss();
    }
}
