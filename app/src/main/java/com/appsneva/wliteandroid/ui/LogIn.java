package com.appsneva.wliteandroid.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.TextView;  // I'm not removing in case we need a textview for something
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.appsneva.wliteandroid.R;

public class LogIn extends Activity {

    protected EditText mUsername;
    protected EditText mPassword;
    protected Button mLogInBtn;
    protected Button mSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_log_in);

        mUsername  = (EditText) findViewById(R.id.editTextUN);
        mPassword  = (EditText) findViewById(R.id.editTextPW);
        mLogInBtn  = (Button) findViewById(R.id.btnLogIn);   // id'd in activity_log_in.xml
        mSignInBtn = (Button) findViewById(R.id.btnSignIn);  // id'd in activity_sign_up.xml

        // segue to signup page
        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });  // mSignInBtn.setOnClickListener

        // log in btn credentials and actions
        mLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                username = username.trim();
                password = password.trim();

                if (username.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LogIn.this);
                    builder.setMessage(getString(R.string.signUpErrorMsg));
                    builder.setTitle(getString(R.string.signUpErrorTitle));
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
                else {
                    // Log in
                    setProgressBarIndeterminateVisibility(true);
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            setProgressBarIndeterminateVisibility(false);
                            Log.d("LOGIN", "Log in made it this far");
                            if (e == null) {
                                Intent intent = new Intent(LogIn.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LogIn.this);
                                builder.setMessage(e.getMessage());
                                builder.setTitle(getString(R.string.signUpErrorTitle));
                                builder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });  // ParseUser.logInInBackground
                }  // IF to validate username/password
            }
        });  // mLogInBtn.setOnClickListener
    }  // onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_in, menu);
        return true;
    }  // onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }  // onOptionsItemSelected
}  // LogIn