package com.wesaphzt.privatelocation.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.wesaphzt.privatelocation.R;
import com.wesaphzt.privatelocation.db.Favorite;
import com.wesaphzt.privatelocation.db.SQLiteDB;

import es.dmoral.toasty.Toasty;

public class DialogFragmentAddFavorite extends DialogFragment {

    private Context context;

    private SQLiteDB mDatabase;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //init
        context = getContext();

        mDatabase = new SQLiteDB(context);

        assert getArguments() != null;
        final double mLat = getArguments().getDouble("lat");
        final double mLng = getArguments().getDouble("lng");

        LayoutInflater inflater = LayoutInflater.from(context);
        View subView = inflater.inflate(R.layout.dialog_favorites_add, null);

        final EditText etName = (EditText) subView.findViewById(R.id.enter_name);
        final TextView tvLatLong = (TextView) subView.findViewById(R.id.display_lat_long);

        tvLatLong.setText(getString(R.string.dialog_favorites_add_lat_long, mLat, mLng));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Favorite");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isEmpty(etName.getText())) {
                    //is empty
                    Toasty.info(context, "Can't be empty", Toast.LENGTH_SHORT, true).show();
                } else {
                    //isn't empty
                    Favorite newFavorite = new Favorite(etName.getText().toString(), mLat, mLng);
                    mDatabase.addProduct(newFavorite);

                    Toasty.success(context, "Favorite added", Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    private boolean isEmpty(Editable value) {
        return TextUtils.isEmpty(value);
    }
}