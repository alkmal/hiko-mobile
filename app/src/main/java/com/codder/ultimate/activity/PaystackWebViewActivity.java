package com.codder.ultimate.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class PaystackWebViewActivity extends BaseActivity {

    public static final String EXTRA_URL = "checkout_url";
    public static final String EXTRA_REFERENCE = "reference";
    public static final int RESULT_SUCCESS = 100;
    public static final int RESULT_CANCELED = 101;

    private WebView webView;
    private String reference;
    private boolean isFinishing = false;

    public class PaystackJsInterface {
        @JavascriptInterface
        public void onCancel() {
            Log.d("PAYSTACK_WV", "✅ JS onCancel called");
            runOnUiThread(() -> finishWithCancel());
        }

        @JavascriptInterface
        public void onSuccess(String ref) {
            Log.d("PAYSTACK_WV", "✅ JS onSuccess called: " + ref);
            runOnUiThread(() -> finishWithSuccess(ref != null ? ref : reference));
        }
    }

    private void finishWithSuccess(String ref) {
        if (isFinishing) return;
        isFinishing = true;
        Intent result = new Intent();
        result.putExtra(EXTRA_REFERENCE, ref);
        setResult(RESULT_SUCCESS, result);
        finish();
    }

    private void finishWithCancel() {
        if (isFinishing) return;
        isFinishing = true;
        setResult(RESULT_CANCELED);
        finish();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        String url = getIntent().getStringExtra(EXTRA_URL);
        reference = getIntent().getStringExtra(EXTRA_REFERENCE);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(new PaystackJsInterface(), "PaystackAndroid");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                                                    WebResourceRequest request) {
                String loadUrl = request.getUrl().toString();
                // ✅ Log EVERY url - cancel press પછી logcat માં જુઓ
                Log.d("PAYSTACK_URL", ">>> " + loadUrl);

                if (loadUrl.contains("close")) {
                    finishWithSuccess(reference);
                    return true;
                }
                if (loadUrl.contains("cancel")) {
                    finishWithCancel();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("PAYSTACK_PAGE", "Loaded: " + url);

                // ✅ Inject JS - every page load પછી
                injectCancelDetector(view);
            }
        });

        if (url != null) {
            webView.loadUrl(url);
        } else {
            finishWithCancel();
        }
    }

    private void injectCancelDetector(WebView view) {
        String js = "javascript:(function() {\n"
                // ✅ Method 1: Button text detect
                + "  function attachCancelListeners() {\n"
                + "    var all = document.querySelectorAll('button, a, div, span');\n"
                + "    for(var i=0; i<all.length; i++) {\n"
                + "      var t = (all[i].innerText||'').toLowerCase();\n"
                + "      if(t.indexOf('cancel') !== -1) {\n"
                + "        Log.d('found cancel btn: ' + all[i].tagName);\n"
                + "        all[i].onclick = function(e) {\n"
                + "          PaystackAndroid.onCancel();\n"
                + "        };\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                // ✅ Method 2: MutationObserver - DOM change detect
                + "  var observer = new MutationObserver(function() {\n"
                + "    attachCancelListeners();\n"
                + "  });\n"
                + "  observer.observe(document.body, {childList:true, subtree:true});\n"
                + "  attachCancelListeners();\n"
                // ✅ Method 3: window.close intercept
                + "  window.close = function() {\n"
                + "    PaystackAndroid.onCancel();\n"
                + "  };\n"
                // ✅ Method 4: history.back intercept
                + "  var origBack = window.history.back;\n"
                + "  window.history.back = function() {\n"
                + "    PaystackAndroid.onCancel();\n"
                + "  };\n"
                + "})()";

        view.loadUrl(js);
    }

    @Override
    public void onBackPressed() {
        finishWithCancel();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
