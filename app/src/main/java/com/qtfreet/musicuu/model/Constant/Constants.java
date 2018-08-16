package com.qtfreet.musicuu.model.Constant;

import android.os.Environment;

/**
 * Created by qtfreet00 on 2016/8/17.
 */
public class Constants {
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String KEY = "key";
    public static final String TYPE = "type";
    public static final String MUSICUU_PREF = "com.qtfreet.musicuu_preferences";
    public static final String SAVE_PATH = "SavePath";
    public static final String AUTO_CHECK = "AutoCheck";
    public static final String IS_FIRST_RUN = "isfirst";
    public static final String SONG_ID = "songid";
    public static final String PLAY_URLS = "playSongs";
    public static final String PLAY_LRCS = "playlrc";
    public static final String PLAY_NAMES = "playname";
    public static final String POSITION = "position";
    public static final String MUSIC_HOST = "http://api.itwusun.com/music/search/%s/1?format=json&sign=a5cc0a8797539d3a1a4f7aeca5b695b9&keyword=%s";
    public static final String lyricPath = Environment.getExternalStorageDirectory() + "/LyricDemo/lyric/";
    public static final String YinyueTai = "keyword";
    public static final String IS_FIRST_SEARCH = "issearch";
    public static final String DEFAULT_DOWNLOAD_PATH = "MusicDownload";
}
