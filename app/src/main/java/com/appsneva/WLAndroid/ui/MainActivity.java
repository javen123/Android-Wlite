package com.appsneva.WLAndroid.ui;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.appsneva.WLAndroid.AlertDialogFragment;
import com.appsneva.WLAndroid.DeveloperKey;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import java.util.List;
import com.appsneva.WLAndroid.R;
import com.appsneva.WLAndroid.VideoItem;
import com.appsneva.WLAndroid.YoutubeConnector;
import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends BaseActivity {
    /**
     * The request code when calling startActivityForResult to recover from an API service error.
     */
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
        videosFound = (ListView) findViewById(R.id.videos_found);
        handler = new Handler();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        // load toolbar
        activateToolbar();

        //confirm youTube API Check
        checkYouTubeApi();

        //check Parse User is current
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        }
        else {
            AlertDialogFragment.grabUserList(currentUser);
            // pull users lists if available
        }
        searchOnYoutube(getString(R.string.ma_searchOnYoutube));
    }  // onCreate


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String newQuery = sharedPref.getString("myQuery", "");
        Log.d("ITR", "Shared pref is " + newQuery);

        if (newQuery.isEmpty()) {
            return;
        }
        else {
            searchOnYoutube(newQuery);
        }
    }  // onResume


    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        }
        else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage = String.format(getString(R.string.ma_error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, LENGTH_LONG).show();
        }
    }  // checkYouTubeApi


    private void navigateToLogin() {
        MyLists.myArrayTitles.clear();
        Intent intent = new Intent(this, LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }  // navigateToLogin


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
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
                searchOnYoutube(newText);
                return false;
            }
        };  // SearchView.OnQueryTextListener textListener
        searchView.setOnQueryTextListener(textListener);
        return true;
    }  // onCreateOptionsMenu


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
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
    }  // onOptionsItemSelected


    private void searchOnYoutube(final String keywords) {
        toggleProgressBar();
        new Thread() {
            public void run() {
                searchResults = yc.search(keywords);
                handler.post(new Runnable() {
                    public void run() {
                        toggleProgressBar();
                        updateVideosFound();
                    }
                });
            }  // run
        }.start();  // Thread
    }  // searchOnYoutube


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
                Picasso.with(getApplicationContext()).load(searchResult.getThumbnail().trim()).resize(106,60).centerCrop().into(thumbnail);
                title.setText(searchResult.getTitle());
                Log.i("YOU", "Update video");
                return convertView;
            }
        };  // final ArrayAdapter
        videosFound.setAdapter(adapter);
    }  // updateVideosFound


    private void addRowClickListener() {
        if (videosFound != null) {
            videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
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
        }  // if (videosFound != null)
    }  // addRowClickListener


    // add dialog and button control for add item to list
    private void addItemToParseArray(int loc) {
        final String videoTitle = searchResults.get(loc).getTitle().toString();
        final String videoId = searchResults.get(loc).getId().toString();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("" + videoTitle);
        alert.setMessage(getString(R.string.ma_dialog_add));
        alert.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                query.orderByAscending("listTitle");
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e != null) {
                            Log.d("Error with list pull: ", e.getLocalizedMessage());
                        }
                        else {
                            if (list.isEmpty()) {
                                // If no searchlists saved for user
                                AlertDialogFragment.addItemAndList(
                                        MainActivity.this, videoId, getString(R.string.ma_dialog_congrats),
                                        getString(R.string.ma_dialog_msg_congrats));
                            }
                            else {
                                Log.d("ITR", "Alert should of triggered");
                                AlertDialogFragment.adjustListItems(0,list, MainActivity.this, videoId, getApplicationContext());
                            }
                        }
                    }  // done
                });  // query.findInBackground
            }  // onClick
        });  // alert.setPositiveButton

        alert.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }  // addItemToParseArray


    private void toggleProgressBar() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            videosFound.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            videosFound.setVisibility(View.VISIBLE);
        }
    }  // toggleProgressBar
}
