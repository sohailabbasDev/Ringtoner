package com.inflexionlabs.ringtoner.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Favourite favourite);

    @Query("SELECT * FROM favourites_table ORDER BY id ASC")
    LiveData<List<Favourite>> getAll();

    @Query("SELECT text FROM favourites_table")
    List<String> getAllUid();

    @Query("DELETE FROM favourites_table")
    void deleteAll();

    @Query("DELETE FROM favourites_table WHERE text = :text")
    void delete(String text);

}
