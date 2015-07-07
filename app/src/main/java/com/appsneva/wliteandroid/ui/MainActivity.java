package com.appsneva.wliteandroid.ui;



import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsneva.wliteandroid.AlertDialogFragment;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import java.util.List;
import com.appsneva.wliteandroid.PlayerActivity;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.SearchActivity;
import com.appsneva.wliteandroid.VideoItem;
import com.appsneva.wliteandroid.YoutubeConnector;


public class MainActivity extends BaseActivity {

    /** The request code when calling startActivityForResult to recover from an API service error. */
    private static final int RECOVERY_DIALOG_REQUEST = 1;


    private ListView videosFound;
    private Handler handler;
    private Button addButton;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        videosFound = (ListView)findViewById(R.id.videos_found);
        handler = new Handler();
        addButton = (Button)findViewById(R.id.add_to_list_btn);


    // add view items and controls

        addClickListener();
        activateToolbar();
        checkYouTubeApi();
      //  addBtnListener();



        //check Parse User is current
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null){
            navigateToLogin();
        }
        else{
            Log.i(TAG, currentUser.getUsername());
        }

        searchOnYoutube("new music");
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
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
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
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }
                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.title);
//                TextView description = (TextView) convertView.findViewById(R.id.video_description);

                VideoItem searchResult = searchResults.get(position);



                Picasso.with(getApplicationContext()).load(searchResult.getThumbnail()).into(thumbnail);
                title.setText(searchResult.getTitle());
//                description.setText(searchResult.getDescription());
                Log.i("YOU", "Update video");
                return convertView;
            }
        };
        videosFound.setAdapter(adapter);
    }

    private void addClickListener(){

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

    private void addBtnListener(){

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
