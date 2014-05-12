package com.sinlimites.ocrapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ContainerDetailActivity extends Activity {

	ListView listView;
	String JsonUrl;
	public JSONArray jsonArray;
	Intent intent;
	String equipmentnumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container_detail);

		intent = getIntent();
		equipmentnumber = intent.getStringExtra("equipmentnumber");
		JsonUrl = "http://145.24.222.137:8080/RestService/rest/container/get/"
				+ equipmentnumber;

		listView = (ListView) findViewById(R.id.list);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				int itemPosition = position;

				String itemValue = (String) listView
						.getItemAtPosition(position);

				Toast.makeText(
						getApplicationContext(),
						"Position :" + itemPosition + "  ListItem : "
								+ itemValue, Toast.LENGTH_LONG).show();
			}

		});

		new JSONAsync().execute();
	}

	public class JSONAsync extends AsyncTask<String, Void, String> {

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
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				} else {
					Log.e(ContainerDetailActivity.class.toString(),
							"Failed to download file");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder.toString();
		}

		protected void onPreExecute() {
			Toast.makeText(ContainerDetailActivity.this, "Please wait...",
					Toast.LENGTH_LONG).show();
		}

		protected void onPostExecute(String json) {
			try {
				JSONObject jsonObject = new JSONObject(json);
				System.out.println(jsonObject.getString("portofdischarge"));

				String[] values = new String[] {
						"Port of discharge: "
								+ jsonObject.getString("portofdischarge"),
						"Terminal: " + jsonObject.getString("terminal"),
						"Ship ID: " + jsonObject.getString("shipid"),
						"Equipmentnumber: "
								+ jsonObject.getString("equipmentnumber"),
						"Stowage Position: "
								+ jsonObject.getString("stowageposition"),
						"UNO: " + jsonObject.getString("uno"),
						"INO: " + jsonObject.getString("ino"),
						"Consignment number: "
								+ jsonObject.getString("consignmentnumber"),
						"Weight: " + jsonObject.getString("weight") + " kilo",
						"Quantity: "
								+ jsonObject.getString("quantityincontainer"),
						"Flashpoint: " + jsonObject.getString("flashpoint"),
						"Shipping name: ENVIRONMENTALLY HAZARDOUS SUBSTANCE, SOLID, N.O.S.",
						"Kind of package: CONTAINER" };

				populateList(values);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// http://145.24.222.137:8080/RestService/rest/container/get/PBAU3761225
		}

	}

	private void populateList(String[] values) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);

		listView.setAdapter(adapter);
	}

}
