package com.inflexionlabs.ringtoner.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "favourites_table", indices = @Index(value = {"text","url"}, unique = true))
public class Favourite {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "url")
    private String url;

    public Favourite() {
    }

    public Favourite(int id, String text, String url) {
        this.id = id;
        this.text = text;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
