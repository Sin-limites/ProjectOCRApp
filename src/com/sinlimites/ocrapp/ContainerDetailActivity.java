package com.sinlimites.ocrapp;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContainerDetailActivity extends AppActivity {

	private final String JsonBaseUrl = "http://145.24.222.137:8080/RestService/rest/container/get/";
	private Intent intent;
	private ArrayList<TextView> tvList = new ArrayList<TextView>();

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container_detail);
		MyApplication.setActivity(this);

		intent = getIntent();
		String equipmentnumber = intent.getStringExtra("equipmentnumber");
		String JsonUrl = JsonBaseUrl + equipmentnumber;
		containerCode = (EditText) findViewById(R.id.container_detail_edit);

		GetTextViews();

		new JSONAsync(JsonUrl, this, tvList).execute();
		
		if(APIChecker.CheckApiLevel() >= Build.VERSION_CODES.JELLY_BEAN){
			containerCode.setBackground(getResources().getDrawable(R.drawable.edittext_border_low_version));
		}
		containerCode.setOnKeyListener(EditTextKeyListener((Button)findViewById(R.id.ListButton)));
	}

	/*
	 * Gets the layout containing the 2 TextViews and adds it to the ArrayList
	 */
	private void GetTextViews() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout viewParent = (LinearLayout) inflater.inflate(R.layout.container_field_layout, null);

		LinearLayout layout = (LinearLayout) viewParent.findViewById(R.id.container_list_item_layout);
		for (int i = 1; i < layout.getChildCount(); i = i + 2) {
			View tv = layout.getChildAt(i);
			if (tv instanceof TextView)
				tvList.add((TextView) tv);
		}
	}
}