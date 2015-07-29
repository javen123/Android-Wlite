package com.appsneva.wliteandroid.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;
import com.appsneva.wliteandroid.ListTuple;
import com.appsneva.wliteandroid.R;
import com.appsneva.wliteandroid.SearchActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lists);

        myListView = (ListView) findViewById(R.id.my_list_titles);
        noLists = (TextView) findViewById(R.id.no_list_text);
        /*
         * This is probably the place to do a check for stored lists.
         * I'm thinking that it would make sense to put in some sort
         * of check that will not allow the displays of titles that
         * are the same name maybe? What I do know is that if I clear
         * the app data (clear app cache) then there is no problem.
         */
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
        getMenuInflater().inflate(R.menu.menu_my_lists, menu);

        return true;
    }  // onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            myArrayTitles.clear();
            ParseUser.logOut();
            navigateToLogin();
        }

        if (id == R.id.menu_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        if (id == R.id.menu_create_list) {
            addNewItemToList();
        }

        return super.onOptionsItemSelected(item);
    }  // onOptionsItemSelected

    private void navigateToLogin() {
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
                        startActivity(intent);

                    }
                    else {
                        Toast.makeText(getApplicationContext(), "This searchlist is empty", Toast.LENGTH_LONG).show();
                    }
                }  // onItemClick
            });  // myListView.setOnItemClickListener
        }  // if-myListView

        myListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                // create alert to give edit title options with long press
                AlertDialog.Builder builder = new AlertDialog.Builder(MyLists.this);
                builder.setTitle("" + myArrayTitles.get(position).get("listTitle").toString());

                // delete selected list
                builder.setNeutralButton("Delete Searchlist", new DialogInterface.OnClickListener() {
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
                                    });
                                }  // main if-else
                            }  // done
                        });  // deletedList.deleteInBackground
                    }  // onClick
                });  // builder.setNeutralButton

                builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // pull reusable alert from layouts
                        View v = getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
                        final AlertDialog.Builder editTitle = new AlertDialog.Builder(MyLists.this);

                        editTitle.setView(v);
                        TextView editAlertTitle = (TextView) v.findViewById(R.id.alert_edit_title_text);
                        final EditText newTitle = (EditText) v.findViewById(R.id.first_title);
                        editAlertTitle.setText("Change title to:");
                        editTitle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });  // editTitle.setNegativeButton

                        editTitle.setPositiveButton("Save", new DialogInterface.OnClickListener() {
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
                                            success.setTitle("Updated");
                                            success.setMessage("Searchlist title saved!");
                                            success.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

        TextView newListAdd = (TextView)v.findViewById(R.id.alert_edit_title_text);
        newListAdd.setText("Name Your Searchlist");
        final EditText newListTitleAdd = (EditText)v.findViewById(R.id.first_title);
        newListTitleAdd.setHint("Enter a Searchlist title");

        newTitle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });  // newTitle.setNegativeButton

        newTitle.setPositiveButton("Save", new DialogInterface.OnClickListener() {
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
                            success.setTitle("Saved!");
                            success.setMessage("Your Searchlist has been created");
                            success.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
