package com.wesaphzt.privatelocation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION =    5;
    private static final String DATABASE_NAME = "favorites";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_FAVORITENAME = "favoritename";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LNG = "lng";

    public SQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "(" + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_FAVORITENAME + " TEXT," + COLUMN_LAT + " DOUBLE," + COLUMN_LNG + " DOUBLE" + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public List<Favorite> listFavorites(){
        String sql = "select * from " + TABLE_FAVORITES;
        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();
        List<Favorite> storeFavorites = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.moveToFirst()){
            do{
                int id = Integer.parseInt(cursor.getString(0));
                String name = cursor.getString(1);
                Double lat = cursor.getDouble(2);
                Double lng = cursor.getDouble(3);
                storeFavorites.add(new Favorite(id, name, lat, lng));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return storeFavorites;
    }

    public void addProduct(Favorite favorite){
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITENAME, favorite.getName());
        values.put(COLUMN_LAT, favorite.getLat());
        values.put(COLUMN_LNG, favorite.getLong());
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_FAVORITES, null, values);
    }

    void updateFavorite(Favorite favorite){
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITENAME, favorite.getName());
        values.put(COLUMN_LAT, favorite.getLat());
        values.put(COLUMN_LNG, favorite.getLong());
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_FAVORITES, values, COLUMN_ID    + "    = ?", new String[] { String.valueOf(favorite.getId())});
    }

    public Favorite findFavorite(String name){
        String query = "Select * FROM "    + TABLE_FAVORITES + " WHERE " + COLUMN_FAVORITENAME + " = " + "name";
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        Favorite mFavorite = null;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            int id = Integer.parseInt(cursor.getString(0));
            String favoriteName = cursor.getString(1);
            Double favoriteLat = cursor.getDouble(2);
            Double favoriteLng = cursor.getDouble(3);

            mFavorite = new Favorite(id, favoriteName, favoriteLat, favoriteLng);
        }
        cursor.close();
        return mFavorite;
    }

    void deleteProduct(int id){
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_ID + "    = ?", new String[] { String.valueOf(id)});
    }
}