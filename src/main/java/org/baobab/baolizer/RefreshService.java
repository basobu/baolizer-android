package org.baobab.baolizer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.hsr.geohash.GeoHash;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import static org.baobab.baolizer.BaobabProvider.Baobab;


public class RefreshService extends IntentService {

    public static final String BASE_URL =
            "http://baolizer.baobab.org/public/";
    public static final String URL = BASE_URL + "items.json";
    private static final String TYP = "typ";
    private static final String KOMMA = ",";
    private static final String LATLON = "latlon";
    private static final String STREET = "street-address";
    private static final String CITY = "city";
    private static final String ZIP = "zip-codepost-code";
    private static final String NAME = "company-or-organisation";
    private static final String TAG = "Service";
    private static final long REFRESH_INTERVALL = 7*24*3600*1000;
    private static final String LAST_REFRESH = "last_refresh";
    private static final String SUCCESS = "..baobabs refreshed";

    public RefreshService() {
        super(TAG);
        Log.d(TAG, "constructor");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onHandleIntent(Intent intent) {
        if (System.currentTimeMillis() - PreferenceManager
                .getDefaultSharedPreferences(this)
                .getLong(LAST_REFRESH, 0) < REFRESH_INTERVALL) {
            Log.d(TAG, "baobabs still fresh");
            return;
        }
        try {
            Log.d(TAG, "refreshing baobabs..");
            JSONObject json = get(URL);
            if (json == null) return;
            json = json.getJSONObject("items");
            ArrayList<ContentValues> items = new ArrayList<ContentValues>(200);
            Iterator<String> iterator = json.keys();
            JSONObject item;
            String podio_id;
            String val;
            while (iterator.hasNext()) {
                podio_id = iterator.next();
                item = json.getJSONObject(podio_id);
                ContentValues values = new ContentValues();
                val = get(item, LATLON);
                if (val != null) {
                    String[] latlon = val.split(KOMMA);
                    if (latlon.length == 2) {
                        values.put(BaobabProvider.Baobab.GEOHASH, GeoHash.withBitPrecision(
                                Double.parseDouble(latlon[0]),
                                Double.parseDouble(latlon[1]),
                                55).toBase32());
                    } else Log.d(TAG, "strange latlon: " + val);
                } else continue;
                values.put(BaobabProvider.Baobab.PODIO_ID, podio_id);
                val = get(item, NAME);
                if (val != null) values.put(Baobab.NAME, val);
                val = get(item, "status");
                if (val != null) values.put(Baobab.STATE, val);
                val = get(item, ZIP);
                if (val != null) values.put(Baobab.ZIP, val);
                val = get(item, CITY);
                if (val != null) values.put(Baobab.CITY, val);
                val = get(item, STREET);
                if (val != null) values.put(Baobab.STREET, val);
                val = get(item, TYP);
                if (val != null) values.put(Baobab.CATEGORIES, val);
                items.add(values);
                Log.d(TAG, " + " + values);
            }
            Log.d(TAG, "done " + items.size());
            getContentResolver().bulkInsert(Uri.parse(
                    "content://org.baobab.baolizer"),
                    items.toArray(new ContentValues[items.size()]));
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putLong(LAST_REFRESH, System.currentTimeMillis()).commit();
            Toast.makeText(this, SUCCESS, Toast.LENGTH_SHORT).show();
            Log.d(TAG, SUCCESS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String get(JSONObject item, String key) throws JSONException {
        JSONArray annoyingExtraArray = item.getJSONArray(key);
        if (annoyingExtraArray.length() > 0)
            return annoyingExtraArray
                    .getString(0);
        else return null;
    }




    public static JSONObject get(String url) {
        try {
            return loadJson((HttpURLConnection) new URL(url).openConnection());
        } catch (MalformedURLException e) {
            Log.e(TAG, "url kaputt");
        } catch (IOException e) {
            Log.e(TAG, "I/O exception!");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Error getting json");
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject loadJson(HttpURLConnection conn) throws Exception {
        return new JSONObject(loadString(conn));
    }

    public static String loadString(HttpURLConnection conn) throws Exception {
        StringBuilder result = new StringBuilder();
        InputStreamReader in = new InputStreamReader(
                new BufferedInputStream(conn.getInputStream()));
        int read;
        char[] buff = new char[1024];
        while ((read = in.read(buff)) != -1) {
            result.append(buff, 0, read);
        }
        conn.disconnect();
        return result.toString();
    }
}
