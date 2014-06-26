package com.sinlimites.ocrapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import com.sinlimites.ocrapp.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class GoogleMapAPI extends Activity {

	GPSLocTrack gps = new GPSLocTrack(this);
	
	/**
	 * Implements a fragment with the Google maps API
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_fragment);

		// Get a handle to the Map Fragment
		GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();

		gps = new GPSLocTrack(GoogleMapAPI.this);

		// Only run if GPS is enabled on device
		if (gps.canGetLocation()) {
			gps.getLocation();
			LatLng loc = new LatLng(gps.getLatitude(), gps.getLongitude());
			map.setMyLocationEnabled(true);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

			map.addMarker(new MarkerOptions().title("Container ID: XXXXXXX")
					.snippet("De locatie van deze container.")
					.position(loc));
			
			CameraPosition cameraPosition = CameraPosition.builder()
	                .target(loc)
	                .zoom(16)
	                .bearing(90)
	                .build();
	        
	        // Animate the change in camera view over 2 seconds
	        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
	                5000, null);
		} else {
			gps.showSettingsAlert();
		}

	}
}
