package com.sinlimites.ocrapp;

import android.os.Build;

public class APIChecker {

	/**
	 * Return the current API level.
	 * @return
	 */
	public static int CheckApiLevel(){
		return Build.VERSION.SDK_INT;
	}
}
