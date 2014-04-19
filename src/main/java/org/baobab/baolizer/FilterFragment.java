package org.baobab.baolizer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class FilterFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private ListView list;
    private CursorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View frame = inf.inflate(R.layout.fragment_filter, parent, false);
        adapter = new CursorAdapter(getActivity(), null, true) {

            @Override
            public View newView(Context ctx, Cursor c, ViewGroup parent) {
                return View.inflate(ctx, R.layout.view_category_list_item, null);
            }

            @Override
            public void bindView(View view, Context ctx, Cursor cursor) {
                ((TextView) view).setText(cursor.getString(1));
            }
        };
        list = (ListView) frame.findViewById(R.id.categories);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        getActivity().getSupportLoaderManager().initLoader(1, null, this);
        return frame;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Uri.parse("content://org.baobab.baolizer/categories"),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) return;
        adapter.swapCursor(data);
        for (int i = 0; i < data.getCount(); i++) {
            list.setItemChecked(i, true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getActivity().getSupportLoaderManager().restartLoader(0, null,
                (LoaderManager.LoaderCallbacks<Cursor>) getActivity());
    }

    public String getWhereClause() {
        StringBuffer where = new StringBuffer();
        where.append("geohash = 'nixselected'");
        Cursor categories = adapter.getCursor();
        if (categories == null) return null;
        SparseBooleanArray selection = list.getCheckedItemPositions();
        for (int i = 0; i < selection.size(); i++) {
            if (selection.get(i)) {
                categories.moveToPosition(i);
                where.append(" OR category_baobab.category_id = ")
                        .append(categories.getString(0));
            }
        }
        return where.toString();
    }
}