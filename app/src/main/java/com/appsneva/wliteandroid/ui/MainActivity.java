package com.appsneva.wliteandroid.ui;



import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
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

import com.appsneva.wliteandroid.AlertDialogFragment;
import com.appsneva.wliteandroid.DeveloperKey;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import java.util.List;

import com.appsneva.wliteandroid.R;

import com.appsneva.wliteandroid.VideoItem;
import com.appsneva.wliteandroid.YoutubeConnector;

import static android.widget.Toast.LENGTH_LONG;


public class MainActivity extends BaseActivity {

    /** The request code when calling startActivityForResult to recover from an API service error. */
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    protected String curUser;
    private ListView videosFound;
    private Handler handler;
    private List<VideoItem> searchResults;
    private ProgressBar mProgressBar;
    protected YoutubeConnector yc;
    private SearchView searchView;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        yc = new YoutubeConnector(MainActivity.this);
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

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        SearchView.OnQueryTextListener textListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchResults.clear();
                searchOnYoutube(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        };
        searchView.setOnQueryTextListener(textListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.action_logout:
                MyLists.myArrayTitles.clear();
                ParseUser.logOut();
                navigateToLogin();
                return true;
            case R.id.menu_search:

                return true;
            case R.id.menu_my_lists:
                Intent intent = new Intent(this, MyLists.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(getIntent());
    }

    private void searchOnYoutube(final String keywords){
        toggleProgressBar();
        new Thread(){
            public void run(){

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
    public void updateVideosFound() {
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
                    List<String> vidIds = new DetailListView().convertSearchResultsToIntentIds(searchResults);
                    Intent intent = YouTubeStandalonePlayer.createVideosIntent(MainActivity.this, DeveloperKey.DEVELOPER_KEY, vidIds, pos, 10, true, true);
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
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("" + videoTitle);
    alert.setMessage("will be added to your lists");
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
            query.whereEqualTo("createdBy", curUser);
            query.orderByAscending("listTitle");
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

                            AlertDialogFragment.adjustListItems(list, MainActivity.this, videoId, getApplicationContext());

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

    private void handleIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(getApplicationContext(),""+query, LENGTH_LONG).show();
        }
    }



}
