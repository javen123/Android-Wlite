package com.appsneva.wliteandroid.ui;



import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Arrays;
import java.util.List;
import com.appsneva.wliteandroid.PlayerActivity;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.SearchActivity;
import com.appsneva.wliteandroid.VideoItem;
import com.appsneva.wliteandroid.YoutubeConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.widget.Toast.LENGTH_LONG;


public class MainActivity extends BaseActivity {

    /** The request code when calling startActivityForResult to recover from an API service error. */
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    protected String curUser;
    private ListView videosFound;
    private Handler handler;
    private List<VideoItem> searchResults;

    private ProgressBar mProgressBar;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videosFound = (ListView)findViewById(R.id.videos_found);
        handler = new Handler();
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
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
                            ParseObject temp = object;
                            MyLists.myArrayTitles.add(object);
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
            MyLists.myArrayTitles.clear();
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


    private void searchOnYoutube(final String keywords){
        toggleProgressBar();
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(MainActivity.this);
                searchResults = yc.search(keywords);
                handler.post(new Runnable(){
                    public void run(){
                        toggleProgressBar();
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
            videosFound.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    addItemToParseArray(position);
                    return true;
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
    alert.setMessage("Add to a Searchlist?");
    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
                            View v = getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
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
                                                success.setMessage("You just saved your first list");
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

                            // set up list titles for alert
                            ArrayList<String> listTitles = new ArrayList<String>();
                            for (ParseObject titles : list) {
                                String title = (titles.get("listTitle").toString());
                                listTitles.add(title);
                            }
                            final CharSequence[] titles = listTitles.toArray(new CharSequence[listTitles.size()]);


                            final AlertDialog.Builder success = new AlertDialog.Builder(MainActivity.this);
                            success.setTitle("Select Searchlist:");
                            success.setItems(titles, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // pull list title
                                    String titleTapped = titles[which].toString();

                                    // convertid to Parse array type
                                    final ArrayList<String> moreToAdd;
                                    moreToAdd = new ArrayList<String>();
                                    moreToAdd.add(videoId);

                                    //query by list title
                                    ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Lists");
                                    query1.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                                    query1.whereEqualTo("listTitle", titleTapped);
                                    query1.getFirstInBackground(new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject object, ParseException e) {
//                                            Array id = new Array();
                                            if (object.get("myLists") == null) {

                                                object.put("myLists", moreToAdd);
                                                object.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e != null) {
                                                            Log.d("Parse:", e.getLocalizedMessage());
                                                        } else {
                                                            Log.d("Parse:", "item was supposed to be saved");
                                                            Toast.makeText(getApplicationContext(), "Video saved", LENGTH_LONG).show();
                                                        }

                                                    }

                                                });
                                            } else {
                                                ArrayList<String> addMore = new ArrayList<String>();

                                                JSONArray myList = object.getJSONArray("myLists");
                                                for (int i = 0; i < myList.length(); i++) {
                                                    try {
                                                        addMore.add(myList.get(i).toString());
                                                    } catch (JSONException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                                addMore.add(videoId);
                                                object.put("myLists", addMore);
                                                object.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e != null) {
                                                            Log.d("Parse:", e.getLocalizedMessage());
                                                        } else {
                                                            Log.d("Parse:", "item was supposed to be saved");
                                                            Toast.makeText(getApplicationContext(), "Video saved", LENGTH_LONG).show();
                                                        }

                                                    }

                                                });

                                            }
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
    alert.setNegativeButton("Later", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    });
    AlertDialog alertDialog = alert.create();
    alertDialog.show();
}

    private void toggleProgressBar(){
        if(mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            videosFound.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            videosFound.setVisibility(View.VISIBLE);
        }
    }
}
