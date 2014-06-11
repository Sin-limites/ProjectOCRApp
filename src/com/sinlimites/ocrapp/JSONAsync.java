package com.sinlimites.ocrapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class JSONAsync extends AsyncTask<String, Void, String> {

	private String JsonUrl;
	private Context context;
	private ArrayList<TextView> tvList = new ArrayList<TextView>();
	private String succeeded = "";
	private Activity activity;

	public JSONAsync(String jsonURL, ContainerDetailActivity containerDetailActivity, ArrayList<TextView> tvList) {
		this.JsonUrl = jsonURL;
		this.context = containerDetailActivity;
		this.tvList = tvList;
		this.activity = MyApplication.getActivity();
	}

	/*
	 * Connects to the server and gets an JSON string as return type.
	 * 
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected String doInBackground(String... params) {

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(JsonUrl);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else if (statusCode == 500){
				succeeded = activity.getResources().getString(R.string.invalid_equipment_number);
			} else {
				succeeded = activity.getResources().getString(R.string.failed_download_file);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			succeeded = activity.getResources().getString(R.string.failed_connect);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	/*
	 * A simple message to show the user, that its connecting to the server.
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	protected void onPreExecute() {
		Toast.makeText(context, R.string.connecting_to_server, Toast.LENGTH_SHORT).show();
	}

	/*
	 * Checks if the JSON is an JSONArray or JSONObject.
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	protected void onPostExecute(String JsonString) {
		if (!JsonString.equals("")){
			try {
				Object JSON = new JSONTokener(JsonString).nextValue();

				if (JSON instanceof JSONArray) {
					JSONArray JsonArray = (JSONArray) JSON;
					for (int i = 0; i < JsonArray.length(); i++) {
						JSONObject JsonObject = JsonArray.getJSONObject(i);
						IterateJsonObject(JsonObject);
					}
				} else if (JSON instanceof JSONObject) {
					JSONObject JsonObject = (JSONObject) JSON;
					IterateJsonObject(JsonObject);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(succeeded.equals(activity.getResources().getString(R.string.invalid_equipment_number))){
			Toast.makeText(context, succeeded, Toast.LENGTH_SHORT).show();
			ShowEditText();
		} else {
			Toast.makeText(context, succeeded, Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * Iterates through the JSON and sets all the TextViews with the data.
	 */
	@SuppressLint("NewApi")
	private void IterateJsonObject(JSONObject JsonObject) {
		try {
			Iterator<?> keys = JsonObject.keys();

			while (keys.hasNext()) {
				String key = (String) keys.next();
				int resourceId = getResId(key, com.sinlimites.ocrapp.R.string.class);

				if (key.equals("equipmentnumber")) {
					String headerTitle = activity.getResources().getText(resourceId) + " " + JsonObject.getString(key);
					if (APIChecker.CheckApiLevel() >= Build.VERSION_CODES.HONEYCOMB) {
						ActionBar actionBar = activity.getActionBar();
						actionBar.setTitle(headerTitle);
						actionBar.show();
					} else {
						TextView tv = (TextView) activity.findViewById(R.id.old_os_header);
						tv.setText(headerTitle);
						tv.setVisibility(View.VISIBLE);
					}
				} else {
					LinearLayout sv = (LinearLayout) activity.findViewById(R.id.scroll_list_layout);
					LinearLayout ll = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.container_field_layout, null);
					TextView header = (TextView) ll.findViewById(R.id.json_header);
					TextView content = (TextView) ll.findViewById(R.id.json_content);

					if (resourceId != -1)
						header.setText(activity.getResources().getText(resourceId));
					else
						header.setText(key);
					if(activity.getResources().getText(resourceId).equals(activity.getResources().getText(R.string.weight)))
						content.setText(JsonObject.getString(key) + " " + activity.getResources().getText(R.string.weight_unit));
					else
						content.setText(JsonObject.getString(key));
					sv.addView(ll);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * Fills the TextViews with the data returned from the JSON.
	 */
	public void FillTextViews(String jsonString) {
		JSONObject jObject;
		try {
			jObject = new JSONObject(jsonString.trim());
			Iterator<?> keys = jObject.keys();
			int i = 0;

			while (keys.hasNext()) {
				String key = (String) keys.next();
				tvList.get(i).setText(jObject.getString(key));
				i++;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/*
	 * An method that returns the ResourceId of the variable name.
	 */
	public static int getResId(String variableName, Class<?> c) {

		try {
			Field idField = c.getDeclaredField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/*
	 * Shows an EditText if invalid code is entered, so the user can enter an new code.
	 */
	private void ShowEditText() {
		RelativeLayout layout = (RelativeLayout)activity.findViewById(R.id.edit_layout);
		layout.setVisibility(View.VISIBLE);
		ScrollView scrollView = (ScrollView)activity.findViewById(R.id.scroll_list);
		scrollView.setVisibility(View.INVISIBLE);
	}
}
