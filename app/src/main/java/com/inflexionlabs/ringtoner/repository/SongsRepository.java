package com.inflexionlabs.ringtoner.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inflexionlabs.ringtoner.model.Song;
import java.util.ArrayList;
import java.util.List;

public class SongsRepository {
    private static SongsRepository instance;
    private final MutableLiveData<ArrayList<Song>> songsList = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Song>> categorySongList = new MutableLiveData<>();
    final ArrayList<Song> data = new ArrayList<>();
    final ArrayList<Song> catData = new ArrayList<>();
    final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    final CollectionReference collectionReference = firebaseFirestore.collection("Songs");

    public static SongsRepository getInstance() {
        if (instance == null) {
            instance = new SongsRepository();
        }
        return instance;
    }

    public MutableLiveData<ArrayList<Song>> getSongsData(){

        if (data.size() == 0){
            loadSongsData();
        }
        songsList.setValue(data);
        return songsList;
    }

    public MutableLiveData<ArrayList<Song>> getCategorySongList(String category){
        catData.clear();
        loadSongsByCategory(category);
        return categorySongList;
    }


    private void loadSongsData(){

        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot : list) {
                    data.add(snapshot.toObject(Song.class));
                }
                songsList.postValue(data);
//                Log.d("tag", "this is the data....  "+data.get(1).getText());
            }

        }).addOnFailureListener(e -> {
//            Log.d("tag", "failed.... ");
        });

    }

    private void loadSongsByCategory(String category){
        collectionReference.whereEqualTo("category",category).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot : list) {
                    catData.add(snapshot.toObject(Song.class));
                }
                categorySongList.postValue(catData);
//                Log.d("tag", "this is the data....  "+data.get(1).getText());
            }
        }).addOnFailureListener(e -> {
//            Log.d("tag", "failed.... ");
        });
        categorySongList.setValue(catData);
    }
}
