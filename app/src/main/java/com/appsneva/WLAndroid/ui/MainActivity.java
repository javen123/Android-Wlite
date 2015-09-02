package com.appsneva.WLAndroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.appsneva.WLAndroid.AlertDialogFragment;
import com.appsneva.WLAndroid.CreateLists;
import com.appsneva.WLAndroid.R;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.parse.ParseUser;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends BaseActivity {

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private WebView webview;



    public static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //set up webview
        webview = (WebView)findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://www.wavlite.com/api/videoPlayer.html");



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

    }  // onCreate



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
            case R.id.main_menu_search:
                Intent searchIntent = new Intent(MainActivity.this, SearchViewActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.main_menu_my_lists:
                Intent intent = new Intent(this, MyLists.class);
                startActivity(intent);
                return true;
            case R.id.main_create_searchlist:
                CreateLists cl = new CreateLists();
                cl.addNewItemToList(MainActivity.this);

            default:
                return super.onOptionsItemSelected(item);
        }
    }  // onOptionsItemSelected






    // add dialog and button control for add item to list



}
