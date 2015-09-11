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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsneva.WLAndroid.AlertDialogFragment;
import com.appsneva.WLAndroid.DeveloperKey;
import com.appsneva.WLAndroid.R;
import com.appsneva.WLAndroid.VideoItem;
import com.appsneva.WLAndroid.YoutubeConnector;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchViewActivity extends BaseActivity {

    public static final String YT_QUERY = "YTSEARCH";

    private SearchView mSearchView;
    private List<VideoItem> searchResults;
    private ProgressBar mProgressBar;
    protected YoutubeConnector yc;
    private ListView videosFound;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);

        activateToolbarWithHomeEnabled();

        yc = new YoutubeConnector(SearchViewActivity.this);
        videosFound = (ListView) findViewById(R.id.videos_found);
        handler = new Handler();
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        searchOnYoutube(getString(R.string.ma_searchOnYoutube));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search_view, menu);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView)menu.findItem(R.id.main_search_view).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
//                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                sharedPref.edit().putString("YTSEARCH", s).commit();
//                Log.d("QUERY", "Query is:" + s);

                searchOnYoutube(s);
                mSearchView.clearFocus();
                finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int nt = newText.length();
                if(nt >2){
                    searchOnYoutube(newText);
                }

                return true;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                finish();
                return false;
            }
        });
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkYTSearchInput();

    }  // onResume



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
                        } else {
                            if (list.isEmpty()) {
                                // If no searchlists saved for user
                                AlertDialogFragment.addItemAndList(
                                        SearchViewActivity.this, videoId, getString(R.string.ma_dialog_congrats),
                                        getString(R.string.ma_dialog_msg_congrats));
                            } else {
                                Log.d("ITR", "Alert should of triggered");
                                AlertDialogFragment.adjustListItems(0, list, SearchViewActivity.this, videoId, getApplicationContext());
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

    private void checkYTSearchInput(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String newQuery = sharedPreferences.getString(YT_QUERY, "");

        if (YT_QUERY == null) {
            searchOnYoutube("new+top+hits");
        }
        else {
            searchOnYoutube(newQuery);
            this.getSharedPreferences(YT_QUERY,0).edit().clear().commit();
        }
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

//                    List<String> curId = new DetailListView().convertSearchResultsToIntentIds(searchResults);
//                    loadYTWebView(curId.get(pos));
//                    webView.animate().translationY(200);

                    List<String> vidIds = new DetailListView().convertSearchResultsToIntentIds(searchResults);
                    Intent intent = YouTubeStandalonePlayer.createVideosIntent(SearchViewActivity.this, DeveloperKey.DEVELOPER_KEY, vidIds, pos, 10, true, true);
                    startActivity(intent);
                }
            });
            videosFound.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    addItemToParseArray(position);
                    return true;
                }
            });
        }  // if (videosFound != null)
    }  // addRowClickListener
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