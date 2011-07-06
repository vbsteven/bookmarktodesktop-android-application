package be.vbsteven.bmtodesk;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class IncomingBookmarkAdapter extends BaseAdapter {

	private ArrayList<IncomingBookmark> bookmarks = new ArrayList<IncomingBookmark>();
	private final Context context;
	private final LayoutInflater inflater;

	public IncomingBookmarkAdapter(Context context, ArrayList<IncomingBookmark> bookmarks) {
		this.bookmarks = bookmarks;
		this.context = context;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return bookmarks.size();
	}

	@Override
	public Object getItem(int position) {
		return bookmarks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		if (convertView != null) {
			view = convertView;
		} else {
			view = inflater.inflate(R.layout.incomingbookmarkitem, null);
		}

		TextView tvTitle = (TextView)view.findViewById(R.id.tv_incoming_title);
		tvTitle.setText(bookmarks.get(position).url);

		return view;
	}

}
