package com.inflexionlabs.ringtoner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.model.Favourite;
import java.util.ArrayList;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {

    final ArrayList<Favourite> favouriteList;
    final Context context;
    final OnFavClick onFavClick;

    public FavouritesAdapter(ArrayList<Favourite> favouriteList, Context context, OnFavClick onFavClick) {
        this.favouriteList = favouriteList;
        this.context = context;
        this.onFavClick = onFavClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_row,parent,false);
        return new ViewHolder(view,onFavClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Favourite favourite = favouriteList.get(position);

        holder.playButton.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);

        holder.songText.setText(favourite.getText());
    }

    @Override
    public int getItemCount() {
        return favouriteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageButton playButton;
        final TextView songText;
        final ImageButton deleteButton;
        final ImageButton arrowButton;
        final ProgressBar progressBar;

        final OnFavClick onFavClick;

        public ViewHolder(@NonNull View itemView, OnFavClick onFavClick) {
            super(itemView);

            this.onFavClick = onFavClick;

            progressBar = itemView.findViewById(R.id.favProgress);
            playButton = itemView.findViewById(R.id.favImageButton);
            songText = itemView.findViewById(R.id.favTextView);
            deleteButton = itemView.findViewById(R.id.deleteFavouriteButton);
            arrowButton = itemView.findViewById(R.id.favArrowButton);

            playButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            arrowButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            onFavClick.onFavClicked(getAdapterPosition(),view);
        }
    }

    public interface OnFavClick{
        void onFavClicked(int position, View view);
    }
}
