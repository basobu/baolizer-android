package org.baobab.baolizer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class DetailsFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

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
                .findFragmentById(R.id.web))
                .load(RefreshService.BASE_URL +
                        WEBVIEW + id + PAGE_HTML);
        getLoaderManager().initLoader(Integer.parseInt(id), null, this);
        return frame;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(new CursorAdapter(getActivity(), null, true) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return new TextView(getActivity());
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                ((TextView) view).setText(cursor.getString(1));
                ((TextView) view).setTextSize(23);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                Uri.parse("content://org.baobab.baolizer/baobabs/" +
                getArguments().getString("id") + "/products"),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d("Baolizer", "loaded " + cursor.getCount());
        ((CursorAdapter) getListAdapter()).swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }
}