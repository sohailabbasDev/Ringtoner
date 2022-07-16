package com.inflexionlabs.ringtoner.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inflexionlabs.ringtoner.activities.PlayActivity;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.adapters.SearchAdapter;
import com.inflexionlabs.ringtoner.player.RingtonePlayer;
import com.inflexionlabs.ringtoner.model.Song;
import com.inflexionlabs.ringtoner.util.Util;
import com.inflexionlabs.ringtoner.viewModel.AppViewModel;
import java.util.ArrayList;
import java.util.Objects;

public class SearchFragment extends Fragment implements SearchAdapter.OnSearchSongClick {

    RecyclerView searchRecyclerView;
    SearchAdapter searchAdapter;

    AppViewModel appViewModel;

    SearchView searchView;

    TextView noDataTextView;

    private int pos;

    ArrayList<Song> filterList;

    LottieAnimationView noDataAnim;

    RingtonePlayer ringtonePlayer;

    ProgressBar loadingProgress;

    public SearchFragment() {
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

        View view =inflater.inflate(R.layout.fragment_search, container, false);

        searchView = view.findViewById(R.id.searchView);

        noDataTextView = view.findViewById(R.id.noDataTextView);
        noDataAnim = view.findViewById(R.id.noDataAnimationView);

        loadingProgress = requireActivity().findViewById(R.id.loadProgress);
        loadingProgress.setVisibility(View.GONE);

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.VISIBLE);

        // Inflate the layout for this fragment
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView.performClick();

        searchView.setOnClickListener(view1 -> {
            searchView.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view1.findFocus(), 0);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ringtonePlayer.stopPlay();
                filterList(s);
                return true;
            }
        });

        //setting recycler view
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        searchRecyclerView.setHasFixedSize(true);

        //setting up app View model
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.initSongsData();
        appViewModel.getSongsData().observe(getViewLifecycleOwner(),songs -> searchAdapter.notifyDataSetChanged());

        //setting up adapter
        searchAdapter = new SearchAdapter(appViewModel.getSongsData().getValue(),this);

        //attaching adapter to recycler view
        searchRecyclerView.setAdapter(searchAdapter);

    }

    @Override
    public void onSongClicked(int position, View view) {
        Song song;
        if (filterList == null){
            song = Objects.requireNonNull(appViewModel.getSongsData().getValue()).get(position);
        }else{
            song = filterList.get(position);
        }

        if(Util.isConnected(requireContext())){
            if (view == view.findViewById(R.id.searchImageButton)){

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

            }else if (view == view.findViewById(R.id.searchArrowButton)){
                Intent intent = new Intent(getActivity(), PlayActivity.class);
                intent.putExtra(Util.CATEGORY_NAME_TEXT,song.getName());
                intent.putExtra(Util.CATEGORY_URL,song.getUri());
                startActivity(intent);
            }
        }else{
            alert();
        }
    }

    private void playRingtone(String url, int id){
        RecyclerView.ViewHolder holder = searchRecyclerView.findViewHolderForAdapterPosition(id);
        assert holder != null;

        ringtonePlayer.playWithDataSource(url, new RingtonePlayer.OnPrepareListener() {
            @Override
            public void isPreparing(boolean isPreparing) {
                if (isPreparing){
                    holder.itemView.findViewById(R.id.songSearchProgress).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.searchImageButton).setVisibility(View.INVISIBLE);
                }else{
                    holder.itemView.findViewById(R.id.songSearchProgress).setVisibility(View.GONE);
                    holder.itemView.findViewById(R.id.searchImageButton).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.searchImageButton).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_stop_circle_24,null));
                }
            }

            @Override
            public void onNext() {
                holder.itemView.findViewById(R.id.searchImageButton).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_play_circle_outline_24,null));
                holder.itemView.findViewById(R.id.songSearchProgress).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.searchImageButton).setVisibility(View.VISIBLE);
            }

            @Override
            public void completed(boolean completed) {
                if (completed){
                    searchAdapter.notifyItemChanged(id);
                }
            }

        });

    }

    public void filterList(String searchText){
        filterList = new ArrayList<>();
        for (Song song : Objects.requireNonNull(appViewModel.getSongsData().getValue())) {
            if (song.getName().toLowerCase().contains(searchText.toLowerCase())){
                filterList.add(song);
            }
        }

        if (filterList.isEmpty()){
            noDataAnim.setVisibility(View.VISIBLE);
            searchRecyclerView.setVisibility(View.INVISIBLE);
            noDataTextView.setVisibility(View.VISIBLE);
        }else{
            noDataAnim.setVisibility(View.INVISIBLE);
            searchRecyclerView.setVisibility(View.VISIBLE);
            noDataTextView.setVisibility(View.INVISIBLE);
            searchAdapter.updateList(filterList);
        }

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

    public void alert(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.no_connection)
                .setMessage("Check your internet connection and retry again")
                .setPositiveButton("RETRY", (dialogInterface, i) -> {
                    if (Util.isConnected(requireContext())) {
                        dialogInterface.dismiss();
                    }else {
                        getParentFragmentManager().beginTransaction().detach(SearchFragment.this).commit();
                        getParentFragmentManager().beginTransaction().attach(SearchFragment.this).commit();
                    }
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ringtonePlayer.destroy();
    }
}