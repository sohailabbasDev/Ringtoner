package com.inflexionlabs.ringtoner.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inflexionlabs.ringtoner.activities.PlayActivity;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.adapters.FavouritesAdapter;
import com.inflexionlabs.ringtoner.player.RingtonePlayer;
import com.inflexionlabs.ringtoner.model.Favourite;
import com.inflexionlabs.ringtoner.util.Util;
import com.inflexionlabs.ringtoner.viewModel.FavouriteViewModel;
import java.util.ArrayList;
import java.util.Objects;

public class FavouritesFragment extends Fragment implements FavouritesAdapter.OnFavClick {

    private RecyclerView favRecyclerView;
    private FavouritesAdapter favouritesAdapter;
    private FavouriteViewModel favouriteViewModel;

    final ArrayList<Favourite> mList = new ArrayList<>();

    int pos;

    TextView text;

    LottieAnimationView noFavAnimation;

    RingtonePlayer ringtonePlayer;

    public FavouritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ringtonePlayer = new RingtonePlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.VISIBLE);

        return inflater.inflate(R.layout.fragment_favourites, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noFavAnimation = view.findViewById(R.id.animationView);

        favRecyclerView = view.findViewById(R.id.favRecyclerView);
        favRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        favRecyclerView.setHasFixedSize(true);

        favouriteViewModel = new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
                .create(FavouriteViewModel.class);

        favouriteViewModel.getAllFavourites().observe(getViewLifecycleOwner(),favourites -> {
            mList.clear();
            mList.addAll(favourites);
            favouritesAdapter.notifyDataSetChanged();
            if (favourites.isEmpty()){
                noFavAnimation.setVisibility(View.VISIBLE);
                text.setVisibility(View.VISIBLE);
            }else {
                noFavAnimation.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
            }
        });

        favouritesAdapter = new FavouritesAdapter(mList,this);
        favRecyclerView.setAdapter(favouritesAdapter);

        text = view.findViewById(R.id.noFavText);

    }

    @Override
    public void onFavClicked(int position, View view) {
        Favourite favourite = Objects.requireNonNull(favouriteViewModel.getAllFavourites().getValue()).get(position);
        if (view == view.findViewById(R.id.favImageButton)){

            if (ringtonePlayer.isPlaying()){

                if (pos == position){
                    ringtonePlayer.stopPlay();
                    pos = -2;
                }else{
                    ringtonePlayer.stopPlay();
                    pos = position;
                    playRingtone(favourite.getUrl(),pos);
                }
            }else if (pos < 0){
                pos = position;
                playRingtone(favourite.getUrl(),pos);
            }else if (pos != position){
                ringtonePlayer.stopPlay();
                pos = position;
                playRingtone(favourite.getUrl(),pos);
            }else{
                playRingtone(favourite.getUrl(),position);
            }

        }else if(view == view.findViewById(R.id.deleteFavouriteButton)){
            Util.showToast(getContext(),Util.REMOVE_FROM_FAVOURITES);
            favouriteViewModel.delete(favourite.getText());
            if (ringtonePlayer.isPlaying()){
                ringtonePlayer.stopPlay();
            }
        }else if (view == view.findViewById(R.id.favArrowButton)){
            Intent intent = new Intent(getActivity(), PlayActivity.class);
            intent.putExtra(Util.POSITION,position);
            intent.putExtra(Util.BOOLEAN,true);
            intent.putExtra(Util.CATEGORY_NAME_TEXT,favourite.getText());
            intent.putExtra(Util.CATEGORY_URL,favourite.getUrl());
            startActivity(intent);
        }

    }

    private void playRingtone(String url, int id){
        RecyclerView.ViewHolder holder = favRecyclerView.findViewHolderForAdapterPosition(id);
        assert holder != null;

        ringtonePlayer.playWithDataSource(url, new RingtonePlayer.OnPrepareListener() {
            @Override
            public void isPreparing(boolean isPreparing) {
                if (isPreparing){
                    holder.itemView.findViewById(R.id.favProgress).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.favImageButton).setVisibility(View.INVISIBLE);
                }else{
                    //while the tone is playing
                    holder.itemView.findViewById(R.id.favProgress).setVisibility(View.GONE);
                    holder.itemView.findViewById(R.id.favImageButton).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.favImageButton).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_stop_circle_24,null));
                }
            }

            @Override
            public void onNext() {
                holder.itemView.findViewById(R.id.favImageButton).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_play_circle_outline_24,null));
                holder.itemView.findViewById(R.id.favProgress).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.favImageButton).setVisibility(View.VISIBLE);
            }

            @Override
            public void completed(boolean completed) {
                if (completed){
                    favouritesAdapter.notifyItemChanged(id);
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
}