package org.baobab.baolizer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class WebFragment extends Fragment {

    private WebView webView;
    private Bundle webViewBundle;


    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {


        int position = getArguments().getInt("position");

        String url = getArguments().getString("url");

        View v = inf.inflate(R.layout.fragment_web, parent, false);


        webView = (WebView)v.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        //webView.setWebViewClient(new WebViewClient());


        if (savedInstanceState == null) {
            System.out.println("load");
            webView.loadUrl(url);
        } else {
            System.out.println("restore");
            webView.restoreState((Bundle) savedInstanceState.getParcelable("state"));
        }


        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("save");
        webViewBundle = new Bundle();
        webView.saveState(webViewBundle);
        outState.putParcelable("state", webViewBundle);
    }

    @Override
    public void onPause() {
        super.onPause();

    }


}