package com.inflexionlabs.ringtoner.viewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.inflexionlabs.ringtoner.model.Favourite;
import com.inflexionlabs.ringtoner.repository.FavouritesRepository;
import java.util.List;

public class FavouriteViewModel extends AndroidViewModel {

    public static FavouritesRepository favouritesRepository;
    public final LiveData<List<Favourite>> allFavourites;

    public FavouriteViewModel(@NonNull Application application) {
        super(application);
        favouritesRepository = new FavouritesRepository(application);
        allFavourites = favouritesRepository.getAllData();
    }

    public LiveData<List<Favourite>> getAllFavourites(){
        return allFavourites;
    }

    public void insert (Favourite favourite) { favouritesRepository.insert(favourite); }

    public void delete (String text) { favouritesRepository.delete(text);}

    public List<String> getAllUid(){return favouritesRepository.getAllUid();}


}
