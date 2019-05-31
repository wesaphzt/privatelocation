package com.wesaphzt.privatelocation.db;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.wesaphzt.privatelocation.R;

public class FavoriteViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    ImageView deleteFavorite;
    ImageView editFavorite;

    FavoriteViewHolder(View itemView) {
        super(itemView);

        name = (TextView)itemView.findViewById(R.id.favorite_name);
        deleteFavorite = (ImageView)itemView.findViewById(R.id.delete_favorite);
        editFavorite = (ImageView)itemView.findViewById(R.id.edit_favorite);
    }
}