package com.wesaphzt.privatelocation.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.wesaphzt.privatelocation.R;

public class FragmentSettings extends PreferenceFragmentCompat {

    private SharedPreferences prefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        setHasOptionsMenu(true);
        Context context = getContext();

        //this static call will reset default values only on the first ever read
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        cbRandomize = findPreference("RANDOMIZE_LOCATION");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set title
        getActivity().setTitle("Settings");

        //background color
        view.setBackgroundColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //hide action bar menu
        menu.setGroupVisible(R.id.menu_top, false);
        menu.setGroupVisible(R.id.menu_bottom, false);

        super.onPrepareOptionsMenu(menu);
    }
}