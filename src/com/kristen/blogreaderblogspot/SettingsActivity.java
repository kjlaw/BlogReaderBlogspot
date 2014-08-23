package com.kristen.blogreaderblogspot;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class SettingsActivity extends ListActivity {
	
	protected static final String TAG = SettingsActivity.class.getSimpleName();;
	protected String[] mSettingsItems = { "Change Blog" };
	public static String mInputText = "firstinmichigan";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSettingsItems);
		setListAdapter (adapter);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Enter Blog Name");

    	// Set up the input
    	final EditText input = new EditText(this);
    	// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    	input.setInputType(InputType.TYPE_CLASS_TEXT);
    	builder.setView(input);

    	// Set up the buttons
    	builder.setPositiveButton("Save", new DialogInterface.OnClickListener() { 
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	        mInputText = input.getText().toString();
    	        
    	        //TODO finish below
    	        Intent intent = new Intent(SettingsActivity.this, MainListActivity.class);
    	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        		startActivity(intent);
    	        Log.v(TAG, mInputText);
    	    }
    	});
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	        dialog.cancel();
    	    }
    	});

    	builder.show();
    }

}