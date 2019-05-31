package com.wesaphzt.privatelocation.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wesaphzt.privatelocation.MainActivity;
import com.wesaphzt.privatelocation.R;
import com.wesaphzt.privatelocation.db.Favorite;
import com.wesaphzt.privatelocation.db.FavoriteAdapter;
import com.wesaphzt.privatelocation.db.SQLiteDB;

import java.util.List;

public class DialogFragmentFavorite extends DialogFragment {

    private SQLiteDB mDatabase;

    private MainActivity mainActivity;

    public DialogFragmentFavorite(Context context, MainActivity mActivity) {
        mainActivity = mActivity;
    }

    //default constructor
    public DialogFragmentFavorite() { }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Context context = getContext();

        //setup alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.dialog_favorites_title));

        //set listview as view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_favorites, null);
        builder.setView(view);

        RecyclerView favoriteView = (RecyclerView) view.findViewById(R.id.favorite_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

        //empty view
        TextView tvEmpty = view.findViewById(R.id.tvEmpty);

        favoriteView.setLayoutManager(linearLayoutManager);
        favoriteView.setHasFixedSize(true);

        //db
        mDatabase = new SQLiteDB(context);
        List<Favorite> allFavorites = mDatabase.listFavorites();

        //if db empty
        if(allFavorites.isEmpty()) {
            favoriteView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);

        } else {
            favoriteView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            //pass mainActivity so we can callback in FavoriteAdapter
            FavoriteAdapter mAdapter = new FavoriteAdapter(context, allFavorites, mainActivity, DialogFragmentFavorite.this);
            favoriteView.setAdapter(mAdapter);
        }

        return builder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mDatabase != null){
            mDatabase.close();
        }
    }
}
