package com.appsneva.wliteandroid.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appsneva.wliteandroid.AlertDialogFragment;
import com.appsneva.wliteandroid.DeveloperKey;
import com.appsneva.wliteandroid.ListTuple;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.VideoItem;
import com.appsneva.wliteandroid.YoutubeConnector;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DetailListView extends BaseActivity {

    private Bundle args;
    private static List<VideoItem> searchResults;
    private Handler handler;
    private ListView detailList;
    private static ArrayAdapter adapter;
    private ProgressBar mProgressBar2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list_view);

        mProgressBar2 = (ProgressBar)findViewById(R.id.progressBar2);
        mProgressBar2.setVisibility(View.INVISIBLE);
        activateToolbarWithjHomeEnabled();
        handler = new Handler();
        Intent intent = this.getIntent();

        //Grab intents
        args = intent.getBundleExtra("myListids");
        String title = intent.getStringExtra("title");

        this.setTitle(title);

        //convert intent ids to youtube searchable string
        String ids = convertIntentToVideoIds(args);

        detailList = (ListView) findViewById(R.id.detail_list_view);

        YoutubeConnector yc = new YoutubeConnector(DetailListView.this);
        toggleProgressBar();
        searchOnYoutube(ids);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_list_view, menu);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search3).getActionView();
        ComponentName cn = new ComponentName(this, MainActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        SearchView.OnQueryTextListener textListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPref.edit().putString("myQuery", query).commit();
                onSearchPressed();
                finish();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        };
        searchView.setOnQueryTextListener(textListener);

        return true;
    }

    private void onSearchPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
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
        if(id == R.id.menu_create_list1){

            MyLists.addToListFromDetail = true;
            finish();
        }
        if (id == R.id.menu_search) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToLogin() {
        MyLists.myArrayTitles.clear();
        Intent intent = new Intent(this, LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void searchOnYoutube(final String keywords) {
        new Thread() {
            public void run() {
                YoutubeConnector yc = new YoutubeConnector(DetailListView.this);
                searchResults = yc.idSearch(keywords);
                handler.post(new Runnable() {
                    public void run() {
                        toggleProgressBar();
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }

    private void updateVideosFound() {
        adapter = new ArrayAdapter<VideoItem>(DetailListView.this, R.layout.list_video_item, searchResults) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_video_item, parent, false);
                }

                // row item tapped action / send to YouTube player / delete video on longPress
                addRowClickListener();

                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.detail_thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.detail_title);

                VideoItem searchResult = searchResults.get(position);

                Picasso.with(getApplicationContext()).load(searchResult.getThumbnail()).into(thumbnail);
                title.setText(searchResult.getTitle());
                Log.i("YOU", "Update video");
                return convertView;
            }
        };
        detailList.setAdapter(adapter);
    }

    private void addRowClickListener(){

        if(detailList != null){
            detailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> av, View v, final int pos,
                                        long id) {

                    List<String> vidIds = convertSearchResultsToIntentIds(searchResults);
                    Intent intent = YouTubeStandalonePlayer.createVideosIntent(DetailListView.this, DeveloperKey.DEVELOPER_KEY, vidIds, pos, 10, true, true);
                    startActivity(intent);
                }
            });
            detailList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                String videoTitle = searchResults.get(position).getTitle();
                final String listId = convertIntentToListId(args);
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailListView.this);
                builder.setTitle("" + videoTitle);

                // delete selected list
                builder.setNeutralButton("Delete this item?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                    query.whereEqualTo("objectId", listId);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                        if (e != null) {
                            Log.d("Parse:", e.getLocalizedMessage());
                        } else {

                            updateListView(args, position, object);

                        }
                        }
                    });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton("Move to", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        final String videoId = searchResults.get(position).getId().toString();
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

                                        //do something

                                    } else {

                                        AlertDialogFragment.adjustListItems(list, DetailListView.this, videoId, getApplicationContext());
                                        updateListView(args, position, list.get(position));
                                    }
                                }
                            }
                        });

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
                }
            });
        }
    }


     //: HELPERS
    private String convertIntentToVideoIds(Bundle info) {
        ArrayList<ListTuple> object = (ArrayList<ListTuple>) info.getSerializable("ArrayList");
        ListTuple ids = object.get(0);
        ArrayList vIds = ids.getVideoIds();
        String mVIds = TextUtils.join(",", vIds);
        return mVIds;
    }

    public List<String> convertSearchResultsToIntentIds(List<VideoItem> curList){
        List<String> temp = new ArrayList<>();
        for(VideoItem x : curList){
            temp.add(x.getId().toString());
        }
        return temp;
    }
    private String convertIntentToListId(Bundle info){

        ArrayList<ListTuple> object = (ArrayList<ListTuple>) info.getSerializable("ArrayList");
        ListTuple ids = object.get(0);
        String listId = ids.getObjectId();
        return listId;
    }

    public void updateListView(Bundle info, final int pos, ParseObject newList) {

        searchResults.remove(pos);
        toggleProgressBar();
        //Add back new array list
        if(searchResults.size() == 0) {
            newList.put("myLists", JSONObject.NULL);
            newList.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        //do something
                    } else {

                        finish();
                    }
                }
            });
        }
        else {
            ArrayList<String> temp = new ArrayList<>();
            for(VideoItem x : searchResults){
                temp.add(x.getId().toString());
            }
            newList.put("myLists", temp);
            newList.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null){
                        //do something
                    }
                    else{

                        adapter.notifyDataSetChanged();

                    }
                }
            });
        }

        toggleProgressBar();
    }

    private void toggleProgressBar(){
        if(mProgressBar2.getVisibility() == View.INVISIBLE){
            mProgressBar2.setVisibility(View.VISIBLE);
            detailList.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar2.setVisibility(View.INVISIBLE);
            detailList.setVisibility(View.VISIBLE);
        }
    }


}
