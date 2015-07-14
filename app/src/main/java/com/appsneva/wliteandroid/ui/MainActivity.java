package com.appsneva.wliteandroid.ui;



import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import com.appsneva.wliteandroid.AlertDialogFragment;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import java.util.List;
import com.appsneva.wliteandroid.PlayerActivity;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.SearchActivity;
import com.appsneva.wliteandroid.VideoItem;
import com.appsneva.wliteandroid.YoutubeConnector;

import static android.widget.Toast.LENGTH_LONG;


public class MainActivity extends BaseActivity {

    /** The request code when calling startActivityForResult to recover from an API service error. */
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    protected String curUser;
    private ListView videosFound;
    private Handler handler;


    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videosFound = (ListView)findViewById(R.id.videos_found);
        handler = new Handler();

        activateToolbar();

        //confirm youTube API Check
        checkYouTubeApi();


        //check Parse User is current

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null){
            navigateToLogin();
        }
        else{
            // pull users lists if available
            Log.i(TAG, currentUser.getUsername());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
            curUser = currentUser.getObjectId();
            query.whereEqualTo("userRelation", curUser);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e != null) {
                        Log.d("Error with list pull: ", e.getLocalizedMessage());
                    } else {
                        Log.d("User's saved list: ", list.toString());
                        for (int i = 0; i < list.size(); i++) {
                            String vidId = list.toString();
                            MyLists.myArrayTitles.add(vidId);
                        };
                    }
                }
            });
        }


        searchOnYoutube("new hit music");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(handler != null){

            String query = getSavedPreferences(YOUTUBE_QUERY);
            if(query.length() > 0) searchOnYoutube(query);
        }
    }

    private String getSavedPreferences(String key){
        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharePref.getString(key, "");
    }

    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason =
                YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage =
                    String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, LENGTH_LONG).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            ParseUser.logOut();
            navigateToLogin();
        }
        if(id == R.id.menu_search){
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.menu_my_lists){
            Intent intent = new Intent(this, MyLists.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
    private List<VideoItem> searchResults;

    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(MainActivity.this);
                searchResults = yc.search(keywords);
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }
    private void updateVideosFound() {
        final ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }

                // row item tapped action / send to YouTube player
                addRowClickListener();

                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.title);
                Button button = (Button)convertView.findViewById(R.id.add_to_list_btn);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addItemToParseArray(position);
                    }
                });


                VideoItem searchResult = searchResults.get(position);

                Picasso.with(getApplicationContext()).load(searchResult.getThumbnail()).into(thumbnail);
                title.setText(searchResult.getTitle());
                Log.i("YOU", "Update video");
                return convertView;
            }
        };
        videosFound.setAdapter(adapter);
    }

    private void addRowClickListener(){

        if(videosFound != null){
            videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> av, View v, int pos,
                                        long id) {
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                    startActivity(intent);
                }
            });
        }
    }

    // add dialog and button control for add item to list
private void addItemToParseArray(int loc){

    String videoTitle = searchResults.get(loc).getTitle().toString();
    Log.d("PARSE PULLED TITLE:", videoTitle);
    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("" + videoTitle);
    alert.setMessage("will be added to your lists");
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
            query.whereEqualTo("userRelation", curUser);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e != null) {
                        Log.d("Error with list pull: ", e.getLocalizedMessage());
                    } else if (list.isEmpty()) {

                    }
                }
            });
            ParseObject userAdd = new ParseObject("Lists");

        }
    });
    alert.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    });
    AlertDialog alertDialog = alert.create();
    alertDialog.show();
}



}
