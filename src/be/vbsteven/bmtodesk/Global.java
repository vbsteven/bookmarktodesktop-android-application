package be.vbsteven.bmtodesk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * global constants and a few utility methods used throughout the application
 * 
 * @author steven
 */
public class Global {
	public static final String TAG = "bookmarktodesktop"; // for logging
	
	public static final String PREFS = "be.vbsteven.bmtodesk_preferences";

	// constants for preferences
	public static final String USERNAME = "USERNAME";
	public static final String PASSWORD = "PASSWORD";
	public static final String FASTSHARING = "FASTSHARING";
	public static final String HIDECONFIRMATION = "HIDECONFIRMATION";
	private static final String SEENYOUTUBEVIDEO = "SEENYOUTUBEVIDEO";

	// constants used for adding extras to intents (prefixed to avoid conflicts)
	public static final String EXTRA_TITLE = "bmtodesk.TITLE";
	public static final String EXTRA_URL = "bmtodesk.URL";
	public static final String EXTRA_COUNT = "bmtodesk.COUNT";

	/**
	 * utility method for retrieving the shared preferences for this app
	 * 
	 * @param context
	 * @return the shared preferences object for this app
	 */
	public static SharedPreferences getPrefs(Context context) {
		return context
				.getSharedPreferences(PREFS, Context.MODE_WORLD_WRITEABLE);
	}

	/**
	 * @param context
	 * @return username of the user or "" if not found
	 */
	public static String getUsername(Context context) {
		return getPrefs(context).getString(USERNAME, "");
	}

	/**
	 * @param context
	 * @return password of the user or "" if not found
	 */
	public static String getPassword(Context context) {
		return getPrefs(context).getString(PASSWORD, "");
	}

	/**
	 * saves the user credentials to the preferences
	 * 
	 * @param context
	 * @param username
	 * @param password
	 */
	public static void saveUsernameAndPassword(Context context,
			String username, String password) {
		getPrefs(context).edit().putString(USERNAME, username)
				.putString(PASSWORD, password).commit();
	}

	/**
	 * checks if the user configured an account
	 * 
	 * @param context
	 * @return true if we have user credentials
	 */
	public static boolean hasUserCredentials(Context context) {
		String username = getUsername(context);
		String password = getPassword(context);

		return (!username.equals("") && !password.equals(""));
	}

	/**
	 * @param context
	 * @return true if the user has fast sharing enabled
	 */
	public static boolean isFastSharing(Context context) {
		return getPrefs(context).getBoolean(FASTSHARING, false);
	}

	/**
	 * @param context
	 * @return true if the user does not want to see the sharing finished screen
	 */
	public static boolean isHideConfirmation(Context context) {
		return getPrefs(context).getBoolean(HIDECONFIRMATION, false);
	}

	/**
	 * @param context
	 * @return true if the user has already seen the popup for the intro video
	 */
	public static boolean hasSeenYoutubeVideoPopup(Context context) {
		return getPrefs(context).getBoolean(SEENYOUTUBEVIDEO, false);
	}

	/**
	 * sets if the user has seen the popup for the intro video
	 * @param context
	 */
	public static void setSeenYoutubeVideoPopup(Context context) {
		getPrefs(context).edit().putBoolean(SEENYOUTUBEVIDEO, true).commit();
	}

	/**
	 * @param context
	 * @return true if the user wants sharing to happen in the background
	 */
	public static boolean isBackgroundSharing(Context context) {
		return getPrefs(context).getBoolean("BACKGROUNDSHARING", false);
	}
}
