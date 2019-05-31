package com.wesaphzt.privatelocation.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wesaphzt.privatelocation.R;
import com.wesaphzt.privatelocation.interfaces.ILatLong;

import es.dmoral.toasty.Toasty;

public class DialogFragmentGoTo extends DialogFragment {

    private Context context;

    private Double dLat;
    private final Double latMin = -90.0;
    private final Double latMax =  90.0;

    private Double dLng;
    private final Double lngMin = -180.0;
    private final Double lngMax =  180.0;

    private ILatLong mCallback;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //init
        context = getContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View subView = inflater.inflate(R.layout.dialog_go_to, null);

        final EditText etLatitude = (EditText) subView.findViewById(R.id.enter_latitude);
        final EditText etLongitude = (EditText) subView.findViewById(R.id.enter_longitude);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_go_to_title);
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("GO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!isEmpty(etLatitude.getText()) || !isEmpty(etLongitude.getText())) {
                    //isn't empty
                    try {
                        //is double
                        dLat = Double.parseDouble(etLatitude.getText().toString());
                        dLng = Double.parseDouble(etLongitude.getText().toString());

                        if (!isMinMax(dLat, latMin, latMax) || !isMinMax(dLng, latMin, latMax)) {
                            //isn't valid lat/long range
                            Toasty.info(context, getString(R.string.dialog_go_to_invalid_range), Toast.LENGTH_SHORT, true).show();
                        } else {
                            //is valid lat/long range
                            mCallback.setLocation(dLat, dLng);
                        }
                    } catch (Exception e1) {
                        //isn't double
                        Toasty.info(context, R.string.dialog_go_to_invalid_number, Toast.LENGTH_SHORT, true).show();
                    }
                } else {
                    //is empty
                    Toasty.info(context, R.string.dialog_go_to_invalid_empty, Toast.LENGTH_SHORT, true).show();
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

    private boolean isMinMax(Double num, Double min, Double max) {
        return (num > min && num < max);
    }

    private boolean isEmpty(Editable value){
        return TextUtils.isEmpty(value);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (ILatLong) context;
        }
        catch (ClassCastException e) {
            Log.d("DialogFragmentGoTo", "ILatLong not implemented");
        }
    }
}
