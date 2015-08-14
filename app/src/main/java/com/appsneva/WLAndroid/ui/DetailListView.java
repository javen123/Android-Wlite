package com.appsneva.WLAndroid.ui;


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
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsneva.WLAndroid.AlertDialogFragment;
import com.appsneva.WLAndroid.DeveloperKey;
import com.appsneva.WLAndroid.ListTuple;
import com.appsneva.WLAndroid.R;
import com.appsneva.WLAndroid.VideoItem;
import com.appsneva.WLAndroid.YoutubeConnector;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DetailListView extends BaseActivity {
    private Bundle args;//received bundle from MyLists page

    private static List<VideoItem> searchResults;
    private Handler handler;
    private static ListView detailList;
    private static ArrayAdapter adapter;

    private static ProgressBar mProgressBar2;

    private CheckBox check;

    public Boolean getCheckActivated() {
        return checkActivated;
    }

//    public void setCheckActivated(Boolean checkActivated) { TODO: test to make sure this is not necessary
//        this.checkActivated = checkActivated;
//    }
    private Boolean checkActivated = false;
    private ViewGroup deleteBtnView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list_view);

        //set porgress bar
        mProgressBar2 = (ProgressBar)findViewById(R.id.progressBar2);
        mProgressBar2.setVisibility(View.INVISIBLE);

        //activate toobar
        activateToolbarWithHomeEnabled();

        //array handler
        handler = new Handler();
        Intent intent = this.getIntent();

        //Grab intents
        args = intent.getBundleExtra("myListids");
        String title = intent.getStringExtra("title");

        //update page titel
        this.setTitle(title);

        //convert intent ids to youtube searchable string
        String ids = convertIntentToVideoIds(args);
        detailList = (ListView) findViewById(R.id.detail_list_view);
        YoutubeConnector yc = new YoutubeConnector(DetailListView.this);
        toggleProgressBar();
        searchOnYoutube(ids);
    }  // onCreate


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
            }  // onQueryTextSubmit

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };  // SearchView.OnQueryTextListener
        searchView.setOnQueryTextListener(textListener);
        return true;
    }  // onCreateOptionsMenu


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

        switch (item.getItemId()) {
            case R.id.action_logout:
                MyLists.myArrayTitles.clear();
                ParseUser.logOut();
                navigateToLogin();
                return true;
            case R.id.menu_create_list1:
                MyLists.addToListFromDetail = true;
                finish();
                return true;
            case R.id.edit_list_bulk:
                addDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }  // onOptionsItemSelected


    private void navigateToLogin() {
        MyLists.myArrayTitles.clear();
        Intent intent = new Intent(this, LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }  // navigateToLogin


    private void searchOnYoutube(final String keywords) {
        new Thread() {
            public void run() {
                YoutubeConnector yc = new YoutubeConnector(DetailListView.this);
                searchResults = yc.idSearch(keywords);
                handler.post(new Runnable() {
                    public void run() {
                        toggleProgressBar();
                        updateVideosFound();
                        Log.d("ITR", ""+ args);
                    }
                });
            }  // run
        }.start();  // Thread
    }  // searchOnYoutube


    private void updateVideosFound() {

        adapter = new ArrayAdapter<VideoItem>(DetailListView.this, R.layout.list_video_item, searchResults) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.list_video_item, parent, false);

                // row item tapped action / send to YouTube player / delete video on longPress
                addRowClickListener();

                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.detail_thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.detail_title);
                VideoItem searchResult = searchResults.get(position);
                Picasso.with(getApplicationContext()).load(searchResult.getThumbnail().trim()).resize(106, 60).centerCrop().into(thumbnail);
                title.setText(searchResult.getTitle());
                Log.i("YOU", "Update video");

                // activate checkbox
                check = (CheckBox)convertView.findViewById(R.id.checkBox1);
                if (checkActivated) {
                    check.setVisibility(View.VISIBLE);
                    check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                searchResults.get(position).setSelected(true);
                            }
                            if (!isChecked) {
                                searchResults.get(position).setSelected(false);
                            }
                        }  // onCheckedChanged
                    });  // check.setOnCheckedChangeListener
                }  // if (checkActivated)
                else {
                    check.setVisibility(View.INVISIBLE);
                }
                return convertView;
              }  // getView
        };  // adapter = new ArrayAdapter
        detailList.setAdapter(adapter);
    }  // updateVideosFound


    private void addRowClickListener() {
        if(detailList != null) {
            if (checkActivated == false) {
                detailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> av, View v, final int pos, long id) {
                        List<String> vidIds = convertSearchResultsToIntentIds(searchResults);
                        Intent intent = YouTubeStandalonePlayer.createVideosIntent(DetailListView.this, DeveloperKey.DEVELOPER_KEY, vidIds, pos, 10, true, true);
                        startActivity(intent);
                    }
                });  // detailList.setOnItemClickListener

                detailList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        String videoTitle = searchResults.get(position).getTitle();
                        final String listId = convertIntentToListId(args);
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailListView.this);
                        builder.setTitle(getString(R.string.dlv_dialog_title));
                        builder.setMessage("" + videoTitle + "\n ");

                        // delete selected list : DELETE BUTTON
                        builder.setNeutralButton(getString(R.string.button_delete), new DialogInterface.OnClickListener() {
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
                                            updateListView(DetailListView.this, position, object);
                                        }
                                    }
                                });
                            }  // onClick
                        });  // builder.setNeutralButton

                        // CANCEL BUTTON
                        builder.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        // MOVE TO BUTTON
                        builder.setPositiveButton(getString(R.string.button_move), new DialogInterface.OnClickListener() {
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
                                        }
                                        else {
                                            if (list.isEmpty()) {
                                                // do something
                                            }
                                            else {
                                                try{
                                                    AlertDialogFragment.adjustListItems(position,list,DetailListView.this, videoId,getApplicationContext());
                                                }
                                                catch (Exception e1){
                                                    throw e1;
                                                }
                                                finally{
                                                    searchResults.remove(position);

                                                    final ArrayList<String> temp = new ArrayList<>();
                                                    for(VideoItem x : searchResults){
                                                        temp.add(x.getId());
                                                    }
                                                    ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Lists");
                                                    query2.getInBackground(listId, new GetCallback<ParseObject>() {
                                                        @Override
                                                        public void done(ParseObject object, ParseException e) {
                                                            object.put("myLists", temp);
                                                            try {
                                                                object.save();
                                                            } catch (ParseException e1) {
                                                                e1.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }  // done
                                });  // query.findInBackground
                            }  // onClick
                        });  // builder.setPositiveButton
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    }  // onItemLongClick
                });  // detailList.setOnItemLongClickListener
            }  // if (checkActivated == false)
        }  // if(detailList != null)
    }  // addRowClickListener


     //: HELPERS
    private String convertIntentToVideoIds(Bundle info) {
        ArrayList<ListTuple> object = (ArrayList<ListTuple>) info.getSerializable("ArrayList");
        ListTuple ids = object.get(0);
        ArrayList vIds = ids.getVideoIds();
        String mVIds = TextUtils.join(",", vIds);
        return mVIds;
    }

    public List<String> convertSearchResultsToIntentIds(List<VideoItem> curList) {
        List<String> temp = new ArrayList<>();
        for(VideoItem x : curList){
            temp.add(x.getId().toString());
        }
        return temp;
    }

    private String convertIntentToListId(Bundle info) {
        ArrayList<ListTuple> object = (ArrayList<ListTuple>) info.getSerializable("ArrayList");
        ListTuple ids = object.get(0);
        String listId = ids.getObjectId();
        return listId;
    }

    public static void updateListView(final Activity activity, final int pos, ParseObject newList) {
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
                    }
                    else {
                        activity.finish();
                    }
                }
            });  // newList.saveInBackground
        }
        else {
            ArrayList<String> temp = new ArrayList<>();
            for(VideoItem x : searchResults){
                temp.add(x.getId());
            }
            newList.put("myLists", temp);
            newList.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        //do something
                    }
                    else {
                        adapter.notifyDataSetChanged();
                    }
                }
            });  // newList.saveInBackground
        }
        toggleProgressBar();
    }  // updateListView

    private static void toggleProgressBar() {
        if (mProgressBar2.getVisibility() == View.INVISIBLE) {
            mProgressBar2.setVisibility(View.VISIBLE);
            detailList.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar2.setVisibility(View.INVISIBLE);
            detailList.setVisibility(View.VISIBLE);
        }
    }  // toggleProgressBar


    private void addDelete() {
        checkActivated = true;
        adapter.notifyDataSetChanged();
        deleteBtnView = (ViewGroup)findViewById(R.id.delete_mass_view);
        deleteBtnView.setVisibility(View.VISIBLE);

        Button cancel = (Button)findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkActivated = false;
                deleteBtnView.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
            }
        });  // cancel.setOnClickListener

        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("ITR", "IS checked:");
                }
            }
        });  // check.setOnCheckedChangeListener

        Button delete = (Button)findViewById(R.id.delete_mass_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> temp = new ArrayList<>();
                final ArrayList<VideoItem> newList = new ArrayList<VideoItem>();
                //add unchecked to temp array
                for(VideoItem x : searchResults) {
                    if (x.isSelected() == false) {
                        temp.add(x.getId());
                        newList.add(x);
                    }
                }
                //update current view
                searchResults = newList;
                toggleProgressBar();

                //query and get cur object
                String listId = convertIntentToListId(args);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                query.whereEqualTo("objectId", listId);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (e != null) {
                            Log.d("Parse:", e.getLocalizedMessage());
                        }
                        else {
                            object.put("myLists", temp);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    checkActivated = false;
                                    updateVideosFound();
                                    toggleProgressBar();
                                }
                            });  // object.saveInBackground
                        }
                    }  // done
                });  // query.getFirstInBackground
                deleteBtnView.setVisibility(View.INVISIBLE);
            }  // onClick
        });  //  delete.setOnClickListener
    }  // addDelete
}  // END DetailListView
