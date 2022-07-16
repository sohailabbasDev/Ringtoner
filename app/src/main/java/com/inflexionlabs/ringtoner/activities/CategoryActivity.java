package com.inflexionlabs.ringtoner.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.adapters.SongsCategoryAdapter;
import com.inflexionlabs.ringtoner.player.RingtonePlayer;
import com.inflexionlabs.ringtoner.model.Favourite;
import com.inflexionlabs.ringtoner.model.Song;
import com.inflexionlabs.ringtoner.util.Util;
import com.inflexionlabs.ringtoner.viewModel.AppViewModel;
import com.inflexionlabs.ringtoner.viewModel.FavouriteViewModel;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryActivity extends AppCompatActivity implements SongsCategoryAdapter.OnSongClick{

    AppViewModel appViewModel;
    RecyclerView cRecyclerView;
    String categoryName;

    FavouriteViewModel favViewModel;
    SongsCategoryAdapter songsCategoryAdapter;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int pos;

    private RingtonePlayer ringtonePlayer;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        ringtonePlayer = new RingtonePlayer();
        favViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication())
                .create(FavouriteViewModel.class);

        executorService.execute(() -> Util.FAVOURITE_STRING_ARRAYLIST = (ArrayList<String>) favViewModel.getAllUid());

        TextView tagTextView = findViewById(R.id.tagTextView);

        appViewModel = new ViewModelProvider(this).get(AppViewModel.class);
        if (getIntent().hasExtra(Util.CATEGORY_NAME_TEXT)){
            categoryName = getIntent().getStringExtra(Util.CATEGORY_NAME_TEXT);
            tagTextView.setText(categoryName);
        }

        cRecyclerView = findViewById(R.id.categoryActRecyclerView);
        cRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        cRecyclerView.setHasFixedSize(true);

        appViewModel.getCategorySongData(categoryName).observe(this, arrayList -> songsCategoryAdapter.notifyDataSetChanged());

        songsCategoryAdapter = new SongsCategoryAdapter(appViewModel.getCategorySongData(categoryName).getValue(),this,Util.FAVOURITE_STRING_ARRAYLIST);
        cRecyclerView.setAdapter(songsCategoryAdapter);
    }

    @Override
    public void onSongClicked(int position, View view) {
        Song song = Objects.requireNonNull(appViewModel.getCategorySongData(categoryName).getValue()).get(position);

        if (Util.isConnected(this)){
            if (view == view.findViewById(R.id.playImageButton)){

                if (ringtonePlayer.isPlaying()){
                    if (pos == position){
                        ringtonePlayer.stopPlay();
                        pos = -2;
                    }else{
                        ringtonePlayer.stopPlay();
                        pos = position;
                        playRingtone(song.getUri(),pos);
                    }
                }else if (pos < 0){
                    pos = position;
                    playRingtone(song.getUri(),pos);
                }else if (pos != position){
                    ringtonePlayer.stopPlay();
                    pos = position;
                    playRingtone(song.getUri(),pos);
                }else{
                    playRingtone(song.getUri(),position);
                }


            }else if(view == view.findViewById(R.id.songsFavouriteButton)){
                Favourite favourite = new Favourite();

                favourite.setText(song.getName());
                favourite.setUrl(song.getUri());

                CheckBox checkBox = view.findViewById(R.id.songsFavouriteButton);
                if (checkBox.isChecked()){
                    favViewModel.insert(favourite);
                    Util.showToast(this, Util.ADDED_TO_FAVOURITES);
                }else{
                    favViewModel.delete(favourite.getText());
                    Util.showToast(this, Util.REMOVE_FROM_FAVOURITES);

                }
            }else if (view == view.findViewById(R.id.songsArrowButton)){
                Intent intent = new Intent(this, PlayActivity.class);
                intent.putExtra(Util.POSITION,position);
                intent.putExtra(Util.CODE_CATEGORY,categoryName);
                intent.putExtra(Util.CATEGORY_NAME_TEXT,song.getName());
                intent.putExtra(Util.CATEGORY_URL,song.getUri());
                startActivity(intent);
            }
        }else{
            Util.alert(this);
        }
    }

    private void playRingtone(String url, int id){
        RecyclerView.ViewHolder holder = cRecyclerView.findViewHolderForAdapterPosition(id);
        assert holder != null;

        ringtonePlayer.playWithDataSource(url, new RingtonePlayer.OnPrepareListener() {
            @Override
            public void isPreparing(boolean isPreparing) {
                if (isPreparing){
                    holder.itemView.findViewById(R.id.songProgress).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.playImageButton).setVisibility(View.INVISIBLE);
                }else{
                    //while the tone is playing
                    holder.itemView.findViewById(R.id.songProgress).setVisibility(View.GONE);
                    holder.itemView.findViewById(R.id.playImageButton).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.playImageButton).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_stop_circle_24,null));
                }
            }

            @Override
            public void onNext() {
                holder.itemView.findViewById(R.id.playImageButton).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_play_circle_outline_24,null));
                holder.itemView.findViewById(R.id.songProgress).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.playImageButton).setVisibility(View.VISIBLE);
            }

            @Override
            public void completed(boolean completed) {
                if (completed){
                    songsCategoryAdapter.notifyItemChanged(id);
                }
            }
        });
    }



    @Override
    public void onPause() {
        super.onPause();
        ringtonePlayer.stopPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        ringtonePlayer.stopPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ringtonePlayer.destroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}