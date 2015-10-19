package com.wavlite.WLAndroid.ui;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.wavlite.WLAndroid.R;

/**
 * Created by javen on 6/26/15.
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


    // setDisplayHomeAsUpEnabled(true) returns back arrow in toolbar
    protected Toolbar activateToolbarWithHomeEnabled() {
        activateToolbar();
        if (mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return mToolbar;
    }  // activateToolbarWithHomeEnabled

}  // BaseActivity
