package org.baobab.baolizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class WebFragment extends Fragment {

    private final String url;
    private WebView webView;
    private ImageView progress;

    public WebFragment() {
        this.url = "http://baobab.org";
    }

    public static WebFragment newInstance(String url) {
        Bundle b = new Bundle();
        b.putString("url", url);
        WebFragment f = new WebFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View frame = inf.inflate(R.layout.fragment_web, parent, false);
        progress = (ImageView) frame.findViewById(R.id.progress);
        webView = (WebView)frame.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        webView.requestFocus(View.FOCUS_DOWN);
        if (getArguments() != null) {
            load(getArguments().getString("url"));
        }
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Crouton.makeText(getActivity(), message, Style.ALERT).show();
                result.cancel();
                return true; //super.onJsAlert(view, get, message, result);
            }
        });
        frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return frame;
    }

    public void load(String url) {
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView v, String url, Bitmap favic) {
                super.onPageStarted(v, url, favic);
                if (url.startsWith("http")) {
                    progress.setVisibility(View.VISIBLE);
                    RotateAnimation rotate = new RotateAnimation(
                            0f, 360f, Animation.RELATIVE_TO_SELF,
                            0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotate.setDuration(600);
                    rotate.setRepeatMode(Animation.RESTART);
                    rotate.setRepeatCount(Animation.INFINITE);
                    progress.startAnimation(rotate);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progress.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                    return true;
                } else if (url.equals("http://map.baobab.org/")) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }
}