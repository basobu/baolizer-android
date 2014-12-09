package org.baobab.baolizer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class DetailsFragment extends Fragment {

    public static final String WEBVIEW = "webview/";
    private static final String PAGE_HTML = ".page.html";
    private ImageButton feedback;

    public static DetailsFragment newInstance(String baobabId) {
        Bundle b = new Bundle();
        b.putString("id", baobabId);
        DetailsFragment f = new DetailsFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View frame = inf.inflate(R.layout.fragment_details, parent, false);
        feedback = (ImageButton) frame.findViewById(R.id.feedback);
        String id = getArguments().getString("id");
        ((WebFragment) getChildFragmentManager()
                .findFragmentById(R.id.products))
                .load(RefreshService.BASE_URL +
                        WEBVIEW + id + PAGE_HTML);
        return frame;
    }
}