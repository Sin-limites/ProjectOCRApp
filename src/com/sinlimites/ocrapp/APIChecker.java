package com.sinlimites.ocrapp;

import android.os.Build;

public class APIChecker {

	public static int CheckApiLevel(){
		return Build.VERSION.SDK_INT;
	}
}
