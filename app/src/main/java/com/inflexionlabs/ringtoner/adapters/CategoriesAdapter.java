package com.inflexionlabs.ringtoner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.model.Category;
import java.util.ArrayList;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private final ArrayList<Category> arrayList;
    private final Context context;
    private final OnCategoryClickListener onCategoryClickListener;

    public CategoriesAdapter(ArrayList<Category> arrayList, Context context, OnCategoryClickListener onCategoryClickListener) {
        this.arrayList = arrayList;
        this.context = context;
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row,parent,false);
        return new ViewHolder(view, onCategoryClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = arrayList.get(position);
        holder.textView.setText(category.getText());
        Glide.with(context).asBitmap().load(category.getUrl()).centerCrop().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView imageView;
        private final TextView textView;
        private final OnCategoryClickListener onCategoryClickListener;

        public ViewHolder(@NonNull View itemView, OnCategoryClickListener onCategoryClickListener) {
            super(itemView);

            this.onCategoryClickListener = onCategoryClickListener;
            imageView = itemView.findViewById(R.id.categoryImageView);
            textView = itemView.findViewById(R.id.categoryTextView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onCategoryClickListener.onCategoryClick(getAdapterPosition());
        }
    }

    public interface OnCategoryClickListener{
        void onCategoryClick(int position);
    }
}
