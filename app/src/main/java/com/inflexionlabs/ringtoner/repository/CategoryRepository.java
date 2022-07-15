package com.inflexionlabs.ringtoner.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inflexionlabs.ringtoner.model.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private static CategoryRepository instance;
    private final MutableLiveData<ArrayList<Category>> db = new MutableLiveData<>();
    final ArrayList<Category> data = new ArrayList<>();
    final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    //creating constructor
    public static CategoryRepository getInstance(){
        if (instance == null) {
            instance = new CategoryRepository();
        }
        return instance;
    }

    //get data when it is ready
    public MutableLiveData<ArrayList<Category>> getCategoryData(){

        if (data.size() == 0){
            loadCategoryData();
        }

        db.setValue(data);
        return db;
    }

    //Load or prepare data
    private void loadCategoryData(){

        firebaseFirestore.collection("Categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot : list) {
                    data.add(snapshot.toObject(Category.class));
                }
                db.postValue(data);
//                Log.d("tag", "this is the data....  "+data.get(1).getText());
            }

        }).addOnFailureListener(e -> {
//            Log.d("tag", "failed.... ");
        });
    }
}