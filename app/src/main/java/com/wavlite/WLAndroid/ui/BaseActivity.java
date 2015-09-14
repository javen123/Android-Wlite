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

        if(mToolbar == null){
            mToolbar = (Toolbar)findViewById(R.id.app_bar);
            if(mToolbar != null){
                setSupportActionBar(mToolbar);
                mToolbar.setLogo(R.drawable.ic_launcher_48);

             }
        }
        return mToolbar;
    }

    protected Toolbar activateToolbarWithHomeEnabled(){
        activateToolbar();

        if(mToolbar != null){


            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//returns back arrow for home in appbar

        }
        return mToolbar;
    }


}
