package com.sinlimites.ocrapp;

import android.app.Activity;
import android.os.Bundle;

import com.example.mobtest.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class GoogleMapAPI extends Activity {

	GPSLocTrack gps = new GPSLocTrack(this);

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
			LatLng loc = new LatLng(gps.getLatitude(), gps.getLongitude());
			map.setMyLocationEnabled(true);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

			map.addMarker(new MarkerOptions().title("Container")
					.snippet("De locatie van deze container.")
					.position(loc));
		} else {
			gps.showSettingsAlert();
		}

	}
}
