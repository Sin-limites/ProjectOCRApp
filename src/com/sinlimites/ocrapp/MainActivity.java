package com.sinlimites.ocrapp;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends ActionBarActivity {
 
	TessBaseAPI baseApi = new TessBaseAPI();
	GPSLocTrack gps = new GPSLocTrack(this);
	ImageView imageView1;
	Button btnCapture;
	File file = new File(Environment.getExternalStorageDirectory()
			+ File.separator + "img.jpg");
	Bitmap thePic;
	TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initializeControls();
	}

	public void onClick(View v) {
		processTesseract();
		processGPS();
	}

	private void processTesseract() {
		String path = Environment.getExternalStorageDirectory().toString()
				+ "/tesseract/"; // Path to Tesseract dir

		baseApi.init(path, "eng");

		Pix img = ReadFile.readBitmap(thePic);
		baseApi.setImage(img);
		tv = (TextView) findViewById(R.id.IDTView);
		tv.setText(baseApi.getUTF8Text());

	}

	private void processGPS() {

		gps = new GPSLocTrack(MainActivity.this);

		// Only run if GPS is enabled on device
		if (gps.canGetLocation()) {
			gps.getLocation();
			// Set latitude and longtitude location to textview
			TextView locView = (TextView) findViewById(R.id.TextViewLoc);
			locView.setText("Locatie: " + String.valueOf(gps.getLatitude())
					+ ", " + String.valueOf(gps.getLongitude()));
		} else {
			// If application can't get GPS location, allow user to change GPS
			// settings
			gps.showSettingsAlert();
		}

	}

	public void onClickMap(View v) {
		Intent intent = new Intent(this, GoogleMapAPI.class);
		startActivity(intent);
	}
	
	public void onClickList(View v) {
		Intent intent = new Intent(this, ContainerDetailActivity.class);
		intent.putExtra("equipmentnumber", tv.getText());
		//System.out.println(tv.getText());
		startActivity(intent);
	}

	private void initializeControls() {
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		btnCapture = (Button) findViewById(R.id.Capture);
		btnCapture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Create an instance of intent Pass action
				// android.media.action.IMAGE_CAPTURE as argument -> Lauches
				// camera.

				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

				// put file uri as extra

				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

				// Start activity for result Pass intent (arg, req code)

				startActivityForResult(intent, 0);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0) {
			// Create instance of File
			// -> Access file from storage
			File file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "img.jpg");
			// Start crop via new intent
			try {
				cropCapturedImage(Uri.fromFile(file));
			} catch (ActivityNotFoundException aNFE) {
				// Capture error if crop action is not supported by device
				String errorMessage = "Sorry - your device doesn't support the crop action!";
				Toast toast = Toast.makeText(this, errorMessage,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		}
		if (requestCode == 1) {
			// Create bundle instance
			// Get the return value
			Bundle extras = data.getExtras();
			// Get cropped bitmap
			thePic = extras.getParcelable("data");
			// Set cropped image to imageview
			imageView1.setImageBitmap(thePic);
		}
	}

	public void cropCapturedImage(Uri picUri) {
		// Call cropping intent
		Intent cropIntent = new Intent("com.android.camera.action.CROP");
		// Set image uri and type
		cropIntent.setDataAndType(picUri, "image/*");
		// Set crop properties
		cropIntent.putExtra("crop", "true");
		cropIntent.putExtra("aspectX", 300);
		cropIntent.putExtra("aspectY", 300);
		cropIntent.putExtra("outputX", 300);
		cropIntent.putExtra("outputY", 300);
		// Get data at return
		cropIntent.putExtra("return-data", true);
		// Start activity (arg, requestCode)
		startActivityForResult(cropIntent, 1);
	}

}

// The code below in comments is the code to read text from >>pre-saved<<
// images. - Brian

/*
 * String path = Environment.getExternalStorageDirectory().toString() +
 * "/tesseract/";
 * 
 * baseApi.init(path, "eng");
 * 
 * baseApi.setImage(BitmapFactory.decodeResource(this.getResources(),
 * R.drawable.container)); Toast.makeText(this, baseApi.getUTF8Text(),
 * Toast.LENGTH_LONG).show(); TextView tv = (TextView)
 * findViewById(R.id.textView1); tv.setText(baseApi.getUTF8Text());
 */
