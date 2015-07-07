package com.appsneva.wliteandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by javen on 7/7/15.
 */
public class AlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.addTitle))
                .setMessage(context.getString(R.string.addMessage))
                .setPositiveButton(context.getString(R.string.errorOK), null);
        AlertDialog dialog = builder.create();
        return dialog;

    }
}
