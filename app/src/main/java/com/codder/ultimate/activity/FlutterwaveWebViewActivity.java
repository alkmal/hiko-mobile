package com.codder.ultimate.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FlutterwaveWebViewActivity extends BaseActivity {

    public static final String EXTRA_URL = "checkout_url";
    public static final String EXTRA_REFERENCE = "reference";
    public static final int RESULT_SUCCESS = 200;
    public static final int RESULT_CANCELED = 201;

    private WebView webView;
    private String reference;
    private boolean isFinishing = false;

    public class FlwJsInterface {
        @JavascriptInterface
        public void onSuccess(String txRef) {
            Log.d("FLW_WV", "✅ onSuccess: " + txRef);
            runOnUiThread(() -> finishWithSuccess(txRef != null ? txRef : reference));
        }

        @JavascriptInterface
        public void onCancel() {
            Log.d("FLW_WV", "❌ onCancel");
            runOnUiThread(() -> finishWithCancel());
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
        webView.addJavascriptInterface(new FlwJsInterface(), "FlwAndroid");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String loadUrl = request.getUrl().toString();
                Log.d("FLW_URL", ">>> " + loadUrl);

                // ✅ Success
                if (loadUrl.contains("status=successful")
                        || loadUrl.contains("status=completed")
                        || loadUrl.contains("transaction_id=")) {
                    Uri uri = Uri.parse(loadUrl);
                    String txRef = uri.getQueryParameter("tx_ref");
                    if (txRef == null) txRef = uri.getQueryParameter("transaction_id");
                    finishWithSuccess(txRef != null ? txRef : reference);
                    return true;
                }

                // ✅ Cancel
                if (loadUrl.contains("status=cancelled")
                        || loadUrl.contains("status=canceled")
                        || loadUrl.contains("cancelled=true")) {
                    finishWithCancel();
                    return true;
                }

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("FLW_PAGE", "Page finished: " + url);
                injectCancelDetector(view);

                // ✅ Page title log
                view.evaluateJavascript("document.title", value ->
                        Log.d("FLW_TITLE", "Title: " + value));

                // ✅ Cancel button text log - exactly what's on screen
                view.evaluateJavascript(
                        "(function() {"
                                + "  var all = document.querySelectorAll('*');"
                                + "  var found = [];"
                                + "  for(var i=0; i<all.length; i++) {"
                                + "    var t = (all[i].innerText||'').trim();"
                                + "    if(t.toLowerCase().indexOf('cancel') !== -1 && t.length < 20) {"
                                + "      found.push(all[i].tagName + ':' + t);"
                                + "    }"
                                + "  }"
                                + "  return JSON.stringify(found);"
                                + "})()",
                        value -> Log.d("FLW_CANCEL_BTN", "Cancel elements: " + value)
                );
            }
        });

        webView.setWebChromeClient(new android.webkit.WebChromeClient() {

            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, android.webkit.JsResult result) {
                Log.d("FLW_JS", "confirm: " + message);

                new androidx.appcompat.app.AlertDialog.Builder(FlutterwaveWebViewActivity.this)
                        .setMessage(message)
                        .setPositiveButton("OK", (dialog, which) -> {
                            result.confirm();

                            finishWithCancel();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            result.cancel();
                        })
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url,
                                     String message, android.webkit.JsResult result) {
                Log.d("FLW_JS", "alert: " + message);
                result.confirm();
                return true;
            }

            @Override
            public boolean onConsoleMessage(android.webkit.ConsoleMessage msg) {
                Log.d("FLW_CONSOLE", msg.message());
                return true;
            }
        });

        if (url != null) {
            webView.loadUrl(url);
        } else {
            finishWithCancel();
        }
    }

    private void injectCancelDetector(WebView view) {
        String js = "javascript:(function() {"

                + "  function attachListeners() {"
                + "    var all = document.querySelectorAll('*');"
                + "    for(var i=0; i<all.length; i++) {"
                + "      var t = (all[i].innerText||all[i].textContent||'').trim();"
                + "      if(t === 'Cancel' || t === 'cancel' || t === 'CANCEL') {"
                + "        all[i].onclick = function(e) {"
                + "          e.stopPropagation();"
                + "          FlwAndroid.onCancel();"
                + "          return false;"
                + "        };"
                + "      }"
                + "    }"
                + "  }"

                + "  var obs = new MutationObserver(function() { attachListeners(); });"
                + "  if(document.body) obs.observe(document.body, {childList:true, subtree:true});"
                + "  attachListeners();"

                + "  window.close = function() { FlwAndroid.onCancel(); };"

                + "  window.addEventListener('message', function(e) {"
                + "    try {"
                + "      var d = typeof e.data === 'string' ? JSON.parse(e.data) : e.data;"
                + "      if(d && (d.type === 'close' || d.event === 'close' "
                + "           || d.type === 'cancel' || d.event === 'cancel'"
                + "           || d.status === 'cancelled' || d.status === 'canceled')) {"
                + "        FlwAndroid.onCancel();"
                + "      }"
                + "      if(d && (d.status === 'successful' || d.status === 'completed'"
                + "           || d.type === 'success')) {"
                + "        FlwAndroid.onSuccess(d.tx_ref || d.txRef || '');"
                + "      }"
                + "    } catch(err) {}"
                + "  });"
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
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}