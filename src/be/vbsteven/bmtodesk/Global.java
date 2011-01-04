package be.vbsteven.bmtodesk;

import android.content.Context;
import android.content.SharedPreferences;

public class Global {
	public static final String TAG = "bookmarktodesktop";
	public static final String USERNAME = "USERNAME";
	public static final String PASSWORD = "PASSWORD";
	public static final String FASTSHARING = "FASTSHARING";
	public static final String HIDECONFIRMATION = "HIDECONFIRMATION";
	public static final String PREFS = "be.vbsteven.bmtodesk_preferences";
	public static final String EXTRA_TITLE = "bmtodesk.TITLE";
	public static final String EXTRA_URL = "bmtodesk.URL";
	private static final String SEENYOUTUBEVIDEO = "SEENYOUTUBEVIDEO";
	public static final String EXTRA_COUNT = "COUNT";
	
	public static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(PREFS, Context.MODE_WORLD_WRITEABLE);
	}
	
	public static String getUsername(Context context) {
		return getPrefs(context).getString(USERNAME, "");
	}
	
	public static String getPassword(Context context) {
		return getPrefs(context).getString(PASSWORD, "");
	}
	
	public static void saveUsernameAndPassword(Context context, String username, String password) {
		getPrefs(context).edit().putString(USERNAME, username).putString(PASSWORD, password).commit();
	}
	
	public static boolean hasUserCredentials(Context context) {
		String username = getUsername(context);
		String password = getPassword(context);
		
		return (!username.equals("") && !password.equals(""));
	}
	
	public static boolean isFastSharing(Context context) {
		return getPrefs(context).getBoolean(FASTSHARING, false);
	}
	
	public static boolean isHideConfirmation(Context context) {
		return getPrefs(context).getBoolean(HIDECONFIRMATION, false);
	}
	
	public static boolean hasSeenYoutubeVideo(Context context) {
		return getPrefs(context).getBoolean(SEENYOUTUBEVIDEO, false);
	}
	
	public static void setSeenYoutubeVideo(Context context) {
		getPrefs(context).edit().putBoolean(SEENYOUTUBEVIDEO, true).commit();
	}
	
	public static boolean isBackgroundSharing(Context context) {
		return getPrefs(context).getBoolean("BACKGROUNDSHARING", false);
	}
}
