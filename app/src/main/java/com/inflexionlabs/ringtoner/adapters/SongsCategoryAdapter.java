package com.inflexionlabs.ringtoner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.model.Song;
import java.util.ArrayList;

public class SongsCategoryAdapter extends RecyclerView.Adapter<SongsCategoryAdapter.ViewHolder> {

    final ArrayList<Song> arrayList;
    final OnSongClick onSongClick;
    final ArrayList<String> integerList;

    public SongsCategoryAdapter(ArrayList<Song> arrayList, OnSongClick onSongClick, ArrayList<String> integerList) {
        this.arrayList = arrayList;
        this.onSongClick = onSongClick;
        this.integerList = integerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row,parent,false);
        return new ViewHolder(view, onSongClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = arrayList.get(position);
        holder.songText.setText(song.getName());

        if (integerList.contains(song.getName())){
            holder.favButton.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageButton playButton;
        final TextView songText;
        final CheckBox favButton;
        final ImageButton arrowButton;
        final OnSongClick onSongClick;

        public ViewHolder(@NonNull View itemView, OnSongClick onSongClick) {
            super(itemView);

            this.onSongClick = onSongClick;

            playButton = itemView.findViewById(R.id.playImageButton);
            songText = itemView.findViewById(R.id.songsTextView);
            favButton = itemView.findViewById(R.id.songsFavouriteButton);
            arrowButton = itemView.findViewById(R.id.songsArrowButton);


            playButton.setOnClickListener(this);
            favButton.setOnClickListener(this);
            arrowButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onSongClick.onSongClicked(getAdapterPosition(), view);
        }
    }

    public interface OnSongClick{
        void onSongClicked(int position, View view);
    }
}
