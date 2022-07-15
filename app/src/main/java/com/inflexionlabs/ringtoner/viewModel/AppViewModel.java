package com.inflexionlabs.ringtoner.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.inflexionlabs.ringtoner.model.Category;
import com.inflexionlabs.ringtoner.model.Song;
import com.inflexionlabs.ringtoner.repository.CategoryRepository;
import com.inflexionlabs.ringtoner.repository.SongsRepository;

import java.util.ArrayList;

public class AppViewModel extends ViewModel {

    private MutableLiveData<ArrayList<Category>> categoryData;
    private MutableLiveData<ArrayList<Song>> songsData;
    private MutableLiveData<ArrayList<Song>> categorySongData;

    public void initCategoryData(){
        if (categoryData != null) {
            return;
        }
        categoryData = CategoryRepository.getInstance().getCategoryData();

    }

    public void initSongsData(){
        if (songsData != null) {
            return;
        }
        songsData = SongsRepository.getInstance().getSongsData();
    }

    public MutableLiveData<ArrayList<Song>> getCategorySongData(String category){
        if (categorySongData == null){
            categorySongData = SongsRepository.getInstance().getCategorySongList(category);
        }
        return categorySongData;
    }

    public MutableLiveData<ArrayList<Category>> getCategoryData(){
        return categoryData;
    }

    public MutableLiveData<ArrayList<Song>> getSongsData(){
        return songsData;
    }

}