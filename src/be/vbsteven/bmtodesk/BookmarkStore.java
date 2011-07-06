package be.vbsteven.bmtodesk;

import java.util.ArrayList;

public class BookmarkStore {

	public ArrayList<IncomingBookmark> getLatestIncomingBookmarks() {
		ArrayList<IncomingBookmark> result = new ArrayList<IncomingBookmark>();

		IncomingBookmark bm = new IncomingBookmark();
		bm.url = "http://google.com";
		bm.timestamp = 12345;
		result.add(bm);

		bm = new IncomingBookmark();
		bm.url = "http://andedit.com";
		bm.timestamp = 123123123;
		result.add(bm);

		bm = new IncomingBookmark();
		bm.url = "http://plus.google.com";
		bm.timestamp = 123123123;
		result.add(bm);

		return result;
	}
}
