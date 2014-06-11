package com.sinlimites.ocrapp;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends AppActivity {

	private TessBaseAPI baseApi = new TessBaseAPI();
	private GPSLocTrack gps = new GPSLocTrack(this);
	private ImageView cameraImage;
	private Button btnCapture;
	private File file = new File(Environment.getExternalStorageDirectory() + File.separator + "img.jpg");
	private Bitmap thePic;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MyApplication.setActivity(this);
		initializeControls();

		containerCode = (EditText) findViewById(R.id.IDTView);
		containerCode.setOnKeyListener(EditTextKeyListener((Button)findViewById(R.id.ListButton)));
		
		
		if(APIChecker.CheckApiLevel() >= Build.VERSION_CODES.JELLY_BEAN){
			containerCode.setBackground(getResources().getDrawable(R.drawable.edittext_border_low_version));
		}
	}

	public void onClick(View v) {
		processTesseract();
		processGPS();
	}

	private void processTesseract() {
		String path = null;
		if (Environment.getExternalStorageDirectory() != null)
			path = Environment.getExternalStorageDirectory().toString() + "/tesseract/"; 
		// Path to Tesseract dir
		else
			path = getFilesDir() + "/tesseract/";

		baseApi.init(path, "eng");

		Pix img = ReadFile.readBitmap(thePic);
		baseApi.setImage(img);

		containerCode.setText(baseApi.getUTF8Text());

	}

	private void processGPS() {

		gps = new GPSLocTrack(MainActivity.this);

		// Only run if GPS is enabled on device
		if (gps.canGetLocation()) {
			gps.getLocation();
			// Set latitude and longtitude location to textview
			TextView locView = (TextView) findViewById(R.id.TextViewLoc);
			locView.setText(R.string.location_header + String.valueOf(gps.getLatitude()) + ", " + String.valueOf(gps.getLongitude()));
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
		if(checkEquipmentNumber()){
			Intent intent = new Intent(this, ContainerDetailActivity.class);
			intent.putExtra("equipmentnumber", containerCode.getText().toString());
			startActivity(intent);
		} else {
			Toast.makeText(this, R.string.wrong_code, Toast.LENGTH_SHORT).show();
		}
	}
	
	/*
	 * Checks if the inserted code isn't empty or equal to the default string.
	 */
	private boolean checkEquipmentNumber() {
		Pattern regex = Pattern.compile("(^[a-zA-Z]{4})([0-9]{7})");
		Matcher matcher = regex.matcher(containerCode.getText().toString());
		return (!containerCode.getText().toString().equals("") &&
				!containerCode.getText().toString().equals(getResources().getString(R.string.container_id)) &&
				matcher.matches());
	}

	/*
	 * Create an Intent with the action android.media.action.IMAGE_CAPTURE which
	 * starts the camera
	 */
	private void initializeControls() {
		cameraImage = (ImageView) findViewById(R.id.imageView1);
		btnCapture = (Button) findViewById(R.id.Capture);
		btnCapture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
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
			File file = new File(Environment.getExternalStorageDirectory() + File.separator + "img.jpg");
			// Start crop via new intent
			try {
				cropCapturedImage(Uri.fromFile(file));
			} catch (ActivityNotFoundException aNFE) {
				// Capture error if crop action is not supported by device
				String errorMessage = "Sorry - your device doesn't support the crop action!";
				Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
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
			cameraImage.setImageBitmap(thePic);
		}
	}

	/*
	 * This method starts the Activity so the user can crop the picture.
	 */
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
