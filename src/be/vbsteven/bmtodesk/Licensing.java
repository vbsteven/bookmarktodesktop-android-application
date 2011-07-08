package be.vbsteven.bmtodesk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Licensing {


	public static boolean verify(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo("be.vbsteven.bmtodesklicense", 0);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}
}
