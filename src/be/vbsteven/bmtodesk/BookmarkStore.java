package be.vbsteven.bmtodesk;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class BookmarkStore {

	private static final String DATABASE_NAME = "bookmarkstore.db";
	private static final int DATABASE_VERSION = 1;

	private static BookmarkStore instance;


	private final Context context;
	private final DatabaseHelper dbHelper;

	public static BookmarkStore get(Context context) {
		if (instance == null) {
			instance = new BookmarkStore(context);
		}

		return instance;
	}

	private BookmarkStore(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(context);
	}

	public ArrayList<IncomingBookmark> getLatestIncomingBookmarks() {

		ArrayList<IncomingBookmark> result = new ArrayList<IncomingBookmark>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String[] columns = new String[] { "_id", "url", "timestamp" };
		Cursor c = db.query("incomingbookmarks", columns, null, null, null, null, "timestamp DESC");

		int columnUrl = c.getColumnIndex("url");
		int columnTimestamp = c.getColumnIndex("timestamp");
		IncomingBookmark bm;

		for (boolean check = c.moveToFirst(); check; check = c.moveToNext()) {
			bm = new IncomingBookmark();
			bm.url = c.getString(columnUrl);
			bm.timestamp = c.getLong(columnTimestamp);
			result.add(bm);
		}

		db.close();

		return result;
	}

	public void addIncomingBookmark(IncomingBookmark bm) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteStatement statement = db.compileStatement("INSERT INTO incomingbookmarks VALUES (?, ?, ?)");
	    statement.bindNull(1); // id
	    statement.bindString(2, bm.url);
	    statement.bindLong(3, bm.timestamp);
	    statement.execute();
	    statement.close();
	    db.close();
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL("CREATE TABLE incomingbookmarks (_id INTEGER PRIMARY KEY,url TEXT NOT NULL, timestamp INTEGER); ");
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS incomingbookmarks");
			onCreate(db);
		}

	}
}
