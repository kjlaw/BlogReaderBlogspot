package com.kristen.blogreaderblogspot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainListActivity extends ListActivity {
	
	public static final int NUMBER_OF_POSTS = 5;
	public static final String TAG = MainListActivity.class.getSimpleName();
	protected JSONObject mBlogData;
	protected ProgressBar mProgressBar;
	
	private final String KEY_TITLE = "title";
	private final String KEY_PUBLISHED = "published";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        
        if (isNetworkAvailable()) {
        	mProgressBar.setVisibility(View.VISIBLE);
        	GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
        	getBlogPostsTask.execute();
        }
        else {
        	Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }
        
        //Toast.makeText(this, getString(R.string.no_items), Toast.LENGTH_LONG).show();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int itemId = item.getItemId();
		
		if (itemId == R.id.action_settings) {
			viewSettings();
		}
		
    	return super.onOptionsItemSelected(item);
    }
    
    private void viewSettings() {
    	Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	try {
    		JSONObject jsonFeed = mBlogData.getJSONObject("feed");
    		JSONArray jsonEntries = jsonFeed.getJSONArray("entry");
    		JSONObject jsonEntry = jsonEntries.getJSONObject(position);
    		JSONArray jsonLinks = jsonEntry.getJSONArray("link");
    		String blogUrl = "";
    		for (int i = 0; i < jsonLinks.length(); i++) {
    			if (jsonLinks.getJSONObject(i).getString("rel").equals("alternate")) {
    				blogUrl = jsonLinks.getJSONObject(i).getString("href");
    			}
    		}
    		
    		Intent intent = new Intent(this, BlogWebViewActivity.class);
    		intent.setData(Uri.parse(blogUrl));
    		startActivity(intent);
    	}
    	catch (JSONException e) {
    		logException(e);
    	}
    }

    private void logException(Exception e) {
    	Log.e(TAG, "Exception caught!", e);
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		boolean isAvailable = false;
		if (networkInfo != null && networkInfo.isConnected()) {
			isAvailable = true;
		}
		
		return isAvailable;
	}

	public void handleBlogResponse() {
		mProgressBar.setVisibility(View.INVISIBLE);
		
		if (mBlogData == null) {
			updateDisplayForError();
		}
		else {
			try {
				JSONObject jsonFeed = mBlogData.getJSONObject("feed");
	    		JSONArray jsonEntries = jsonFeed.getJSONArray("entry");
				ArrayList<HashMap<String, String>> blogEntries = 
						new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < jsonEntries.length(); i++) {
					JSONObject entry = jsonEntries.getJSONObject(i);
					
					JSONObject titleObject = entry.getJSONObject(KEY_TITLE);
					String title = titleObject.getString("$t");
					title = Html.fromHtml(title).toString();
					
					JSONObject publishedObject = entry.getJSONObject(KEY_PUBLISHED);
					String tPublished = publishedObject.getString("$t");
					DateTime publishedDateTime = DateTime.parse(tPublished);
					String publishedDayOfWeek = publishedDateTime.dayOfWeek().getAsText();
					String publishedMonthOfYear = publishedDateTime.monthOfYear().getAsText();
					String publishedDayOfMonth = publishedDateTime.dayOfMonth().getAsText();
					String publishedYear = publishedDateTime.year().getAsText();
					String published = publishedDayOfWeek + ", " + publishedMonthOfYear + " " + publishedDayOfMonth + ", " + publishedYear;
					
					HashMap<String, String> blogEntry = new HashMap<String, String>();
					blogEntry.put(KEY_TITLE, title);
					blogEntry.put(KEY_PUBLISHED, published);
					
					blogEntries.add(blogEntry);
				}
				
				String[] keys = { KEY_TITLE, KEY_PUBLISHED };
				int[] ids = { android.R.id.text1, android.R.id.text2 };
				SimpleAdapter adapter = new SimpleAdapter(this, blogEntries,
						android.R.layout.simple_list_item_2, 
						keys, ids);
				
				setListAdapter(adapter);
			} 
			catch (JSONException e) {
				logException(e);
			}
		}
	}

	private void updateDisplayForError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.error_title));
		builder.setMessage(getString(R.string.error_message));
		builder.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
		
		TextView emptyTextView = (TextView) getListView().getEmptyView();
		emptyTextView.setText(getString(R.string.no_items));
	}
    
    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Object... arg0) {
			int responseCode = -1;
			JSONObject jsonResponse = null;
			
	        try {
	        	URL blogFeedUrl = new URL("http://" + SettingsActivity.mInputText + ".blogspot.com/feeds/posts/default?alt=json&max-results=" + NUMBER_OF_POSTS);
	        	HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
	        	connection.connect();
	        	
	        	responseCode = connection.getResponseCode();
	        	if (responseCode == HttpURLConnection.HTTP_OK) {
	        		InputStream inputStream = connection.getInputStream();
	        		Reader reader = new InputStreamReader(inputStream);
	        		int nextCharacter; // read() returns an int, we cast it to char later
	        		String responseData = "";
	        		while(true){ // Infinite loop, can only be stopped by a "break" statement
	        		    nextCharacter = reader.read(); // read() without parameters returns one character
	        		    if(nextCharacter == -1) // A return value of -1 means that we reached the end
	        		        break;
	        		    responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
	        		}
	        		
	        		jsonResponse = new JSONObject(responseData);
	        	}
	        	else {
	        		Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
	        	}
	        }
	        catch (MalformedURLException e) {
	        	logException(e);
	        }
	        catch (IOException e) {
	        	logException(e);
	        }
	        catch (Exception e) {
	        	logException(e);
	        }
	        
	        return jsonResponse;
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			mBlogData = result;
			handleBlogResponse();
		}
    	
    }
    
}
