package com.wavlite.WLAndroid.ui;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.wavlite.WLAndroid.R;


/**
 * Created by javen on 6/26/15.
 * Sets up the base activity for the application: set's toobar properties and 
 * sets up a toolbar that also includes the internal up/home back button. Note 
 * that setDisplayHomeAsUpEnabled(true) returns back arrow in toolbar, the 
 * activateToolbar does not.
 */
public class BaseActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    protected Toolbar activateToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar)findViewById(R.id.app_bar);
            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
                mToolbar.setLogo(R.drawable.ic_toolbar);
            }
        }
        return mToolbar;
    }  // activateToolbar

    protected Toolbar activateToolbarWithHomeEnabled() {
        activateToolbar();
        if (mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return mToolbar;
    }  // activateToolbarWithHomeEnabled
}  // BaseActivity