package com.example.androidstudio.popularmovies;

/**
 * This class will store the schema for the video object
 */

class Video {

    private String mVideoKey;
    private String mVideoName;
    private String mVideoSite;

    Video(String videoKey, String videoName, String videoSite){
        mVideoKey = videoKey;
        mVideoName = videoName;
        mVideoSite = videoSite;
    }

    String getVideoKey() {

        return mVideoKey;
    }

    String getVideoName() {

        return mVideoName;
    }

    String getVideoSite() {

        return mVideoSite;
    }

}
