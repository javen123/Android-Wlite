package com.appsneva.wliteandroid.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appsneva.wliteandroid.ListTuple;
import com.appsneva.wliteandroid.PlayerActivity;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.VideoItem;
import com.appsneva.wliteandroid.YoutubeConnector;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;


import java.util.ArrayList;

import java.util.List;

public class DetailListView extends ActionBarActivity {

    private Bundle args;
    private List<VideoItem> searchResults;
    private Handler handler;
    private ListView detailList;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list_view);

        handler = new Handler();
        Intent intent = this.getIntent();

        //Grab intent
        args = intent.getBundleExtra("myListids");

        //convert intent ids to youtube searchable string
        String ids = convertIntentToVideoIds(args);

        detailList = (ListView) findViewById(R.id.detail_list_view);

        YoutubeConnector yc = new YoutubeConnector(DetailListView.this);
        searchOnYoutube(ids);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_list_view, menu);
        return true;
    }

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

    private void searchOnYoutube(final String keywords) {
        new Thread() {
            public void run() {
                YoutubeConnector yc = new YoutubeConnector(DetailListView.this);
                searchResults = yc.idSearch(keywords);
                handler.post(new Runnable() {
                    public void run() {
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
                public void onItemClick(AdapterView<?> av, View v, int pos,
                                        long id) {
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
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
                    builder.setNeutralButton("Delete Video", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                            query.whereEqualTo("objectId", listId);
                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    if(e != null){
                                        Log.d("Parse:", e.getLocalizedMessage());
                                    }
                                    else{

                                        updateListView(args, position, object);

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
        String mVIds = TextUtils.join(",",vIds);
        return mVIds;
    }
    private String convertIntentToListId(Bundle info){

        ArrayList<ListTuple> object = (ArrayList<ListTuple>) info.getSerializable("ArrayList");
        ListTuple ids = object.get(0);
        String listId = ids.getObjectId();

        return listId;
    }
    private void updateListView(Bundle info, int pos, ParseObject newList) {

        // remove positionm from ListeViewDataArray
        searchResults.remove(pos);

        //Add back new array list
        if(searchResults.size() ==0){
            newList.put("myLists", JSONObject.NULL);
            newList.saveInBackground();
            this.finish();
        }
        else {
            newList.put("myLists", searchResults);
            newList.saveInBackground();
        }
        adapter.notifyDataSetChanged();
    }
}
