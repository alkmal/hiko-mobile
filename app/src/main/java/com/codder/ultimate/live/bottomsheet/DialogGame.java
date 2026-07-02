package com.codder.ultimate.live.bottomsheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.DialogGameBinding;

public class DialogGame {
    private static final String TAG = "DialogGame";
    private DialogGameBinding binding;
    private Dialog dialog;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    public DialogGame(@NonNull Context context, @NonNull String gameUrl, @NonNull OnDialogDismissListener onDialogDismissListener) {
        SessionManager sessionManager = new SessionManager(context);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_game, null, false);
        dialog.setContentView(binding.getRoot());

        // WebView setup
        webView = binding.webViewGame;
        if (webView != null) {
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setWebViewClient(new SafeWebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);

            String userId = (sessionManager.getUser() != null && sessionManager.getUser().getId() != null)
                    ? sessionManager.getUser().getId() : "";
            webView.loadUrl(gameUrl + "?id=" + userId);
            WebView.setWebContentsDebuggingEnabled(true);
        }

        binding.loader.setVisibility(View.VISIBLE);

        binding.closeBtn.setOnClickListener(v -> dismiss(onDialogDismissListener));
        dialog.setOnDismissListener(d -> dismiss(onDialogDismissListener));

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void dismiss(OnDialogDismissListener onDialogDismissListener) {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
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
            if (view != null) view.destroy();
            return true;
        }
    }

    public interface OnDialogDismissListener {
        void onDismiss();
    }
}

