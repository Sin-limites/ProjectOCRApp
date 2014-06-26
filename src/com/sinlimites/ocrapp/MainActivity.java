package com.sinlimites.ocrapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
	private File file = new File(Environment.getExternalStorageDirectory() + File.separator + "img.jpg");
	private Bitmap thePic;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MyApplication.setActivity(this);

		containerCode = (EditText) findViewById(R.id.IDTView);
		containerCode.setOnKeyListener(EditTextKeyListener((Button) findViewById(R.id.ListButton)));

		if (APIChecker.CheckApiLevel() >= Build.VERSION_CODES.JELLY_BEAN) {
			containerCode.setBackground(getResources().getDrawable(R.drawable.edittext_border_low_version));
		}
	}

	/**
	 * Process the Tesseract if the picture is taken.
	 * 
	 * @param v
	 */
	public void onClick(View v) {
		if (thePic != null) {
			String code = CheckForContainerCode(processTesseract());
			if(!code.equals(""))
				containerCode.setText(code);
			else
				Toast.makeText(this, R.string.no_code_found, Toast.LENGTH_SHORT).show();
			
			processGPS();
		} else
			Toast.makeText(this, R.string.no_picture, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Return the String Tesseract has found inside the Bitmap
	 * @param image
	 * @return
	 */
	private String processTesseract() {
		String tesseractFolder = Environment.getExternalStorageDirectory() + "/tesseract/tesseract-ocr/";
		String path = null;
		if (Environment.getExternalStorageDirectory() != null) {
			File folder = new File(tesseractFolder + "/tessdata");
			if (!folder.exists()) {
				try {
					String assetsFolder[] = getAssets().list("tesseract-ocr");
					for (int i = 0; i < assetsFolder.length; i++)
						if (assetsFolder[i].equals("tessdata"))
							copyFolderToExternalStorage("tesseract-ocr/" + assetsFolder[i]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			path = tesseractFolder;
		}
		// Path to Tesseract dir
		else
			path = getFilesDir() + "/tesseract/";

		baseApi.init(path, "eng");

		Pix img = ReadFile.readBitmap(thePic);
		baseApi.setImage(img);
		return baseApi.getUTF8Text();
	}
	
	/**
	 * Copy the folder to the ExternalStorage. 
	 * @param name
	 */
	private void copyFolderToExternalStorage(String name) {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list(name);
			for (String filename : files) {
				InputStream in = null;
				OutputStream out = null;
				// First: checking if there is already a target folder
				File folder = new File(Environment.getExternalStorageDirectory() + "/tesseract/" + name);
				boolean success = true;
				if (!folder.exists()) {
					success = folder.mkdirs();
				}
				if (success) {
					try {
						in = assetManager.open(name + "/" + filename);
						out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/tesseract/" + name + "/" + filename);
						Log.i("WEBVIEW", Environment.getExternalStorageDirectory() + "/tesseract/" + name + "/" + filename);
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
					} catch (IOException e) {
						Log.e("ERROR", "Failed to copy asset file: " + filename, e);
					}
				}
			}
		} catch (IOException e) {
			Log.e("ERROR", "Failed to get asset file list.", e);
		}
	}
	
	/**
	 * Copy a file to the ouput stream
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	/**
	 * Get the GPS
	 */
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

	/**
	 * Start the Google map Activity and API
	 * 
	 * @param v
	 */
	public void onClickMap(View v) {
		Intent intent = new Intent(this, GoogleMapAPI.class);
		startActivity(intent);
	}

	public void onClickList(View v) {
		if (checkEquipmentNumber()) {
			Intent intent = new Intent(this, ContainerDetailActivity.class);
			intent.putExtra("equipmentnumber", containerCode.getText().toString());
			startActivity(intent);
		} else {
			Toast.makeText(this, R.string.wrong_code, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Checks if the inserted code isn't empty or equal to the default string.
	 */
	private boolean checkEquipmentNumber() {
		Pattern regex = Pattern.compile("(^[a-zA-Z]{4})([0-9]{7})");
		Matcher matcher = regex.matcher(containerCode.getText().toString());
		return (!containerCode.getText().toString().equals("") && !containerCode.getText().toString().equals(getResources().getString(R.string.container_id)) && matcher.matches());
	}

	/**
	 * Create an Intent with the action android.media.action.IMAGE_CAPTURE which
	 * starts the camera
	 */
	public void onCapClick(View v) {
		cameraImage = (ImageView) findViewById(R.id.imageView1);
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		startActivityForResult(intent, 0);
	}

	/**
	 * Crop the image so it gets the best output.
	 */
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
			// Create bundle instance and get the return value
			if (data != null) {
				Bundle extras = data.getExtras();
				// Get cropped bitmap
				thePic = extras.getParcelable("data");
				// Set cropped image to imageview
				cameraImage.setImageBitmap(thePic);
				onClick(null);
			} else {
				thePic = null;
				Toast.makeText(this, R.string.no_picture, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
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

	/**
	 * Regex and scan the picture for the code.
	 * 
	 * @param code
	 * @return
	 */
	private String CheckForContainerCode(String code) {
		code = code.replaceAll(" ", "");
		Pattern regex = Pattern.compile("([a-zA-Z]{4})([0-9]{7})");
		Matcher matcher = regex.matcher(code);
		if (matcher.find())
			return matcher.group();
		else
			return "";
	}
}