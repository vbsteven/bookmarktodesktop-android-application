/* 
 * Copyright (C) 2010-2011 Steven Van Bael <steven.v.bael@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */
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
