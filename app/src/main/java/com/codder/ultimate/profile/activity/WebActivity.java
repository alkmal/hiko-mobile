package com.codder.ultimate.profile.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityWebBinding;
import com.codder.ultimate.retrofit.Const;

public class WebActivity extends BaseActivity {

    ActivityWebBinding binding;
    private String website = "";
    private String title = "";
    private boolean loadingFinished = true;
    private boolean redirect = false;
    private boolean isToolbar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web);

        handleIntent(getIntent());
    }

    public static void open(Context context, String title, String url, boolean isToolbar) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(Const.TITLE, title);
        intent.putExtra(Const.URL, url);
        intent.putExtra("isToolbar", isToolbar);
        context.startActivity(intent);
    }

    private void handleIntent(Intent intent) {
        binding.pd.setVisibility(View.VISIBLE);

        if (intent == null) return;

        website = intent.getStringExtra(Const.URL);
        title = intent.getStringExtra(Const.TITLE);
        isToolbar = intent.getBooleanExtra("isToolbar", false);

        if (title != null) binding.tvTitle.setText(title);
        binding.topLayout.setVisibility(isToolbar ? View.VISIBLE : View.GONE);

        if (website != null && !website.trim().isEmpty()) {
            loadUrl(website);
            Log.d(TAG, "handleIntent: =====" + website);
        } else {
            Toast.makeText(this, getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUrl(String url) {
        WebSettings settings = binding.webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        binding.webview.addJavascriptInterface(new WebAppInterface(this), "Android");
        binding.webview.setWebViewClient(new SafeWebViewClient());
        binding.webview.loadUrl(url);
    }

    private class SafeWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
            if (!loadingFinished) redirect = true;
            loadingFinished = false;
            view.loadUrl(urlNewString);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap facIcon) {
            loadingFinished = false;
            binding.pd.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!redirect) loadingFinished = true;
            binding.pd.setVisibility(View.GONE);
            redirect = false;
        }
    }

    public class WebAppInterface {
        private final Context mContext;

        public WebAppInterface(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            finish();
        }

        @JavascriptInterface
        public void showAndroidToast(String toast) {
            Toast.makeText(mContext, toast + "asasa", Toast.LENGTH_SHORT).show();
        }


        @JavascriptInterface
        public void ok(String toast) {
            Toast.makeText(mContext, toast + mContext.getString(R.string.ok), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (binding.webview.canGoBack()) {
            binding.webview.goBack();
        } else {
            finish();
        }
    }
}