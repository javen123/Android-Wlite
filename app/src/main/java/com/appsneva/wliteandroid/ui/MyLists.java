package com.appsneva.wliteandroid.ui;


import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import android.widget.TextView;

import com.appsneva.wliteandroid.MyListItem;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.SearchActivity;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class MyLists extends BaseActivity {

    private ListView myListView;
    public static ArrayList<String> myArrayTitles = new ArrayList<String>();
    private TextView noLists;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lists);

        myListView = (ListView)findViewById(R.id.my_list_titles);
        noLists = (TextView) findViewById(R.id.no_list_text);

        if(myArrayTitles != null){
            noLists.setVisibility(noLists.INVISIBLE);
            updateListTitles();

        }
        else {
            noLists.setVisibility(noLists.VISIBLE);
            myListView.setVisibility(myListView.INVISIBLE);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_lists, menu);

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


        return super.onOptionsItemSelected(item);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private void updateListTitles() {

           String[] values = myArrayTitles.toArray(new String[myArrayTitles.size()]);

            this.adapter = new ArrayAdapter<String>(MyLists.this, android.R.layout.simple_list_item_1,android.R.id.text1, values);
            myListView.setVisibility(myListView.VISIBLE);
            myListView.setAdapter(adapter);

    }
}
