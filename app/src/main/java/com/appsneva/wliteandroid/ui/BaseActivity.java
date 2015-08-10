package com.appsneva.wliteandroid.ui;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import com.appsneva.wliteandroid.R;



/**
 * Created by javen on 6/26/15.
 */
public class BaseActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    public static final String YOUTUBE_QUERY = "YT QUERY";

    protected Toolbar activateToolbar() {
        if(mToolbar == null){
            mToolbar = (Toolbar)findViewById(R.id.app_bar);
            if(mToolbar != null){
                setSupportActionBar(mToolbar);
                mToolbar.setLogo(R.drawable.ic_launcher_48);

            }
        }
        return mToolbar;
    }

    protected Toolbar activateToolbarWithjHomeEnabled(){
        activateToolbar();
        if(mToolbar != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }
        return mToolbar;
    }


}
