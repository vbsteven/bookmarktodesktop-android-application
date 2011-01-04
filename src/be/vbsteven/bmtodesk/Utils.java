package be.vbsteven.bmtodesk;

import android.app.AlertDialog;
import android.content.Context;

public class Utils {
	public static void showMessage(Context context, String title, String message) {
		new AlertDialog.Builder(context).setTitle(title).setMessage(message)
				.setPositiveButton("OK", null).create().show();
	}
}
