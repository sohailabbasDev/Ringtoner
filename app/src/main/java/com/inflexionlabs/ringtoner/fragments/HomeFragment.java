package com.inflexionlabs.ringtoner.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inflexionlabs.ringtoner.activities.CategoryActivity;
import com.inflexionlabs.ringtoner.activities.PlayActivity;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.adapters.CategoriesAdapter;
import com.inflexionlabs.ringtoner.adapters.RingtonesAdapter;
import com.inflexionlabs.ringtoner.player.RingtonePlayer;
import com.inflexionlabs.ringtoner.model.Category;
import com.inflexionlabs.ringtoner.model.Favourite;
import com.inflexionlabs.ringtoner.model.Song;
import com.inflexionlabs.ringtoner.util.Util;
import com.inflexionlabs.ringtoner.viewModel.AppViewModel;
import com.inflexionlabs.ringtoner.viewModel.FavouriteViewModel;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements CategoriesAdapter.OnCategoryClickListener, RingtonesAdapter.OnSongClick{

    private CategoriesAdapter categoriesAdapter;
    private AppViewModel appViewModel;

    private RecyclerView songRecyclerView;
    private RingtonesAdapter ringtonesAdapter;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private FavouriteViewModel favViewModel;

    private int pos;

    private boolean entered = false;

    private ProgressBar loadingProgress;

    private BottomNavigationView bottomNavigationView;

    private RingtonePlayer ringtonePlayer;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ringtonePlayer =  new RingtonePlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        favViewModel = new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
                .create(FavouriteViewModel.class);

        executorService.execute(() -> Util.FAVOURITE_STRING_ARRAYLIST = (ArrayList<String>) favViewModel.getAllUid());

        loadingProgress = requireActivity().findViewById(R.id.loadProgress);

        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);

//        Toast.makeText(getContext(),"from onCreate",Toast.LENGTH_SHORT).show();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //initializing recyclerview of categories
        RecyclerView recyclerView = view.findViewById(R.id.categoryRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        //initializing app view model
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.initCategoryData();
        appViewModel.getCategoryData().observe(getViewLifecycleOwner(),categories -> categoriesAdapter.notifyDataSetChanged());

        categoriesAdapter = new CategoriesAdapter(appViewModel.getCategoryData().getValue(),getContext(), this);
        recyclerView.setAdapter(categoriesAdapter);

        songRecyclerView = view.findViewById(R.id.songsRecyclerView);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        songRecyclerView.setHasFixedSize(true);

        appViewModel.initSongsData();
        appViewModel.getSongsData().observe(getViewLifecycleOwner(),songs -> ringtonesAdapter.notifyDataSetChanged());

        ringtonesAdapter = new RingtonesAdapter(appViewModel.getSongsData().getValue(),this,Util.FAVOURITE_STRING_ARRAYLIST);
        songRecyclerView.setAdapter(ringtonesAdapter);

        ringtonesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (ringtonesAdapter.getItemCount() > 0){
                    loadingProgress.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    ringtonesAdapter.unregisterAdapterDataObserver(this);
                }
            }
        });
    }

    @Override
    public void onCategoryClick(int position) {
        Category category = Objects.requireNonNull(appViewModel.getCategoryData().getValue()).get(position);
        Intent intent = new Intent(getActivity(),CategoryActivity.class);
        intent.putExtra(Util.CATEGORY_NAME_TEXT,category.getText());
        entered = true;
        startActivity(intent);
    }

    @Override
    public void onSongClicked(int position, View view) {
        Song song = Objects.requireNonNull(appViewModel.getSongsData().getValue()).get(position);

        if (Util.isConnected(requireContext())){
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
                    Util.showToast(getContext(), Util.ADDED_TO_FAVOURITES);
                }else{
                    favViewModel.delete(favourite.getText());
                    Util.showToast(getContext(),Util.REMOVE_FROM_FAVOURITES);
                }
            }else if (view == view.findViewById(R.id.songsArrowButton)){
                entered = true;
                Intent intent = new Intent(getActivity(), PlayActivity.class);
                intent.putExtra(Util.POSITION,position);
                startActivity(intent);
            }
        }else{
            alert();
        }

    }

    public void playRingtone(String url, int id){
        RecyclerView.ViewHolder holder = songRecyclerView.findViewHolderForAdapterPosition(id);
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
                    ringtonesAdapter.notifyItemChanged(id);
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
    public void onResume() {
        super.onResume();
        if (entered){
            refreshFragment();
        }
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

    public void alert(){
         new AlertDialog.Builder(getActivity())
                .setTitle(R.string.no_connection)
                .setMessage("Check your internet connection and retry again")
                .setPositiveButton("RETRY", (dialogInterface, i) -> {
                    if (Util.isConnected(requireContext())) {
                        dialogInterface.dismiss();
                    }else {
                        getParentFragmentManager().beginTransaction().detach(HomeFragment.this).commit();
                        getParentFragmentManager().beginTransaction().attach(HomeFragment.this).commit();
                    }
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    public void refreshFragment(){
        NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.homeFragment);
    }
}