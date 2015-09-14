package com.wavlite.WLAndroid.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;
import com.wavlite.WLAndroid.AlertDialogFragment;
import com.wavlite.WLAndroid.CreateLists;
import com.wavlite.WLAndroid.R;

import java.util.Arrays;

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

        // grab current list
        ParseUser curUser = ParseUser.getCurrentUser();
        if (curUser == null){
            parseLoginHelper();
        }
        else {
            AlertDialogFragment.grabUserList(curUser);
        }

    //TODO: wrap into connection test


    }  // onCreate

    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);

        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.cancel();
                }
            });
        }
        else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage = String.format(getString(R.string.ma_error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, LENGTH_LONG).show();
        }
    }  // checkYouTubeApi

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
            case R.id.main_action_logout:
                com.wavlite.WLAndroid.ui.MyLists.myArrayTitles.clear();
                ParseUser.logOut();
                navigateToLogin();
                return true;
            case R.id.main_menu_search:
                Intent searchIntent = new Intent(MainActivity.this, SearchViewActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.main_menu_my_lists:
                Intent intent = new Intent(this, com.wavlite.WLAndroid.ui.MyLists.class);
                startActivity(intent);
                return true;
            case R.id.main_create_searchlist:
                CreateLists cl = new CreateLists();
                cl.addNewItemToList(MainActivity.this);

            default:
                return super.onOptionsItemSelected(item);
        }
    }  // onOptionsItemSelected

    private void navigateToLogin() {
        MyLists.myArrayTitles.clear();
        parseLoginHelper();
    }  // navigateToLogin


    private void parseLoginHelper(){
        ParseLoginBuilder builder = new ParseLoginBuilder(MainActivity.this);
        Intent parseLoginIntent = builder.setAppLogo(R.drawable.ic_launcher_192)
                .setParseLoginEnabled(true)
                .setParseLoginButtonText("Go")
                .setParseSignupButtonText("Register")
                .setParseLoginHelpText("Forgot password?")
                .setParseLoginInvalidCredentialsToastText("Your email and/or password is not correct")
                .setParseLoginEmailAsUsername(true)
                .setParseSignupSubmitButtonText("Submit registration")
                .setFacebookLoginEnabled(true)
                .setFacebookLoginButtonText("Facebook")
                .setFacebookLoginPermissions(Arrays.asList("public_profile", "user_friends"))
                .setTwitterLoginEnabled(true)
                .setTwitterLoginButtontext("Twitter")
                .build();
        startActivityForResult(parseLoginIntent, 0);
    }
}
