package be.vbsteven.bmtodesk;

import android.app.AlertDialog;
import android.content.Context;

/**
 * utility methods used throughout the application
 * 
 * @author steven
 */
public class Utils {
	public static void showMessage(Context context, String title, String message) {
		new AlertDialog.Builder(context).setTitle(title).setMessage(message)
				.setPositiveButton("OK", null).create().show();
	}
}
