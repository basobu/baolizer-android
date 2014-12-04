import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import org.baobab.baolizer.BaobabProvider;

import static org.baobab.baolizer.BaobabProvider.Baobab;

public class ProviderTest extends ProviderTestCase2<BaobabProvider> {

    public ProviderTest() {
        super(BaobabProvider.class, "org.baobab.baolizer.test");
    }

    private ContentValues[] dummyBaobabs() {
        ContentValues[] items = new ContentValues[3];
        ContentValues baobab = new ContentValues();
        baobab.put(Baobab.NAME, "foo");
        baobab.put(Baobab.GEOHASH, "abc1");
        baobab.put(Baobab.CATEGORIES, "[\"kiosk\", \"markt\"]");
        baobab.put(Baobab.PRODUCTS, "[{\"app_item_id\": 14, \"title\": \"Baola\"}]");
        items[0] = baobab;
        baobab = new ContentValues();
        baobab.put(Baobab.NAME, "bar");
        baobab.put(Baobab.GEOHASH, "abc2");
        baobab.put(Baobab.CATEGORIES, "[\"gastro\", \"kiosk\"]");
        baobab.put(Baobab.PRODUCTS, "[{\"app_item_id\": 14, \"title\": \"Baola\"}," +
                "{\"app_item_id\": 42, \"title\": \"Pulver\"}," +
                "{\"app_item_id\": 3, \"title\": \"BaoJam\"}]");
        items[1] = baobab;
        baobab = new ContentValues();
        baobab.put(Baobab.NAME, "baz");
        baobab.put(Baobab.GEOHASH, "abc3");
        baobab.put(Baobab.CATEGORIES, "[\"gastro\", \"laden\"]");
        items[2] = baobab;
        return items;
    }

    public void testBaobab() {
        getMockContentResolver().bulkInsert(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"),
                dummyBaobabs());
        Cursor baobabs = getMockContentResolver().query(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"),
                null, null, null, null);
        assertEquals("three baobabs", 3, baobabs.getCount());
        baobabs.moveToPosition(1);
        assertEquals("name", "baz", baobabs.getString(1));
        assertEquals("geohash", "abc3", baobabs.getString(6));
    }

    public void testCategories() {
        getMockContentResolver().bulkInsert(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"),
                dummyBaobabs());
        Cursor categories = getMockContentResolver().query(
                Uri.parse("content://org.baobab.baolizer.test/categories"),
                null, null, null, null);
        assertEquals("four categories", 4, categories.getCount());
        categories.moveToPosition(2);
        assertEquals("name", "gastro", categories.getString(1));
    }


    public void testFilterByCategories() {
        getMockContentResolver().bulkInsert(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"),
                dummyBaobabs());
        Cursor baobabs = getMockContentResolver().query(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"), null,
                "categories.title IS 'laden' OR categories.title IS 'gastro'",
                null, null);
        assertEquals("two baobabs", 2, baobabs.getCount());
        baobabs.moveToPosition(1);
        assertEquals("name", "baz", baobabs.getString(1));
        assertEquals("geohash", "abc3", baobabs.getString(6));
    }

    public void testProducts() {
        getMockContentResolver().bulkInsert(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"),
                dummyBaobabs());
        Cursor products = getMockContentResolver().query(
                Uri.parse("content://org.baobab.baolizer.test/products"),
                null, null, null, null);
        assertEquals("three products", 3, products.getCount());
        products.moveToPosition(0);
        assertEquals("title", "Baola", products.getString(1));
    }

    public void testFilterByProducts() {
        getMockContentResolver().bulkInsert(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"),
                dummyBaobabs());
        Cursor baobabs = getMockContentResolver().query(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"), null,
                "products.title IS 'Baola' OR products.title IS 'BaoJam'",
                null, null);
        assertEquals("two baobabs", 2, baobabs.getCount());
        baobabs.moveToPosition(0);
        assertEquals("name", "bar", baobabs.getString(1));
    }

    public void testProductForBaobab() {
        getMockContentResolver().bulkInsert(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"),
                dummyBaobabs());
        Cursor products = getMockContentResolver().query(
                Uri.parse("content://org.baobab.baolizer.test/baobabs/2/products"),
                null, null, null, null);
        assertEquals("three products", 3, products.getCount());
        products.moveToPosition(0);
        assertEquals("title", "BaoJam", products.getString(1));
    }

}