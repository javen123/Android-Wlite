package com.appsneva.wliteandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by javen on 7/7/15.
 */
public class AlertDialogFragment extends DialogFragment {

    public String alertTitle;
    public String alertMessage;

    public static void adjustListItems(List<ParseObject> list, Activity activity, final String videoId, final Context context){

        ArrayList<String> listTitles = new ArrayList<String>();
        for (ParseObject titles : list) {
            String title = (titles.get("listTitle").toString());
            listTitles.add(title);
        }
        final CharSequence[] titles = listTitles.toArray(new CharSequence[listTitles.size()]);


        final AlertDialog.Builder success = new AlertDialog.Builder(activity);
        success.setTitle("Add to ");
        success.setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // pull list title
                String titleTapped = titles[which].toString();

                // convertid to Parse array type
                final ArrayList<String> moreToAdd;
                moreToAdd = new ArrayList<String>();
                moreToAdd.add(videoId);

                //query by list title
                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Lists");
                query1.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                query1.orderByAscending("listTitle");
                query1.whereEqualTo("listTitle", titleTapped);
                query1.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
//                                            Array id = new Array();
                        if (object.get("myLists") == null) {

                            object.put("myLists", moreToAdd);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.d("Parse:", e.getLocalizedMessage());
                                    } else {
                                        Log.d("Parse:", "item was supposed to be saved");
                                        Toast.makeText(context, "Video saved", LENGTH_LONG).show();
                                    }

                                }

                            });
                        } else {
                            ArrayList<String> addMore = new ArrayList<String>();

                            JSONArray myList = object.getJSONArray("myLists");
                            for (int i = 0; i < myList.length(); i++) {
                                try {
                                    addMore.add(myList.get(i).toString());
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            addMore.add(videoId);
                            object.put("myLists", addMore);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.d("Parse:", e.getLocalizedMessage());
                                    } else {
                                        Log.d("Parse:", "item was supposed to be saved");
                                        Toast.makeText(context, "Video saved", LENGTH_LONG).show();
                                    }

                                }

                            });

                        }
                    }
                });
            }
        });
        AlertDialog alert = success.create();
        alert.show();
    }



}
