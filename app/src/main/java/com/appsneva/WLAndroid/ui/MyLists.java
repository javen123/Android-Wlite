package com.appsneva.WLAndroid.ui;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;
import com.appsneva.WLAndroid.ListTuple;
import com.appsneva.WLAndroid.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class MyLists extends BaseActivity {
    private ListView myListView;
    public static ArrayList<ParseObject> myArrayTitles = new ArrayList<ParseObject>();
    private TextView noLists;
    private ArrayAdapter adapter;
    public static Boolean addToListFromDetail = false;  // return bool activated from detail list page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lists);

        myListView = (ListView) findViewById(R.id.my_list_titles);
        noLists = (TextView) findViewById(R.id.no_list_text);
        activateToolbarWithjHomeEnabled();  // does this create the "back" arrow?

        if (myArrayTitles.size() != 0) {
            noLists.setVisibility(View.INVISIBLE);
            loadListNames();
        }
        else {
            updatedListTitles();
        }
        activateToolbar();
        addRowClickListener();
    }  // onCreate


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_lists, menu);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search1).getActionView();
        ComponentName cn = new ComponentName(this, MainActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));

        SearchView.OnQueryTextListener textListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPref.edit().putString("myQuery", query).commit();
                Log.d("ITR", "Mylist query is "+ query);
                finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };  // textListener = new SearchView.OnQueryTextListener
        searchView.setOnQueryTextListener(textListener);
        return true;
    }  // onCreateOptionsMenu


    @Override
    protected void onResume() {
        super.onResume();
        if(addToListFromDetail == true){
            addNewItemToList();
            addToListFromDetail = false;
        }
        else {
            return;
        }
    }  // onResume


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            myArrayTitles.clear();
            ParseUser.logOut();
            navigateToLogin();
        }
        if (id == R.id.menu_search) {

        }
        if (id == R.id.menu_create_list) {
            addNewItemToList();
        }
        return super.onOptionsItemSelected(item);
    }  // onOptionsItemSelected


    private void navigateToLogin() {
        myArrayTitles.clear();
        Intent intent = new Intent(this, LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }  // navigateToLogin

    private void loadListNames() {
        ArrayList<String> values = new ArrayList<String>();
        for (ParseObject object : myArrayTitles) {
            String title = (object.get("listTitle").toString());
            values.add(title);
        }
        this.adapter = new ArrayAdapter<String>(MyLists.this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        noLists.setVisibility(View.INVISIBLE);
        myListView.setVisibility(View.VISIBLE);
        myListView.setAdapter(adapter);
    }  // loadListNames


    private void addRowClickListener() {
        if (myListView != null) {
            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                    ArrayList<ListTuple> temp = new ArrayList<ListTuple>();
                    String title = myArrayTitles.get(pos).get("listTitle").toString();
                    String vid = myArrayTitles.get(pos).getObjectId();
                    JSONArray myList = myArrayTitles.get(pos).getJSONArray("myLists");
                    ArrayList xList = (ArrayList) myArrayTitles.get(pos).getList("myLists");
                    ListTuple i;
                    i = new ListTuple(vid, xList);
                    temp.add(i);

                    if (xList != null) {
                        Intent intent = new Intent(MyLists.this, DetailListView.class);
                        Bundle args = new Bundle();
                        args.putSerializable("ArrayList", temp);
                        intent.putExtra("myListids", args);
                        intent.putExtra("title", title);
                        startActivity(intent);
                        // startActivityForResult(intent, REQUEST_CODE);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_empty_list), Toast.LENGTH_LONG).show();
                    }
                }  // onItemClick
            });  // myListView.setOnItemClickListener
        }  // if-myListView


        myListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // create alert to give edit title options with long press
                AlertDialog.Builder builder = new AlertDialog.Builder(MyLists.this);
                // Get the searchlist title from the array
                builder.setTitle(getString(R.string.builder_title));
                builder.setMessage("" + myArrayTitles.get(position).get("listTitle").toString());

                // delete selected list
                builder.setNeutralButton(getString(R.string.button_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseObject deletedList = myArrayTitles.get(position);
                        deletedList.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.d("Parse Error: ", e.getLocalizedMessage());
                                }
                                else {
                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                                    query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                                    query.addAscendingOrder("listTitle");
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> list, ParseException e) {
                                            if (e != null) {
                                                Log.d("Error with list pull: ", e.getLocalizedMessage());
                                            }
                                            else {
                                                // clear current list and update with new material
                                                myArrayTitles.clear();
                                                for (ParseObject object : list) {
                                                    MyLists.myArrayTitles.add(object);
                                                }
                                                if (myArrayTitles.size() == 0) {
                                                    noLists.setVisibility(View.VISIBLE);
                                                    myListView.setVisibility(View.INVISIBLE);
                                                }
                                                else {
                                                    loadListNames();
                                                }
                                            }  // main if-else
                                        }  // done
                                    });  // query.findInBackground
                                }  // main if-else
                            }  // done
                        });  // deletedList.deleteInBackground
                    }  // onClick
                });  // builder.setNeutralButton


                builder.setPositiveButton(getString(R.string.button_edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // pull reusable alert from layouts
                        View v = getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
                        final AlertDialog.Builder editTitle = new AlertDialog.Builder(MyLists.this);

                        editTitle.setView(v);
                        TextView editAlertTitle = (TextView) v.findViewById(R.id.alert_edit_title_text);
                        final EditText newTitle = (EditText) v.findViewById(R.id.first_title);
                        editAlertTitle.setText(getString(R.string.ml_dialog_rename));

                        editTitle.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });  // editTitle.setNegativeButton

                        editTitle.setPositiveButton(getString(R.string.button_save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // grab new title name from user input
                                String mNewTitle = newTitle.getText().toString();

                                //grab parse object to update
                                ParseObject object = myArrayTitles.get(position);
                                object.put("listTitle", mNewTitle);
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.d("Parse: ", e.getLocalizedMessage());
                                        }
                                        else {
                                            // immediately grab new lists for new Listview
                                            myArrayTitles.clear();
                                            updatedListTitles();

                                            //return alert dialog box of success
                                            final AlertDialog.Builder success = new AlertDialog.Builder(MyLists.this);
                                            success.setTitle(getString(R.string.ml_dialog_updated));
                                            success.setMessage(getString((R.string.ml_dialog_msg_updated)));
                                            success.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });  // success.setPositiveButton
                                            AlertDialog alert = success.create();
                                            alert.show();
                                        }
                                    }  // done
                                });  // object.saveInBackground
                            }  // onClick
                        });  // editTitle.setPositiveButton
                        AlertDialog editTapped = editTitle.create();
                        editTapped.show();
                    }  // onClick
                });  // builder.setPositiveButton

                builder.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });  // myListView.setOnItemLongClickListener
    }  // addRowClickListener


    private void addNewItemToList() {
        View v = getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
        final AlertDialog.Builder newTitle = new AlertDialog.Builder(MyLists.this);
        newTitle.setView(v);

        // Edit the searchlist title name
        TextView newListAdd = (TextView)v.findViewById(R.id.alert_edit_title_text);
        newListAdd.setText(getString(R.string.ml_dialog_name));
        final EditText newListTitleAdd = (EditText)v.findViewById(R.id.first_title);
        newListTitleAdd.setHint(getString(R.string.ml_dialog_hint_name));

        newTitle.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });  // newTitle.setNegativeButton

        newTitle.setPositiveButton(getString(R.string.button_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String mTitle = newListTitleAdd.getText().toString();
                ParseObject newListTitle = new ParseObject("Lists");
                newListTitle.put("listTitle", mTitle);
                ParseRelation<ParseObject> relation = newListTitle.getRelation("createdBy");
                relation.add(ParseUser.getCurrentUser());

                newListTitle.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            // empty if body?
                        }
                        else {
                            final AlertDialog.Builder success = new AlertDialog.Builder(MyLists.this);
                            success.setTitle(getString(R.string.ml_dialog_save));
                            success.setMessage(getString(R.string.ml_dialog_msg_save));
                            success.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updatedListTitles();
                                }
                            });  // success.setPositiveButton
                            AlertDialog alert = success.create();
                            alert.show();
                        }
                    }  // done
                });  // newListTitle.saveInBackground
            }  // onClick
        });  // newTitle.setPositiveButton
        AlertDialog alert = newTitle.create();
        alert.show();
    }  // addNewItemToList


    public void updatedListTitles() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
        query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
        query.orderByAscending("listTitle");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) {
                    Log.d("Error with list pull: ", e.getLocalizedMessage());
                }
                else {
                    // clear current list and update with new material
                    myArrayTitles.clear();
                    for (ParseObject object : list) {

                        MyLists.myArrayTitles.add(object);
                    }
                    if (myArrayTitles.isEmpty()) {
                        noLists.setVisibility(View.VISIBLE);
                        myListView.setVisibility(View.INVISIBLE);
                    }
                    else {
                        loadListNames();
                    }
                }
            }  // done
        });  // query.findInBackground
    }  // updatedListTitles
}  // MyLists (END CLASS)
