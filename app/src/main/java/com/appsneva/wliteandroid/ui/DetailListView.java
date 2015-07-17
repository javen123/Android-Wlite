package com.appsneva.wliteandroid.ui;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appsneva.wliteandroid.PlayerActivity;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.VideoItem;
import com.appsneva.wliteandroid.YoutubeConnector;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailListView extends ActionBarActivity {

    ArrayList<String> passedIds = new ArrayList<String>();
    private List<VideoItem> searchResults;
    private Handler handler;
    private ListView detailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list_view);

        handler = new Handler();
        Intent intent = this.getIntent();
        passedIds = intent.getExtras().getStringArrayList("myIdList");
        detailList = (ListView)findViewById(R.id.detail_list_view);

        String ids = TextUtils.join(",", passedIds);
        Log.d("ids", ids);

        searchOnYoutube("ids");
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
        final ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.list_video_item, searchResults) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_video_item, parent, false);
                }

                // row item tapped action / send to YouTube player
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
        }
    }


}
