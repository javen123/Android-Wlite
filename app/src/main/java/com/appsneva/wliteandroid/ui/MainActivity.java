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
import android.text.Layout;
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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import com.appsneva.wliteandroid.PlayerActivity;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.SearchActivity;
import com.appsneva.wliteandroid.VideoItem;
import com.appsneva.wliteandroid.YoutubeConnector;

import org.json.JSONArray;

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

        // load toolbar
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
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
            curUser = currentUser.getObjectId();
            query.whereEqualTo("createdBy", curUser);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e != null) {
                        Log.d("Error with list pull: ", e.getLocalizedMessage());
                    } else {
                        Log.d("User's saved list: ", list.toString());
                        for (ParseObject object : list) {
                            String title = (object.get("listTitle").toString());
                            MyLists.myArrayTitles.add(title);
                        }
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

    final String videoTitle = searchResults.get(loc).getTitle().toString();
    final String videoId = searchResults.get(loc).getId().toString();
    Log.d("PARSE PULLED TITLE:", videoTitle);
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("" + videoTitle);
    alert.setMessage("will be added to your lists");
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
            query.whereEqualTo("createdBy", curUser);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e != null) {
                        Log.d("Error with list pull: ", e.getLocalizedMessage());
                    } else {
                        if (list.isEmpty()) {
                            View v = getLayoutInflater().inflate(R.layout.first_list_title, null);
                            final AlertDialog.Builder newTitle = new AlertDialog.Builder(MainActivity.this);
                            newTitle.setView(v);
                            final EditText userTitleView = (EditText) v.findViewById(R.id.first_title);
                            final AlertDialog.Builder builder = newTitle.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //get user added title from alert dialog
                                    String mTitle = userTitleView.getText().toString();

                                    //instantiate new array for videoId to load to Parse
                                    ArrayList<String> firstIdToAdd;
                                    firstIdToAdd = new ArrayList<String>();
                                    firstIdToAdd.add(videoId);

                                    // initiate Parse object to being adding
                                    ParseObject firstList = new ParseObject("Lists");
                                    firstList.put("listTitle", mTitle);
                                    firstList.put("myLists", firstIdToAdd);

                                    // create user relation
                                    ParseRelation<ParseObject> relation = firstList.getRelation("createdBy");
                                    relation.add(ParseUser.getCurrentUser());
                                    firstList.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                final AlertDialog.Builder success = new AlertDialog.Builder(MainActivity.this);
                                                success.setTitle("Congrats");
                                                success.setMessage("You just saved yuour first list");
                                                success.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });
                                                AlertDialog alert = success.create();
                                                alert.show();
                                            } else {
                                                final AlertDialog.Builder success = new AlertDialog.Builder(MainActivity.this);
                                                success.setTitle("Opps");
                                                success.setMessage("" + e.getLocalizedMessage());
                                                success.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });
                                                AlertDialog alert = success.create();
                                                alert.show();
                                            }
                                        }
                                    });
                                }
                            });
                            AlertDialog addTitle = newTitle.create();
                            addTitle.show();

                        } else {
                            ArrayList<String> listTitles = new ArrayList<String>();
                            for (ParseObject titles : list) {
                                String title = (titles.get("listTitle").toString());
                                listTitles.add(title);
                            }
                            final CharSequence[] titles = listTitles.toArray(new CharSequence[listTitles.size()]);


                            final AlertDialog.Builder success = new AlertDialog.Builder(MainActivity.this);
                            success.setTitle("Add to ");
                            success.setItems(titles, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String titleTapped = titles[which].toString();

                                    ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Lists");
                                    query1.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                                    query1.whereEqualTo("listTitle", titleTapped);
                                    query1.getFirstInBackground(new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject object, ParseException e) {
                                            object.add("myLists", videoId);
                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e != null) {
                                                        // add error handling
                                                    } else {
                                                        //tell them how awesome they are and to use theLists page to manage their lists
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            AlertDialog alert = success.create();
                            alert.show();

                        }
                    }
                }
            });
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
