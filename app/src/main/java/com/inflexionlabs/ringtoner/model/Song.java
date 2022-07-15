package com.inflexionlabs.ringtoner.model;

public class Song {

    private String category;
    private String name;
    private String uri;

    public Song() {
    }

    public Song(String name, String uri, String category) {
        this.name = name;
        this.uri = uri;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
