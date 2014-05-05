package com.sinlimites.ocrtestapp;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	protected Button _button;
	protected ImageView _image;
	protected TextView _field;
	protected String _path;
	protected boolean _taken;
		
	protected static final String PHOTO_TAKEN = "photo_taken";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		_image = ( ImageView ) findViewById( R.id.image );
	    _field = ( TextView ) findViewById( R.id.field );
	    _button = ( Button ) findViewById( R.id.button );
	    _button.setOnClickListener( new ButtonClickHandler() );
	        
	     _path = Environment.getExternalStorageDirectory() + "/images/make_machine_example.jpg";
	}

	

}
