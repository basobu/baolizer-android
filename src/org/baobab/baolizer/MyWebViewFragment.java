package org.baobab.baolizer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by tsc on 05.12.13.
 */
public class MyWebViewFragment extends Fragment {

    private WebView webView;
    private Bundle webViewBundle;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        int position = getArguments().getInt("position");

        String url = getArguments().getString("url");

        View v = inflater.inflate(R.layout.fragment_web, container, false);


        webView = (WebView)v.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        //webView.setWebViewClient(new WebViewClient());


        if (webViewBundle == null) {
            webView.loadUrl(url);
        } else {
            webView.restoreState(webViewBundle);
        }


        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        webViewBundle = new Bundle();
        webView.saveState(webViewBundle);
    }


}