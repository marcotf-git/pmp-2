package com.example.androidstudio.popularmovies;

/**
 * This class will store the schema for review object
 */

public class Review {

    private String mAuthor;

    private String mContent;

    private String mUrl;

    Review(String author, String content, String url){
        mAuthor = author;
        mContent = content;
        mUrl = url;
    }

    String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    String getUrl() {
        return mUrl;
    }
}
