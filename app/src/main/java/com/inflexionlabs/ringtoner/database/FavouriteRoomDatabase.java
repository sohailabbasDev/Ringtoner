package com.inflexionlabs.ringtoner.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.inflexionlabs.ringtoner.model.Favourite;
import com.inflexionlabs.ringtoner.model.FavouriteDao;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Favourite.class},version = 1, exportSchema = false)
public abstract class FavouriteRoomDatabase extends RoomDatabase {

    public abstract FavouriteDao favouriteDao();
    public static final int NUMBER_OF_THREADS = 3;

    public static volatile FavouriteRoomDatabase INSTANCE;
    public static final ExecutorService dataBaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static FavouriteRoomDatabase getInstance(final Context context){
        if (INSTANCE == null) {
            synchronized (FavouriteRoomDatabase.class){
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),FavouriteRoomDatabase.class,"favourites_database")
                            .addCallback(roomDatabaseCallback)
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public static final RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            dataBaseWriteExecutor.execute(()->{
                FavouriteDao favouriteDao = INSTANCE.favouriteDao();
                favouriteDao.deleteAll();

            });
        }
    };

}
