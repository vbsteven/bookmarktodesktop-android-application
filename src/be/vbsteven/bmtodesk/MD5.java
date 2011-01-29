package be.vbsteven.bmtodesk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public static String md5(String s) {
		String result = "";
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte passDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexPass = new StringBuffer();
			for (int i = 0; i < passDigest.length; i++) {
				String h = Integer.toHexString(0xFF & passDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexPass.append(h);
			}
			result = hexPass.toString();
		} catch (NoSuchAlgorithmException e) {
		}
		return result;
	}
	
	 public static byte[] md5Bytes(String s) {
	    try {
	      MessageDigest digest = java.security.MessageDigest
	          .getInstance("MD5");
	      digest.update(s.getBytes());
	      byte passDigest[] = digest.digest();
	      return passDigest;
	    } catch (NoSuchAlgorithmException e) {
	    }
	    return null;
	  }
}
