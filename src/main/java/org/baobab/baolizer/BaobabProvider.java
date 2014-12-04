package org.baobab.baolizer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaobabProvider extends ContentProvider {

    private static final String TAG = "BaobabProvider";
    private SQLiteStatement getCategory;
    private SQLiteStatement getProduct;

    static public class Baobab {
        public static final String CATEGORIES = "types";
        public static final String PRODUCTS = "produkte";
        public static final String STATE = "state";
        public static final String NAME = "name";
        public static final String ZIP = "zip";
        public static final String CITY = "city";
        public static final String STREET = "street";
        public static final String GEOHASH = "geohash";
        public static final String PODIO_ID = "podio_id";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        static final String TAG = "BaobabProvider";

        public DatabaseHelper(Context context) {
            super(context, "baobab.db", null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE baobabs (" +
                            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            Baobab.NAME + " TEXT, " +
                            Baobab.STATE + " TEXT, " +
                            Baobab.ZIP + " TEXT, " +
                            Baobab.CITY + " TEXT, " +
                            Baobab.STREET + " TEXT, " +
                            Baobab.GEOHASH + " TEXT, " +
                            Baobab.PODIO_ID + " TEXT " +
                        ");");
            db.execSQL("CREATE TABLE category_baobab (" +
                            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           " category_id, " +
                           " baobab_id" +
                       ");");
            db.execSQL("CREATE TABLE categories (" +
                           "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           " title TEXT " +
                       ");");
            db.execSQL("CREATE TABLE baobab_products (" +
                            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            " product_id, " +
                            " baobab_id" +
                        ");");
            db.execSQL("CREATE TABLE products (" +
                            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            " title TEXT, " +
                            " guid TEXT " +
                        ");");
            Log.d(TAG, "created DB");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
            db.execSQL("DROP TABLE IF EXISTS baobabs;");
            db.execSQL("DROP TABLE IF EXISTS products;");
            db.execSQL("DROP TABLE IF EXISTS categories;");
            db.execSQL("DROP TABLE IF EXISTS baobab_products;");
            db.execSQL("DROP TABLE IF EXISTS category_baobab;");
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .remove(RefreshService.LAST_REFRESH).commit();
            onCreate(db);
        }
    }



    private DatabaseHelper db;
    private SQLiteStatement insert;
    
    @Override
    public boolean onCreate() {
        db = new DatabaseHelper(getContext());
        getCategory = db.getReadableDatabase().compileStatement(GET_CATEGORY);
        getProduct = db.getReadableDatabase().compileStatement(GET_PRODUCT);
        return false;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    static final String CLEAN_UP = "DELETE FROM baobabs";

    static final String INSERT_BAOBAB = "INSERT INTO baobabs (" +
            Baobab.NAME + ", " +
            Baobab.STATE + ", " +
            Baobab.ZIP + ", " +
            Baobab.CITY + ", " +
            Baobab.STREET + ", " +
            Baobab.GEOHASH + ", " +
            Baobab.PODIO_ID + ") " +
            " VALUES (?, ?, ?, ?, ?, ?, ?);";

    private void prepareInsertBaobab() {
        if (insert == null)
            insert = db.getWritableDatabase().compileStatement(INSERT_BAOBAB);
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        db.getWritableDatabase().beginTransaction();
        db.getWritableDatabase().execSQL(CLEAN_UP);
        prepareInsertBaobab();
        try {
            for (int i = 0; i < values.length; i++) {
                ContentValues baobab = values[i];
                insert.clearBindings();
                if (baobab.containsKey(Baobab.GEOHASH))
                    insert.bindString(6, baobab.getAsString(Baobab.GEOHASH));
                else continue;
                if (baobab.containsKey(Baobab.PODIO_ID))
                    insert.bindString(7, baobab.getAsString(Baobab.PODIO_ID));
                if (baobab.containsKey(Baobab.NAME))
                    insert.bindString(1, baobab.getAsString(Baobab.NAME));
                if (baobab.containsKey(Baobab.STATE))
                    insert.bindString(2, baobab.getAsString(Baobab.STATE));
                if (baobab.containsKey(Baobab.ZIP))
                    insert.bindString(3, baobab.getAsString(Baobab.ZIP));
                if (baobab.containsKey(Baobab.CITY))
                    insert.bindString(4, baobab.getAsString(Baobab.CITY));
                if (baobab.containsKey(Baobab.STREET))
                    insert.bindString(5, baobab.getAsString(Baobab.STREET));
                long id = insert.executeInsert();
                if (baobab.containsKey(Baobab.CATEGORIES)) {
                    Log.d(TAG, "json: " + baobab.getAsString(Baobab.CATEGORIES));
                    JSONArray categories = new JSONArray(
                            baobab.getAsString(Baobab.CATEGORIES));
                    for (int j = 0; j < categories.length(); j++) {
                        insertCategory(id, categories.getString(j));
                    }
                }
                if (baobab.containsKey(Baobab.PRODUCTS)) {
                    Log.d(TAG, "json: " + baobab.getAsString(Baobab.CATEGORIES));
                    JSONArray products = new JSONArray(
                            baobab.getAsString(Baobab.PRODUCTS));
                    for (int j = 0; j < products.length(); j++) {
                        JSONObject product = products.getJSONObject(j);
                        insertProduct(id, product.getString("title"),
                                product.getString("app_item_id"));
                    }
                }
            }
            db.getWritableDatabase().setTransactionSuccessful();
        } catch (JSONException e) {
            Log.e(TAG, "bad json! ");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "error during bulk insert: " + e.getClass().getName());
            e.printStackTrace();
        } finally {
            db.getWritableDatabase().endTransaction();
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(Uri.parse(
                    "content://org.baobab.baolizer/categories"), null);
        }
        return values.length;
    }

    static final String GET_CATEGORY = "SELECT _id " +
            " FROM categories WHERE title = ?;";

    private void insertCategory(long baobabId, String categoryTitle) {
        ContentValues values = new ContentValues();
        values.put("baobab_id", baobabId);
        values.put("category_id", getCategoryId(categoryTitle));
        db.getWritableDatabase().insert("category_baobab", null, values);
    }

    private long getCategoryId(String title) {
        try {
            getCategory.bindString(1, title);
            return Long.parseLong(getCategory.simpleQueryForString());
        } catch (SQLiteDoneException e) {
            ContentValues category = new ContentValues();
            category.put("title", title);
            return db.getWritableDatabase().insert("categories", null, category);
        }
    }

    static final String GET_PRODUCT = "SELECT _id " +
            " FROM products WHERE title = ?;";

    private void insertProduct(long baobabId, String title, String guid) {
        ContentValues values = new ContentValues();
        values.put("baobab_id", baobabId);
        values.put("product_id", getProductId(title, guid));
        db.getWritableDatabase().insert("baobab_products", null, values);
    }

    private long getProductId(String title, String guid) {
        try {
            getProduct.bindString(1, title);
            return Long.parseLong(getProduct.simpleQueryForString());
        } catch (SQLiteDoneException e) {
            ContentValues category = new ContentValues();
            category.put("title", title);
            category.put("guid", guid);
            return db.getWritableDatabase().insert("products", null, category);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db.getWritableDatabase().insert("baobabs", null, values);
        return null;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Cursor results;
        if (uri.getPath().equals("/categories")) {
            results = db.getReadableDatabase().query("categories",
                    null, null, null, null, null, null);
        } else if (uri.getPath().equals("/products")) {
            results = db.getReadableDatabase().query("products",
                    null, null, null, null, null, null);
        } else if (uri.getPathSegments().size() > 1) {
            results = db.getReadableDatabase().query(
                    "products JOIN baobab_products " +
                    "ON products._id = baobab_products.product_id", null,
                    "baobab_id = " + uri.getPathSegments().get(1), null,
                    "title", null, null);
        } else {
            results = db.getReadableDatabase().query("baobabs " +
                            " JOIN category_baobab ON category_baobab.baobab_id = baobabs._id " +
                            " JOIN categories ON category_baobab.category_id = categories._id" +
                            " LEFT OUTER JOIN baobab_products ON baobab_products.baobab_id = baobabs._id " +
                            " LEFT OUTER JOIN products ON baobab_products.product_id = products._id",
                    null, selection, null, Baobab.NAME, null, null);
        }
        results.setNotificationUri(getContext().getContentResolver(), uri);
        return results;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
