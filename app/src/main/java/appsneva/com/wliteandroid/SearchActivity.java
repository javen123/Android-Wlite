package appsneva.com.wliteandroid;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;



import com.squareup.picasso.Picasso;

import java.util.List;

import appsneva.com.wliteandroid.ui.BaseActivity;


/**
 * Created by javen on 6/25/15.
 */
public class SearchActivity extends BaseActivity {

    private SearchView mSearchView;
//    private EditText searchInput;
//    private ListView videosFound;
//    private List<VideoItem> searchResults;
//    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search);
        activateToolbarWithjHomeEnabled();

//        searchInput = (EditText) findViewById(R.id.search_input);
//        videosFound = (ListView) findViewById(R.id.videos_found);

//        handler = new Handler();
//
//        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    searchOnYoutube(v.getText().toString());
//                    return false;
//                }
//                return true;
//            }
//        });
//        addClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.search_view);
        mSearchView = (SearchView)searchItem.getActionView();
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPref.edit().putString(YOUTUBE_QUERY, s).commit();
                mSearchView.clearFocus();
                finish();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
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

//    private void searchOnYoutube(final String keywords){
//        new Thread(){
//            public void run(){
//                YoutubeConnector yc = new YoutubeConnector(SearchActivity.this);
//                searchResults = yc.search(keywords);
//
//                handler.post(new Runnable(){
//                    public void run(){
//                        updateVideosFound();
//                    }
//                });
//            }
//        }.start();
//    }
//
//    private void updateVideosFound() {
//        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults) {
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                if (convertView == null) {
//                    convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
//                }
//                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
//                TextView title = (TextView) convertView.findViewById(R.id.title);
//                TextView description = (TextView) convertView.findViewById(R.id.video_description);
//
//                VideoItem searchResult = searchResults.get(position);
//
//                Picasso.with(getApplicationContext()).load(searchResult.getThumbnail()).into(thumbnail);
//                title.setText(searchResult.getTitle());
//                description.setText(searchResult.getDescription());
//                return convertView;
//            }
//        };
//
//        videosFound.setAdapter(adapter);
//    }
//
//    private void addClickListener(){
//        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> av, View v, int pos,
//                                    long id) {
//                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
//                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
//                startActivity(intent);
//            }
//
//        });
//    }
}