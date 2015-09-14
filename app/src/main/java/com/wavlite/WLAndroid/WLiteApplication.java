package com.wavlite.WLAndroid;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;

/**
 * Created by javen on 6/19/15.
 */
public class WLiteApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "ykrFkxjHcVz8j8oBSc8ZFpXH0gSu99vkKEhzt6Hs", "pmzgjReSbDsmwvZXLNoqNGXtIqo5ZCbu04WwuSrM");
        FacebookSdk.sdkInitialize(getApplicationContext());

    }
}
