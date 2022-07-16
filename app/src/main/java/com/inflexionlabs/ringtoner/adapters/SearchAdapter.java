package com.inflexionlabs.ringtoner.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.model.Song;
import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    ArrayList<Song> songArrayList;
    final OnSearchSongClick onSearchSongClick;

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(ArrayList<Song> arrayList){
        this.songArrayList = arrayList;
        notifyDataSetChanged();
    }

    public SearchAdapter(ArrayList<Song> songArrayList, OnSearchSongClick onSearchSongClick) {
        this.songArrayList = songArrayList;
        this.onSearchSongClick = onSearchSongClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_row,parent,false);
        return new ViewHolder(view, onSearchSongClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songArrayList.get(position);

        holder.songText.setText(song.getName());
    }



    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final ImageButton playButton;
        final TextView songText;
        final ImageButton arrowButton;
        final OnSearchSongClick onSearchSongClick;

        public ViewHolder(@NonNull View itemView, OnSearchSongClick onSearchSongClick) {
            super(itemView);

            this.onSearchSongClick = onSearchSongClick;

            playButton = itemView.findViewById(R.id.searchImageButton);
            songText = itemView.findViewById(R.id.searchTextView);
            arrowButton = itemView.findViewById(R.id.searchArrowButton);

            playButton.setOnClickListener(this);
            arrowButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onSearchSongClick.onSongClicked(getAdapterPosition(),view);
        }
    }

    public interface OnSearchSongClick{
        void onSongClicked(int position, View view);
    }

}