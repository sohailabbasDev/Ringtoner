package com.inflexionlabs.ringtoner.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.inflexionlabs.ringtoner.database.FavouriteRoomDatabase;
import com.inflexionlabs.ringtoner.model.Favourite;
import com.inflexionlabs.ringtoner.model.FavouriteDao;
import java.util.List;

public class FavouritesRepository {

    private final FavouriteDao favouriteDao;
    private final LiveData<List<Favourite>> allFavourites;

    public FavouritesRepository(Application application){
        FavouriteRoomDatabase db = FavouriteRoomDatabase.getInstance(application);
        favouriteDao = db.favouriteDao();
        allFavourites = favouriteDao.getAll();
    }

    public LiveData<List<Favourite>> getAllData(){
        return allFavourites;
    }

    public void insert (Favourite favourite){
        FavouriteRoomDatabase.dataBaseWriteExecutor.execute(() -> favouriteDao.insert(favourite));
    }

    public List<String> getAllUid(){
        return favouriteDao.getAllUid();
    }

    public void delete(String text){
        FavouriteRoomDatabase.dataBaseWriteExecutor.execute(() -> favouriteDao.delete(text));
    }
}