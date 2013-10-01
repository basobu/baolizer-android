/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package org.baobab.baolizer;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


@SuppressLint("SetJavaScriptEnabled")
public class WebActivity extends Activity {

    private static final String TAG = "Fahrgemeinschaft";
    private ProgressDialog progress;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        webView = new WebView(this);
        progress = new ProgressDialog(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                    JsResult result) {
                Crouton.makeText(WebActivity.this, message, Style.ALERT).show();
                result.cancel();
                return true; //super.onJsAlert(view, url, message, result);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageStarted(WebView v, String url, Bitmap favic) {
                Log.d(TAG, "url");
                if (url.startsWith("http")) {
                    Log.d(TAG, "url http");
                    progress.show();
                }
                super.onPageStarted(v, url, favic);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "finished");
                progress.dismiss();
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl(getIntent().getDataString());
        webView.requestFocus(View.FOCUS_DOWN);
        setContentView(webView);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }
}
