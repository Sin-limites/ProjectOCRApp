package com.sinlimites.ocrapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnKeyListener;

public class AppActivity extends Activity {
	
	protected EditText containerCode;

	/*
	 * OnClick for the button so the user can get the entered Container
	 * information
	 */
	public void onClickList(View v) {
		if (checkEquipmentNumber()) {
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
		return (!containerCode.getText().toString().equals("") && !containerCode.getText().toString().equals(getResources().getString(R.string.container_id)) && matcher.matches());
	}
	
	public void onClickCheckText(View v){
		if(v instanceof EditText){
			EditText editText = (EditText)v;
			if(editText.getText().toString().equals(getResources().getString(R.string.container_id)))
				editText.setText("");
		}
	}

	public OnKeyListener EditTextKeyListener(final Button button){
		return new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
				    System.out.println("Enter ingedrukt!");
				    button.performClick();
				}
				return false;
			}
		};
	}
}
