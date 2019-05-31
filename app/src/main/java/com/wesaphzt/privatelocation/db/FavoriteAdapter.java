package com.wesaphzt.privatelocation.db;

import android.content.Context;
import android.content.DialogInterface;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.wesaphzt.privatelocation.fragments.DialogFragmentFavorite;
import com.wesaphzt.privatelocation.MainActivity;
import com.wesaphzt.privatelocation.R;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteViewHolder> {

    private Context context;

    private List<Favorite> listFavorites;
    private SQLiteDB mDatabase;
    private int position;

    private View view;
    private MainActivity mainActivity;
    private DialogFragmentFavorite dfFavorite;

    public FavoriteAdapter(Context context, List<Favorite> listFavorites, MainActivity mActivity, DialogFragmentFavorite dfFavorite) {
        this.context = context;
        this.listFavorites = listFavorites;
        mDatabase = new SQLiteDB(context);
        this.mainActivity = mActivity;
        this.dfFavorite = dfFavorite;
    }

    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_favorites_list_item, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FavoriteViewHolder holder, final int mPosition) {
        final Favorite singleFavorite = listFavorites.get(mPosition);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainActivity != null){
                    //get position again in case delete operations have been done prior
                    Favorite favorite = listFavorites.get(mPosition);
                    mainActivity.setLocation(favorite.getLat(), favorite.getLong());
                }

                dfFavorite.dismiss();
            }
        });

        holder.name.setText(singleFavorite.getName());

        holder.editFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = mPosition;

                editTaskDialog(singleFavorite);
            }
        });

        holder.deleteFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = mPosition;

                //delete row from database
                mDatabase.deleteProduct(singleFavorite.getId());

                //remove and notify change
                listFavorites.remove(position);
                //notify of position changes
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listFavorites.size();
    }

    private void editTaskDialog(final Favorite favorite){
        LayoutInflater inflater = LayoutInflater.from(context);
        View subView = inflater.inflate(R.layout.dialog_favorites_add, null);

        final EditText nameField = (EditText)subView.findViewById(R.id.enter_name);
        final TextView latLngField = (TextView) subView.findViewById(R.id.display_lat_long);

        if(favorite != null){
            nameField.setText(favorite.getName());
            latLngField.setText("Latitude: " + String.valueOf(favorite.getLat() + "\nLongitude: " + String.valueOf(favorite.getLong())));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Favorite");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String name = nameField.getText().toString();

                if(TextUtils.isEmpty(name) /*|| lat_long <= 0*/){
                    Toast.makeText(context, "Can't be empty", Toast.LENGTH_LONG).show();
                } else {
                    mDatabase.updateFavorite(new Favorite(favorite.getId(), name, favorite.getLat(), favorite.getLong()));

                    listFavorites.set(position, new Favorite(favorite.getId(), name, favorite.getLat(), favorite.getLong()));
                    notifyItemChanged(position);
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

}