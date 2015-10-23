package com.wavlite.WLAndroid.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;
import com.wavlite.WLAndroid.AlertDialogFragment;
import com.wavlite.WLAndroid.CreateLists;
import com.wavlite.WLAndroid.R;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends BaseActivity {
    private WebView webview;
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    public static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up webview
        webview = (WebView)findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://www.wavlite.com/api/videoPlayer.html");

        if (!isNetworkAvailable()) {
            AlertDialogFragment.dataConnection(this);
        }

        // load toolbar
        activateToolbar();

        // load music genre buttons
        loadMusicGenreButtons();

        // confirm youTube API Check
        checkYouTubeApi();

        // grab current list
        ParseUser curUser = ParseUser.getCurrentUser();
        if (curUser == null) {
            parseLoginHelper();
        }
        else {
            AlertDialogFragment.grabUserList(curUser);
        }

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
            Toast.makeText(this, errorMessage, LENGTH_LONG).show();  // error initializing
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


    private void parseLoginHelper() {
        ParseLoginBuilder builder = new ParseLoginBuilder(MainActivity.this);
        // ic_launcher_192 should not be used. Use ic_login (not yet available) instead
        // These can be put into string resources as well
        Intent parseLoginIntent = builder.setAppLogo(R.drawable.ic_launcher_192)
                .setParseLoginEnabled(true)
                .setParseLoginButtonText(getString(R.string.parse_LoginButtonText))
                .setParseSignupButtonText(getString(R.string.parse_SignupButtonText))
                .setParseLoginHelpText(getString(R.string.parse_LoginHelpText))
                .setParseLoginInvalidCredentialsToastText(getString(R.string.parse_LoginInvalid))
                .setParseLoginEmailAsUsername(true)
                .setParseSignupSubmitButtonText(getString(R.string.parse_SignupSubmitButtonText))
                .setTwitterLoginEnabled(true)
                .build();
        startActivityForResult(parseLoginIntent, 0);
    }  // parseLoginHelper


    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }  // isNetworkAvailable

    private void loadMusicGenreButtons () {

        final Intent searchIntent =  new Intent(MainActivity.this, SearchViewActivity.class);


        Button btnRock = (Button)findViewById(R.id.btn_rock);
        btnRock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+rock");
                startActivity(searchIntent);
            }
        });
        Button btnJazz = (Button) findViewById(R.id.btn_jazz);
        btnJazz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+jazz");
                startActivity(searchIntent);
            }
        });
        Button btnHipHop = (Button)findViewById(R.id.btn_hiphop);
        btnHipHop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+hiphop");
                startActivity(searchIntent);
            }
        });
        Button btnCountry = (Button) findViewById(R.id.btn_country);
        btnCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+country");
                startActivity(searchIntent);
            }
        });
        Button btnBlues = (Button) findViewById(R.id.btn_blues);
        btnBlues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+blues");
                startActivity(searchIntent);
            }
        });
        Button btnRNB = (Button) findViewById(R.id.btn_rnb);
        btnRNB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+R&B");
                startActivity(searchIntent);
            }
        });
    }

    private void setGenreSearchParams (String genre){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("YTSEARCH", genre);
        editor.commit();
    }

}  // MainActivity
