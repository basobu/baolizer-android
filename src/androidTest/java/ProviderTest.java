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
        items[0] = baobab;
        baobab = new ContentValues();
        baobab.put(Baobab.NAME, "bar");
        baobab.put(Baobab.GEOHASH, "abc2");
        baobab.put(Baobab.CATEGORIES, "[\"gastro\", \"kiosk\"]");
        items[1] = baobab;
        baobab = new ContentValues();
        baobab.put(Baobab.NAME, "baz");
        baobab.put(Baobab.GEOHASH, "abc3");
        baobab.put(Baobab.CATEGORIES, "[\"gastro\", \"laden\"]");
        items[2] = baobab;
        return items;
    }

    public void testInsert() {

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

        Cursor baobabs = getMockContentResolver().query(
                Uri.parse("content://org.baobab.baolizer.test/baobabs"), null,
                "categories.name IS 'laden' OR categories.name IS 'gastro'",
                null, null);
        assertEquals("two baobabs", 2, baobabs.getCount());
        baobabs.moveToPosition(1);
        assertEquals("name", "baz", baobabs.getString(1));
        assertEquals("geohash", "abc3", baobabs.getString(6));
    }

}